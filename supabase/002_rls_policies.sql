-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 002_rls_policies.sql — Políticas de Row Level Security
--
-- Ejecutar DESPUÉS de 001_schema.sql
-- ============================================================================

-- ============================================================================
-- FUNCIÓN HELPER: Verificar si el usuario actual es admin
-- ============================================================================
CREATE OR REPLACE FUNCTION public.is_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid()
        AND role IN ('admin', 'super_admin')
        AND is_active = TRUE
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER STABLE;

CREATE OR REPLACE FUNCTION public.is_super_admin()
RETURNS BOOLEAN AS $$
BEGIN
    RETURN EXISTS (
        SELECT 1 FROM public.profiles
        WHERE id = auth.uid()
        AND role = 'super_admin'
        AND is_active = TRUE
    );
END;
$$ LANGUAGE plpgsql SECURITY DEFINER STABLE;

-- ============================================================================
-- PROFILES
-- ============================================================================
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

-- Todos los autenticados pueden ver perfiles (para mostrar nombres en reseñas, etc.)
CREATE POLICY "profiles_select_own"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (id = auth.uid());

-- Admin puede ver todos los perfiles
CREATE POLICY "profiles_select_admin"
    ON public.profiles FOR SELECT
    TO authenticated
    USING (public.is_admin());

-- Cada usuario puede actualizar su propio perfil (excepto el rol)
CREATE POLICY "profiles_update_own"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (id = auth.uid())
    WITH CHECK (
        id = auth.uid()
        AND role = (SELECT role FROM public.profiles WHERE id = auth.uid())  -- No puede cambiar su propio rol
    );

-- Admin puede actualizar cualquier perfil
CREATE POLICY "profiles_update_admin"
    ON public.profiles FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- Insert se maneja via trigger, no necesita política para usuarios normales
CREATE POLICY "profiles_insert_trigger"
    ON public.profiles FOR INSERT
    TO authenticated
    WITH CHECK (id = auth.uid());

-- ============================================================================
-- CATEGORIES
-- ============================================================================
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;

-- Todos pueden leer categorías activas
CREATE POLICY "categories_select_public"
    ON public.categories FOR SELECT
    USING (is_active = TRUE);

-- Solo admins pueden crear/editar/eliminar categorías
CREATE POLICY "categories_insert_admin"
    ON public.categories FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "categories_update_admin"
    ON public.categories FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "categories_delete_admin"
    ON public.categories FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- PRODUCTS
-- ============================================================================
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

-- Todos pueden leer productos activos
CREATE POLICY "products_select_public"
    ON public.products FOR SELECT
    USING (is_active = TRUE);

-- ============================================================================
-- PRODUCT_IMAGES
-- ============================================================================
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;

-- Todos pueden ver imágenes de productos
CREATE POLICY "product_images_select_public"
    ON public.product_images FOR SELECT
    USING (TRUE);

-- Solo admins pueden gestionar imágenes
CREATE POLICY "product_images_insert_admin"
    ON public.product_images FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "product_images_update_admin"
    ON public.product_images FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "product_images_delete_admin"
    ON public.product_images FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- PRODUCT_SPECIFICATIONS
-- ============================================================================
ALTER TABLE public.product_specifications ENABLE ROW LEVEL SECURITY;

CREATE POLICY "product_specs_select_public"
    ON public.product_specifications FOR SELECT
    USING (TRUE);

CREATE POLICY "product_specs_insert_admin"
    ON public.product_specifications FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "product_specs_update_admin"
    ON public.product_specifications FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "product_specs_delete_admin"
    ON public.product_specifications FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- BRANCHES
-- ============================================================================
ALTER TABLE public.branches ENABLE ROW LEVEL SECURITY;

CREATE POLICY "branches_select_public"
    ON public.branches FOR SELECT
    USING (is_active = TRUE);

CREATE POLICY "branches_insert_admin"
    ON public.branches FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "branches_update_admin"
    ON public.branches FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "branches_delete_admin"
    ON public.branches FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- BRANCH_SCHEDULES
-- ============================================================================
ALTER TABLE public.branch_schedules ENABLE ROW LEVEL SECURITY;

CREATE POLICY "branch_schedules_select_public"
    ON public.branch_schedules FOR SELECT
    USING (TRUE);

CREATE POLICY "branch_schedules_insert_admin"
    ON public.branch_schedules FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "branch_schedules_update_admin"
    ON public.branch_schedules FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "branch_schedules_delete_admin"
    ON public.branch_schedules FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- BRANCH_INVENTORY
-- ============================================================================
ALTER TABLE public.branch_inventory ENABLE ROW LEVEL SECURITY;

