-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 003_functions.sql — Funciones, Triggers y RPCs
--
-- Ejecutar DESPUÉS de 002_rls_policies.sql
-- ============================================================================

-- ============================================================================
-- 1. TRIGGER: Auto-crear perfil al registrar usuario en auth.users
-- ============================================================================
CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS TRIGGER AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, email, username, role)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', ''),
        COALESCE(NEW.email, ''),
        COALESCE(NEW.raw_user_meta_data->>'username', ''),
        'customer'
    );
    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- Crear trigger en auth.users
DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW EXECUTE FUNCTION public.handle_new_user();

-- ============================================================================
-- 2. FUNCIÓN: Generar número de orden secuencial NIN-XXXX
-- ============================================================================
CREATE SEQUENCE IF NOT EXISTS public.order_number_seq START WITH 1 INCREMENT BY 1;

CREATE OR REPLACE FUNCTION public.generate_order_number()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.order_number IS NULL OR NEW.order_number = '' THEN
        NEW.order_number := 'NIN-' || LPAD(nextval('public.order_number_seq')::TEXT, 4, '0');
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS set_order_number ON public.orders;
CREATE TRIGGER set_order_number
    BEFORE INSERT ON public.orders
    FOR EACH ROW EXECUTE FUNCTION public.generate_order_number();