-- Todos pueden ver inventario (para saber disponibilidad)
CREATE POLICY "branch_inventory_select_public"
    ON public.branch_inventory FOR SELECT
    USING (TRUE);

-- Solo admins gestionan inventario
CREATE POLICY "branch_inventory_insert_admin"
    ON public.branch_inventory FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "branch_inventory_update_admin"
    ON public.branch_inventory FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "branch_inventory_delete_admin"
    ON public.branch_inventory FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- ADDRESSES
-- ============================================================================
ALTER TABLE public.addresses ENABLE ROW LEVEL SECURITY;

-- Cada usuario solo ve y gestiona sus direcciones
CREATE POLICY "addresses_select_own"
    ON public.addresses FOR SELECT
    TO authenticated
    USING (user_id = auth.uid() OR public.is_admin());

CREATE POLICY "addresses_insert_own"
    ON public.addresses FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "addresses_update_own"
    ON public.addresses FOR UPDATE
    TO authenticated
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "addresses_delete_own"
    ON public.addresses FOR DELETE
    TO authenticated
    USING (user_id = auth.uid());

-- ============================================================================
-- ORDERS
-- ============================================================================
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;

-- Clientes ven sus propias órdenes; admins ven todas
CREATE POLICY "orders_select_own"
    ON public.orders FOR SELECT
    TO authenticated
    USING (user_id = auth.uid() OR public.is_admin());

-- Clientes pueden crear órdenes (solo las suyas)
CREATE POLICY "orders_insert_own"
    ON public.orders FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

-- Solo admins pueden actualizar órdenes (cambiar estado, agregar notas)
CREATE POLICY "orders_update_admin"
    ON public.orders FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

-- ============================================================================
-- ORDER_ITEMS
-- ============================================================================
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;

CREATE POLICY "order_items_select_own"
    ON public.order_items FOR SELECT
    TO authenticated
    USING (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_items.order_id
            AND (orders.user_id = auth.uid() OR public.is_admin())
        )
    );

CREATE POLICY "order_items_insert_own"
    ON public.order_items FOR INSERT
    TO authenticated
    WITH CHECK (
        EXISTS (
            SELECT 1 FROM public.orders
            WHERE orders.id = order_items.order_id
            AND orders.user_id = auth.uid()
        )
    );

-- ============================================================================
-- CART_ITEMS
-- ============================================================================
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;

-- Cada usuario gestiona solo su carrito
CREATE POLICY "cart_items_select_own"
    ON public.cart_items FOR SELECT
    TO authenticated
    USING (user_id = auth.uid());

CREATE POLICY "cart_items_insert_own"
    ON public.cart_items FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "cart_items_update_own"
    ON public.cart_items FOR UPDATE
    TO authenticated
    USING (user_id = auth.uid())
    WITH CHECK (user_id = auth.uid());

CREATE POLICY "cart_items_delete_own"
    ON public.cart_items FOR DELETE
    TO authenticated
    USING (user_id = auth.uid());

-- ============================================================================
-- APP_SETTINGS — Configuración global
-- ============================================================================
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;

-- Settings públicos son visibles para todos; los privados solo para admins
CREATE POLICY "app_settings_select_public"
    ON public.app_settings FOR SELECT
    TO authenticated
    USING (is_public = TRUE OR public.is_admin());

-- Solo admins pueden modificar
CREATE POLICY "app_settings_insert_admin"
    ON public.app_settings FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "app_settings_update_admin"
    ON public.app_settings FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "app_settings_delete_admin"
    ON public.app_settings FOR DELETE
    TO authenticated
    USING (public.is_super_admin());  -- Solo super admin puede eliminar settings

-- ============================================================================
-- BANNERS
-- ============================================================================
ALTER TABLE public.banners ENABLE ROW LEVEL SECURITY;

-- Todos ven banners activos y dentro de su rango de fechas
CREATE POLICY "banners_select_public"
    ON public.banners FOR SELECT
    TO authenticated
    USING (
        (is_active = TRUE
         AND (starts_at IS NULL OR starts_at <= NOW())
         AND (ends_at IS NULL OR ends_at >= NOW()))
        OR public.is_admin()
    );

CREATE POLICY "banners_insert_admin"
    ON public.banners FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "banners_update_admin"
    ON public.banners FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "banners_delete_admin"
    ON public.banners FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- PAYMENT_METHODS
-- ============================================================================
ALTER TABLE public.payment_methods ENABLE ROW LEVEL SECURITY;

CREATE POLICY "payment_methods_select_public"
    ON public.payment_methods FOR SELECT
    TO authenticated
    USING (is_active = TRUE OR public.is_admin());