-- ============================================================================
-- 3. RPC: Obtener catálogo de productos con info completa
-- ============================================================================
CREATE OR REPLACE FUNCTION public.get_product_catalog(
    p_category_slug TEXT DEFAULT NULL,
    p_search TEXT DEFAULT NULL,
    p_only_featured BOOLEAN DEFAULT FALSE,
    p_only_new BOOLEAN DEFAULT FALSE,
    p_branch_id UUID DEFAULT NULL,
    p_limit INTEGER DEFAULT 50,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    name TEXT,
    slug TEXT,
    description TEXT,
    price DECIMAL,
    old_price DECIMAL,
    is_new BOOLEAN,
    is_featured BOOLEAN,
    category_name TEXT,
    category_slug TEXT,
    primary_image_url TEXT,
    total_stock BIGINT,
    branch_stock INTEGER
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id,
        p.name,
        p.slug,
        p.description,
        p.price,
        p.old_price,
        p.is_new,
        p.is_featured,
        c.name AS category_name,
        c.slug AS category_slug,
        pi.image_url AS primary_image_url,
        COALESCE(SUM(bi.stock), 0)::BIGINT AS total_stock,
        CASE
            WHEN p_branch_id IS NOT NULL THEN
                (SELECT COALESCE(bix.stock, 0) FROM public.branch_inventory bix
                 WHERE bix.product_id = p.id AND bix.branch_id = p_branch_id)
            ELSE NULL
        END AS branch_stock
    FROM public.products p
    JOIN public.categories c ON p.category_id = c.id
    LEFT JOIN public.product_images pi ON pi.product_id = p.id AND pi.is_primary = TRUE
    LEFT JOIN public.branch_inventory bi ON bi.product_id = p.id
    WHERE p.is_active = TRUE
        AND c.is_active = TRUE
        AND (p_category_slug IS NULL OR c.slug = p_category_slug)
        AND (p_search IS NULL OR p.name ILIKE '%' || p_search || '%' OR p.description ILIKE '%' || p_search || '%')
        AND (p_only_featured = FALSE OR p.is_featured = TRUE)
        AND (p_only_new = FALSE OR p.is_new = TRUE)
    GROUP BY p.id, p.name, p.slug, p.description, p.price, p.old_price,
             p.is_new, p.is_featured, c.name, c.slug, pi.image_url, p_branch_id
    ORDER BY p.is_featured DESC, p.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 4. RPC: Obtener detalle completo de un producto
-- ============================================================================
CREATE OR REPLACE FUNCTION public.get_product_detail(p_product_id UUID)
RETURNS TABLE (
    id UUID,
    name TEXT,
    slug TEXT,
    description TEXT,
    price DECIMAL,
    old_price DECIMAL,
    is_new BOOLEAN,
    brand TEXT,
    category_name TEXT,
    category_slug TEXT,
    images JSONB,
    specifications JSONB,
    branch_availability JSONB
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        p.id,
        p.name,
        p.slug,
        p.description,
        p.price,
        p.old_price,
        p.is_new,
        p.brand,
        c.name AS category_name,
        c.slug AS category_slug,
        -- Imágenes como JSON array
        COALESCE(
            (SELECT jsonb_agg(
                jsonb_build_object(
                    'id', pimg.id,
                    'image_url', pimg.image_url,
                    'is_primary', pimg.is_primary,
                    'sort_order', pimg.sort_order
                ) ORDER BY pimg.sort_order
            )
            FROM public.product_images pimg
            WHERE pimg.product_id = p.id),
            '[]'::JSONB
        ) AS images,
        -- Especificaciones como JSON array
        COALESCE(
            (SELECT jsonb_agg(
                jsonb_build_object(
                    'key', ps.spec_key,
                    'value', ps.spec_value
                ) ORDER BY ps.sort_order
            )
            FROM public.product_specifications ps
            WHERE ps.product_id = p.id),
            '[]'::JSONB
        ) AS specifications,
        -- Disponibilidad por sucursal
        COALESCE(
            (SELECT jsonb_agg(
                jsonb_build_object(
                    'branch_id', b.id,
                    'branch_name', b.name,
                    'stock', bi.stock
                )
            )
            FROM public.branch_inventory bi
            JOIN public.branches b ON b.id = bi.branch_id AND b.is_active = TRUE
            WHERE bi.product_id = p.id),
            '[]'::JSONB
        ) AS branch_availability
    FROM public.products p
    JOIN public.categories c ON p.category_id = c.id
    WHERE p.id = p_product_id AND p.is_active = TRUE;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 5. RPC: Crear una orden completa (transaccional)
-- ============================================================================
CREATE OR REPLACE FUNCTION public.create_order(
    p_payment_method TEXT,
    p_branch_id UUID DEFAULT NULL,
    p_address_id UUID DEFAULT NULL,
    p_coupon_code TEXT DEFAULT NULL,
    p_notes TEXT DEFAULT NULL
)
RETURNS UUID AS $$
DECLARE
    v_order_id UUID;
    v_subtotal DECIMAL(12,2) := 0;
    v_discount DECIMAL(12,2) := 0;
    v_total DECIMAL(12,2);
    v_coupon_id UUID;
    v_coupon_discount_type TEXT;
    v_coupon_discount_value DECIMAL(12,2);
    v_coupon_max_discount DECIMAL(12,2);
    v_cart_item RECORD;
    v_user_id UUID := auth.uid();
BEGIN
    -- Verificar que el carrito no está vacío
    IF NOT EXISTS (SELECT 1 FROM public.cart_items WHERE user_id = v_user_id) THEN
        RAISE EXCEPTION 'El carrito está vacío';
    END IF;

    -- Calcular subtotal desde el carrito
    SELECT COALESCE(SUM(p.price * ci.quantity), 0)
    INTO v_subtotal
    FROM public.cart_items ci
    JOIN public.products p ON p.id = ci.product_id
    WHERE ci.user_id = v_user_id;

    -- Validar y aplicar cupón si existe
    IF p_coupon_code IS NOT NULL AND p_coupon_code != '' THEN
        SELECT id, discount_type, discount_value, max_discount
        INTO v_coupon_id, v_coupon_discount_type, v_coupon_discount_value, v_coupon_max_discount
        FROM public.coupons
        WHERE code = UPPER(p_coupon_code)
            AND is_active = TRUE
            AND (ends_at IS NULL OR ends_at >= NOW())
            AND (max_uses IS NULL OR used_count < max_uses)
            AND (min_purchase IS NULL OR min_purchase <= v_subtotal);

        IF v_coupon_id IS NOT NULL THEN
            IF v_coupon_discount_type = 'percentage' THEN
                v_discount := v_subtotal * (v_coupon_discount_value / 100);
                IF v_coupon_max_discount IS NOT NULL AND v_discount > v_coupon_max_discount THEN
                    v_discount := v_coupon_max_discount;
                END IF;
            ELSE
                v_discount := v_coupon_discount_value;
            END IF;
        END IF;
    END IF;

    v_total := v_subtotal - v_discount;
    IF v_total < 0 THEN v_total := 0; END IF;

    -- Crear la orden
    INSERT INTO public.orders (user_id, payment_method, subtotal, discount, total, branch_id, address_id, coupon_id, notes)
    VALUES (v_user_id, p_payment_method, v_subtotal, v_discount, v_total, p_branch_id, p_address_id, v_coupon_id, p_notes)
    RETURNING id INTO v_order_id;

    -- Crear order_items desde el carrito (snapshot de datos)
    INSERT INTO public.order_items (order_id, product_id, product_name, product_image, unit_price, quantity, line_total)
    SELECT
        v_order_id,
        ci.product_id,
        p.name,
        (SELECT pi.image_url FROM public.product_images pi WHERE pi.product_id = p.id AND pi.is_primary = TRUE LIMIT 1),
        p.price,
        ci.quantity,
        p.price * ci.quantity
    FROM public.cart_items ci
    JOIN public.products p ON p.id = ci.product_id
    WHERE ci.user_id = v_user_id;

    -- Descontar stock de la sucursal (si se especificó)
    IF p_branch_id IS NOT NULL THEN
        FOR v_cart_item IN
            SELECT ci.product_id, ci.quantity
            FROM public.cart_items ci
            WHERE ci.user_id = v_user_id
        LOOP
            UPDATE public.branch_inventory
            SET stock = GREATEST(stock - v_cart_item.quantity, 0)
            WHERE branch_id = p_branch_id AND product_id = v_cart_item.product_id;
        END LOOP;
    END IF;

    -- Registrar uso de cupón
    IF v_coupon_id IS NOT NULL THEN
        INSERT INTO public.coupon_usage (coupon_id, user_id, order_id)
        VALUES (v_coupon_id, v_user_id, v_order_id);

        UPDATE public.coupons SET used_count = used_count + 1 WHERE id = v_coupon_id;
    END IF;

    -- Limpiar carrito
    DELETE FROM public.cart_items WHERE user_id = v_user_id;

    RETURN v_order_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 6. RPC: Dashboard de admin — KPIs
-- ============================================================================
CREATE OR REPLACE FUNCTION public.admin_get_dashboard()
RETURNS TABLE (
    total_orders BIGINT,
    pending_orders BIGINT,
    completed_orders BIGINT,
    total_revenue DECIMAL,
    revenue_this_month DECIMAL,
    total_customers BIGINT,
    new_customers_this_month BIGINT,
    total_products BIGINT,
    out_of_stock_products BIGINT,
    low_stock_alerts BIGINT
) AS $$
BEGIN
    -- Solo admins
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'No autorizado';
    END IF;

    RETURN QUERY
    SELECT
        (SELECT COUNT(*) FROM public.orders)::BIGINT,
        (SELECT COUNT(*) FROM public.orders WHERE status = 'pending')::BIGINT,
        (SELECT COUNT(*) FROM public.orders WHERE status = 'completed')::BIGINT,
        (SELECT COALESCE(SUM(total), 0) FROM public.orders WHERE status IN ('completed', 'delivered'))::DECIMAL,
        (SELECT COALESCE(SUM(total), 0) FROM public.orders
         WHERE status IN ('completed', 'delivered')
         AND created_at >= date_trunc('month', NOW()))::DECIMAL,
        (SELECT COUNT(*) FROM public.profiles WHERE role = 'customer')::BIGINT,
        (SELECT COUNT(*) FROM public.profiles
         WHERE role = 'customer'
         AND created_at >= date_trunc('month', NOW()))::BIGINT,
        (SELECT COUNT(*) FROM public.products WHERE is_active = TRUE)::BIGINT,
        (SELECT COUNT(DISTINCT bi.product_id) FROM public.branch_inventory bi
         WHERE bi.stock = 0)::BIGINT,
        (SELECT COUNT(*) FROM public.branch_inventory
         WHERE stock > 0 AND stock <= min_stock)::BIGINT;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 7. RPC: Admin — Obtener órdenes con filtros
-- ============================================================================
CREATE OR REPLACE FUNCTION public.admin_get_orders(
    p_status TEXT DEFAULT NULL,
    p_search TEXT DEFAULT NULL,
    p_date_from TIMESTAMPTZ DEFAULT NULL,
    p_date_to TIMESTAMPTZ DEFAULT NULL,
    p_limit INTEGER DEFAULT 50,
    p_offset INTEGER DEFAULT 0
)
RETURNS TABLE (
    id UUID,
    order_number TEXT,
    status TEXT,
    payment_method TEXT,
    payment_status TEXT,
    total DECIMAL,
    customer_name TEXT,
    customer_email TEXT,
    items_count BIGINT,
    created_at TIMESTAMPTZ
) AS $$
BEGIN
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'No autorizado';
    END IF;

    RETURN QUERY
    SELECT
        o.id,
        o.order_number,
        o.status,
        o.payment_method,
        o.payment_status,
        o.total,
        pr.full_name AS customer_name,
        pr.email AS customer_email,
        (SELECT COUNT(*) FROM public.order_items oi WHERE oi.order_id = o.id) AS items_count,
        o.created_at
    FROM public.orders o
    JOIN public.profiles pr ON pr.id = o.user_id
    WHERE (p_status IS NULL OR o.status = p_status)
        AND (p_search IS NULL OR o.order_number ILIKE '%' || p_search || '%'
             OR pr.full_name ILIKE '%' || p_search || '%')
        AND (p_date_from IS NULL OR o.created_at >= p_date_from)
        AND (p_date_to IS NULL OR o.created_at <= p_date_to)
    ORDER BY o.created_at DESC
    LIMIT p_limit OFFSET p_offset;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 8. RPC: Admin — Actualizar estado de orden
-- ============================================================================
CREATE OR REPLACE FUNCTION public.admin_update_order_status(
    p_order_id UUID,
    p_new_status TEXT,
    p_admin_notes TEXT DEFAULT NULL
)
RETURNS BOOLEAN AS $$
DECLARE
    v_old_status TEXT;
BEGIN
    IF NOT public.is_admin() THEN
        RAISE EXCEPTION 'No autorizado';
    END IF;

    SELECT status INTO v_old_status FROM public.orders WHERE id = p_order_id;

    IF v_old_status IS NULL THEN
        RAISE EXCEPTION 'Orden no encontrada';
    END IF;

    UPDATE public.orders
    SET status = p_new_status,
        admin_notes = COALESCE(p_admin_notes, admin_notes)
    WHERE id = p_order_id;

    -- Registrar en log de actividad
    INSERT INTO public.activity_log (user_id, action, entity_type, entity_id, details)
    VALUES (
        auth.uid(),
        'status_change',
        'order',
        p_order_id::TEXT,
        jsonb_build_object('old_status', v_old_status, 'new_status', p_new_status)
    );

    -- Crear notificación para el cliente
    INSERT INTO public.notifications (title, message, notification_type, target_type, target_user_id, action_type, action_value)
    SELECT
        'Actualización de pedido',
        'Tu pedido ' || o.order_number || ' cambió a: ' || p_new_status,
        'order_update',
        'specific_user',
        o.user_id,
        'order',
        p_order_id::TEXT
    FROM public.orders o
    WHERE o.id = p_order_id;

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 9. RPC: Admin — Log de actividad
-- ============================================================================
CREATE OR REPLACE FUNCTION public.admin_log_activity(
    p_action TEXT,
    p_entity_type TEXT,
    p_entity_id TEXT DEFAULT NULL,
    p_details JSONB DEFAULT NULL
)
RETURNS VOID AS $$
BEGIN
    IF NOT public.is_admin() THEN
        RETURN;
    END IF;

    INSERT INTO public.activity_log (user_id, action, entity_type, entity_id, details)
    VALUES (auth.uid(), p_action, p_entity_type, p_entity_id, p_details);
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

-- ============================================================================
-- 10. RPC: Obtener notificaciones no leídas del usuario
-- ============================================================================
CREATE OR REPLACE FUNCTION public.get_unread_notifications()
RETURNS TABLE (
    id UUID,
    title TEXT,
    message TEXT,
    notification_type TEXT,
    action_type TEXT,
    action_value TEXT,
    sent_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT
        n.id, n.title, n.message, n.notification_type,
        n.action_type, n.action_value, n.sent_at
    FROM public.notifications n
    WHERE (n.target_type = 'all' OR n.target_user_id = auth.uid())
        AND n.is_read = FALSE
    ORDER BY n.sent_at DESC
    LIMIT 50;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 11. RPC: Obtener configuración pública de la app (para la app móvil)
-- ============================================================================
CREATE OR REPLACE FUNCTION public.get_app_config()
RETURNS TABLE (
    setting_key TEXT,
    value TEXT,
    value_type TEXT
) AS $$
BEGIN
    RETURN QUERY
    SELECT s.setting_key, s.value, s.value_type
    FROM public.app_settings s
    WHERE s.is_public = TRUE;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- 12. RPC: Validar cupón antes de aplicar
-- ============================================================================
CREATE OR REPLACE FUNCTION public.validate_coupon(p_code TEXT, p_subtotal DECIMAL DEFAULT 0)
RETURNS TABLE (
    is_valid BOOLEAN,
    coupon_id UUID,
    discount_type TEXT,
    discount_value DECIMAL,
    max_discount DECIMAL,
    calculated_discount DECIMAL,
    error_message TEXT
) AS $$
DECLARE
    v_coupon RECORD;
    v_usage_count INTEGER;
    v_discount DECIMAL := 0;
BEGIN
    SELECT * INTO v_coupon
    FROM public.coupons c
    WHERE c.code = UPPER(p_code);

    IF v_coupon IS NULL THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL, 'Cupón no encontrado'::TEXT;
        RETURN;
    END IF;

    IF NOT v_coupon.is_active THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL, 'Cupón inactivo'::TEXT;
        RETURN;
    END IF;

    IF v_coupon.ends_at IS NOT NULL AND v_coupon.ends_at < NOW() THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL, 'Cupón expirado'::TEXT;
        RETURN;
    END IF;

    IF v_coupon.max_uses IS NOT NULL AND v_coupon.used_count >= v_coupon.max_uses THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL, 'Cupón agotado'::TEXT;
        RETURN;
    END IF;

    IF v_coupon.min_purchase IS NOT NULL AND p_subtotal < v_coupon.min_purchase THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL,
            ('Compra mínima requerida: Bs ' || v_coupon.min_purchase)::TEXT;
        RETURN;
    END IF;

    -- Verificar uso por usuario
    SELECT COUNT(*) INTO v_usage_count
    FROM public.coupon_usage cu
    WHERE cu.coupon_id = v_coupon.id AND cu.user_id = auth.uid();

    IF v_coupon.max_uses_per_user IS NOT NULL AND v_usage_count >= v_coupon.max_uses_per_user THEN
        RETURN QUERY SELECT FALSE, NULL::UUID, NULL::TEXT, NULL::DECIMAL, NULL::DECIMAL, 0::DECIMAL, 'Ya usaste este cupón'::TEXT;
        RETURN;
    END IF;

    -- Calcular descuento
    IF v_coupon.discount_type = 'percentage' THEN
        v_discount := p_subtotal * (v_coupon.discount_value / 100);
        IF v_coupon.max_discount IS NOT NULL AND v_discount > v_coupon.max_discount THEN
            v_discount := v_coupon.max_discount;
        END IF;
    ELSE
        v_discount := v_coupon.discount_value;
    END IF;

    RETURN QUERY SELECT TRUE, v_coupon.id, v_coupon.discount_type, v_coupon.discount_value,
        v_coupon.max_discount, v_discount, NULL::TEXT;
END;
$$ LANGUAGE plpgsql STABLE SECURITY DEFINER;

-- ============================================================================
-- FIN DE 003_functions.sql
-- Continuar con 004_storage.sql
-- ============================================================================