CREATE POLICY "payment_methods_insert_admin"
    ON public.payment_methods FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "payment_methods_update_admin"
    ON public.payment_methods FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "payment_methods_delete_admin"
    ON public.payment_methods FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- PAGES (CMS)
-- ============================================================================
ALTER TABLE public.pages ENABLE ROW LEVEL SECURITY;

CREATE POLICY "pages_select_public"
    ON public.pages FOR SELECT
    TO authenticated
    USING (is_active = TRUE OR public.is_admin());

CREATE POLICY "pages_insert_admin"
    ON public.pages FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "pages_update_admin"
    ON public.pages FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "pages_delete_admin"
    ON public.pages FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- COUPONS
-- ============================================================================
ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;

-- Clientes pueden leer cupones activos (para validar en checkout)
CREATE POLICY "coupons_select_public"
    ON public.coupons FOR SELECT
    TO authenticated
    USING (
        (is_active = TRUE AND (ends_at IS NULL OR ends_at >= NOW()))
        OR public.is_admin()
    );

CREATE POLICY "coupons_insert_admin"
    ON public.coupons FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "coupons_update_admin"
    ON public.coupons FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "coupons_delete_admin"
    ON public.coupons FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- COUPON_USAGE
-- ============================================================================
ALTER TABLE public.coupon_usage ENABLE ROW LEVEL SECURITY;

CREATE POLICY "coupon_usage_select_own"
    ON public.coupon_usage FOR SELECT
    TO authenticated
    USING (user_id = auth.uid() OR public.is_admin());

CREATE POLICY "coupon_usage_insert_own"
    ON public.coupon_usage FOR INSERT
    TO authenticated
    WITH CHECK (user_id = auth.uid());

-- ============================================================================
-- FAQS
-- ============================================================================
ALTER TABLE public.faqs ENABLE ROW LEVEL SECURITY;

CREATE POLICY "faqs_select_public"
    ON public.faqs FOR SELECT
    TO authenticated
    USING (is_active = TRUE OR public.is_admin());

CREATE POLICY "faqs_insert_admin"
    ON public.faqs FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "faqs_update_admin"
    ON public.faqs FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "faqs_delete_admin"
    ON public.faqs FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- NOTIFICATIONS
-- ============================================================================
ALTER TABLE public.notifications ENABLE ROW LEVEL SECURITY;

-- Usuarios ven sus propias notificaciones y las generales
CREATE POLICY "notifications_select_own"
    ON public.notifications FOR SELECT
    TO authenticated
    USING (
        target_type = 'all'
        OR target_user_id = auth.uid()
        OR public.is_admin()
    );

-- Solo admins pueden crear notificaciones
CREATE POLICY "notifications_insert_admin"
    ON public.notifications FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

-- Usuarios pueden marcar como leída sus propias notificaciones
CREATE POLICY "notifications_update_own"
    ON public.notifications FOR UPDATE
    TO authenticated
    USING (target_user_id = auth.uid() OR target_type = 'all')
    WITH CHECK (TRUE);

-- Admins pueden eliminar notificaciones
CREATE POLICY "notifications_delete_admin"
    ON public.notifications FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- ACTIVITY_LOG
-- ============================================================================
ALTER TABLE public.activity_log ENABLE ROW LEVEL SECURITY;

-- Solo admins pueden ver y escribir en el log
CREATE POLICY "activity_log_select_admin"
    ON public.activity_log FOR SELECT
    TO authenticated
    USING (public.is_admin());

CREATE POLICY "activity_log_insert_admin"
    ON public.activity_log FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

-- ============================================================================
-- SHIPPING_ZONES
-- ============================================================================
ALTER TABLE public.shipping_zones ENABLE ROW LEVEL SECURITY;

CREATE POLICY "shipping_zones_select_public"
    ON public.shipping_zones FOR SELECT
    TO authenticated
    USING (is_active = TRUE OR public.is_admin());

CREATE POLICY "shipping_zones_insert_admin"
    ON public.shipping_zones FOR INSERT
    TO authenticated
    WITH CHECK (public.is_admin());

CREATE POLICY "shipping_zones_update_admin"
    ON public.shipping_zones FOR UPDATE
    TO authenticated
    USING (public.is_admin())
    WITH CHECK (public.is_admin());

CREATE POLICY "shipping_zones_delete_admin"
    ON public.shipping_zones FOR DELETE
    TO authenticated
    USING (public.is_admin());

-- ============================================================================
-- FIN DE 002_rls_policies.sql
-- Continuar con 003_functions.sql
-- ============================================================================
