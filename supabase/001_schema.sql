-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 001_schema.sql — Esquema principal
-- 
-- Ejecutar en: Supabase Dashboard → SQL Editor → New Query
-- Orden de ejecución: 001 → 002 → 003 → 004 → 005
-- ============================================================================

-- ============================================================================
-- EXTENSIONES
-- ============================================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ============================================================================
-- 1. PROFILES — Extensión de auth.users
-- ============================================================================
-- Se crea automáticamente al registrar usuario via trigger (ver 003_functions.sql)
CREATE TABLE IF NOT EXISTS public.profiles (
    id            UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name     TEXT NOT NULL DEFAULT '',
    username      TEXT UNIQUE,
    email         TEXT NOT NULL DEFAULT '',
    phone         TEXT,
    avatar_url    TEXT,
    role          TEXT NOT NULL DEFAULT 'customer' CHECK (role IN ('customer', 'admin', 'super_admin')),
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.profiles IS 'Perfiles de usuario extendidos. Se crea automáticamente al registrar en auth.users.';
COMMENT ON COLUMN public.profiles.role IS 'customer = cliente app móvil, admin = panel administrativo, super_admin = acceso total';

-- ============================================================================
-- 2. CATEGORIES — Categorías de productos
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.categories (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    slug        TEXT NOT NULL UNIQUE,
    description TEXT,
    icon_name   TEXT,                -- Nombre del icono (ej: 'ic_laptop', 'ic_phone_android')
    image_url   TEXT,                -- URL de imagen de categoría (para panel admin/web)
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.categories IS 'Categorías de productos gestionables desde el panel admin.';

-- ============================================================================
-- 3. PRODUCTS — Catálogo de productos
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.products (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id   UUID NOT NULL REFERENCES public.categories(id) ON DELETE RESTRICT,
    name          TEXT NOT NULL,
    slug          TEXT NOT NULL UNIQUE,
    description   TEXT,
    price         DECIMAL(12,2) NOT NULL CHECK (price >= 0),
    old_price     DECIMAL(12,2) CHECK (old_price IS NULL OR old_price >= 0),
    is_new        BOOLEAN NOT NULL DEFAULT FALSE,
    is_featured   BOOLEAN NOT NULL DEFAULT FALSE,  -- Para mostrar en home / ofertas
    is_active     BOOLEAN NOT NULL DEFAULT TRUE,
    sku           TEXT UNIQUE,                      -- Código SKU del producto
    brand         TEXT,                             -- Marca del producto
    weight_kg     DECIMAL(8,3),                     -- Peso para cálculos de envío
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.products IS 'Catálogo maestro de productos. Los precios están en Bolivianos (Bs).';
COMMENT ON COLUMN public.products.old_price IS 'Precio anterior para mostrar descuento. NULL si no tiene descuento.';

CREATE INDEX idx_products_category ON public.products(category_id);
CREATE INDEX idx_products_is_active ON public.products(is_active);
CREATE INDEX idx_products_is_featured ON public.products(is_featured);
CREATE INDEX idx_products_slug ON public.products(slug);

-- ============================================================================
-- 4. PRODUCT_IMAGES — Imágenes de productos (múltiples por producto)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.product_images (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id  UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    image_url   TEXT NOT NULL,
    alt_text    TEXT,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_primary  BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.product_images IS 'Galería de imágenes por producto. Almacenadas en Supabase Storage.';

CREATE INDEX idx_product_images_product ON public.product_images(product_id);

-- ============================================================================
-- 5. PRODUCT_SPECIFICATIONS — Especificaciones técnicas clave-valor
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.product_specifications (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    product_id  UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    spec_key    TEXT NOT NULL,       -- Ej: "Procesador", "Memoria RAM"
    spec_value  TEXT NOT NULL,       -- Ej: "Apple M3 Max 14-core", "36 GB"
    sort_order  INTEGER NOT NULL DEFAULT 0
);

COMMENT ON TABLE public.product_specifications IS 'Especificaciones técnicas de producto en formato clave-valor.';

CREATE INDEX idx_product_specs_product ON public.product_specifications(product_id);

-- ============================================================================
-- 6. BRANCHES — Sucursales
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.branches (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name        TEXT NOT NULL,
    address     TEXT NOT NULL,
    city        TEXT NOT NULL DEFAULT 'La Paz',
    latitude    DOUBLE PRECISION NOT NULL,
    longitude   DOUBLE PRECISION NOT NULL,
    phone       TEXT,
    email       TEXT,
    image_url   TEXT,                -- Foto de la sucursal
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.branches IS 'Sucursales de NINTEC con geolocalización.';

-- ============================================================================
-- 7. BRANCH_SCHEDULES — Horarios por día de la semana
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.branch_schedules (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id   UUID NOT NULL REFERENCES public.branches(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week BETWEEN 0 AND 6), -- 0=Domingo, 6=Sábado
    open_time   TIME NOT NULL,
    close_time  TIME NOT NULL,
    is_closed   BOOLEAN NOT NULL DEFAULT FALSE, -- TRUE = cerrado ese día

    UNIQUE(branch_id, day_of_week)
);

COMMENT ON TABLE public.branch_schedules IS 'Horarios de apertura por día de la semana. 0=Domingo, 1=Lunes, ..., 6=Sábado.';

CREATE INDEX idx_branch_schedules_branch ON public.branch_schedules(branch_id);

-- ============================================================================
-- 8. BRANCH_INVENTORY — Inventario por sucursal
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.branch_inventory (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    branch_id   UUID NOT NULL REFERENCES public.branches(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    stock       INTEGER NOT NULL DEFAULT 0 CHECK (stock >= 0),
    min_stock   INTEGER NOT NULL DEFAULT 0,  -- Alerta de stock bajo
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(branch_id, product_id)
);

COMMENT ON TABLE public.branch_inventory IS 'Stock de cada producto por sucursal. Permite gestión de inventario diferenciado.';

CREATE INDEX idx_branch_inventory_branch ON public.branch_inventory(branch_id);
CREATE INDEX idx_branch_inventory_product ON public.branch_inventory(product_id);
CREATE INDEX idx_branch_inventory_low_stock ON public.branch_inventory(stock) WHERE stock <= 0;

-- ============================================================================
-- 9. ADDRESSES — Direcciones de envío del usuario
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.addresses (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id       UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    label         TEXT NOT NULL DEFAULT 'Casa',  -- 'Casa', 'Oficina', 'Otro'
    address_line  TEXT NOT NULL,
    city          TEXT NOT NULL DEFAULT 'La Paz',
    zone          TEXT,                          -- Zona/barrio
    reference     TEXT,                          -- Referencia para el repartidor
    latitude      DOUBLE PRECISION,
    longitude     DOUBLE PRECISION,
    is_default    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.addresses IS 'Direcciones de envío guardadas por el usuario.';

CREATE INDEX idx_addresses_user ON public.addresses(user_id);

-- ============================================================================
-- 10. ORDERS — Órdenes de compra
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.orders (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id         UUID NOT NULL REFERENCES public.profiles(id) ON DELETE RESTRICT,
    order_number    TEXT NOT NULL UNIQUE,          -- Formato: NIN-0001, NIN-0002, etc.
    status          TEXT NOT NULL DEFAULT 'pending'
                    CHECK (status IN ('pending', 'confirmed', 'processing', 'shipped', 'delivered', 'completed', 'cancelled', 'rejected')),
    payment_method  TEXT NOT NULL,
    payment_status  TEXT NOT NULL DEFAULT 'pending'
                    CHECK (payment_status IN ('pending', 'paid', 'failed', 'refunded')),
    subtotal        DECIMAL(12,2) NOT NULL CHECK (subtotal >= 0),
    discount        DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (discount >= 0),
    tax             DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (tax >= 0),
    shipping_cost   DECIMAL(12,2) NOT NULL DEFAULT 0 CHECK (shipping_cost >= 0),
    total           DECIMAL(12,2) NOT NULL CHECK (total >= 0),
    branch_id       UUID REFERENCES public.branches(id),     -- Sucursal de recogida (NULL si envío)
    address_id      UUID REFERENCES public.addresses(id),    -- Dirección de envío (NULL si recogida)
    coupon_id       UUID,                                     -- FK se agrega después de crear coupons
    notes           TEXT,                                     -- Notas del cliente
    admin_notes     TEXT,                                     -- Notas internas del admin
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.orders IS 'Órdenes de compra. El admin puede cambiar status y agregar notas internas.';
COMMENT ON COLUMN public.orders.status IS 'pending → confirmed → processing → shipped → delivered → completed. También: cancelled, rejected.';

CREATE INDEX idx_orders_user ON public.orders(user_id);
CREATE INDEX idx_orders_status ON public.orders(status);
CREATE INDEX idx_orders_created ON public.orders(created_at DESC);
CREATE INDEX idx_orders_number ON public.orders(order_number);

-- ============================================================================
-- 11. ORDER_ITEMS — Items de una orden (snapshot desnormalizado)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.order_items (
    id            UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id      UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    product_id    UUID REFERENCES public.products(id) ON DELETE SET NULL,  -- SET NULL si producto se elimina
    product_name  TEXT NOT NULL,       -- Snapshot: nombre al momento de la compra
    product_image TEXT,                -- Snapshot: URL de imagen al momento de la compra
    unit_price    DECIMAL(12,2) NOT NULL,
    quantity      INTEGER NOT NULL CHECK (quantity > 0),
    line_total    DECIMAL(12,2) NOT NULL
);

COMMENT ON TABLE public.order_items IS 'Items de orden con datos desnormalizados para preservar historial.';

CREATE INDEX idx_order_items_order ON public.order_items(order_id);
CREATE INDEX idx_order_items_product ON public.order_items(product_id);

-- ============================================================================
-- 12. CART_ITEMS — Carrito persistente del usuario (server-side)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.cart_items (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    product_id  UUID NOT NULL REFERENCES public.products(id) ON DELETE CASCADE,
    quantity    INTEGER NOT NULL DEFAULT 1 CHECK (quantity > 0),
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(user_id, product_id)  -- Un usuario no puede tener el mismo producto dos veces
);

COMMENT ON TABLE public.cart_items IS 'Carrito persistente server-side. Se sincroniza con la app móvil.';

CREATE INDEX idx_cart_items_user ON public.cart_items(user_id);

-- ============================================================================
-- ======================== TABLAS DE CONFIGURACIÓN ADMIN =====================
-- ============================================================================

-- ============================================================================
-- 13. APP_SETTINGS — Configuración global de la app (clave-valor)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.app_settings (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    setting_key TEXT NOT NULL UNIQUE,
    value       TEXT NOT NULL,
    value_type  TEXT NOT NULL DEFAULT 'string' CHECK (value_type IN ('string', 'number', 'boolean', 'json', 'color', 'url')),
    category    TEXT NOT NULL DEFAULT 'general',  -- Agrupación en el panel admin
    label       TEXT NOT NULL,                    -- Etiqueta legible para el panel admin
    description TEXT,                             -- Descripción para el admin
    is_public   BOOLEAN NOT NULL DEFAULT TRUE,    -- Si es visible para la app móvil
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by  UUID REFERENCES public.profiles(id)
);

COMMENT ON TABLE public.app_settings IS 'Configuración global de la app. El admin puede modificar nombre, colores, logos, contacto, etc.';

-- ============================================================================
-- 14. BANNERS — Banners promocionales (carrusel del home)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.banners (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    subtitle        TEXT,
    image_url       TEXT NOT NULL,
    action_type     TEXT CHECK (action_type IN ('product', 'category', 'url', 'none')),
    action_value    TEXT,              -- ID del producto, slug de categoría, o URL según action_type
    background_color TEXT,             -- Color de fondo si no hay imagen
    text_color      TEXT DEFAULT '#FFFFFF',
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at       TIMESTAMPTZ,       -- Programar activación
    ends_at         TIMESTAMPTZ,       -- Programar desactivación
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.banners IS 'Banners del carrusel de la página principal. El admin programa y gestiona promociones.';

CREATE INDEX idx_banners_active ON public.banners(is_active, sort_order);

-- ============================================================================
-- 15. PAYMENT_METHODS — Métodos de pago configurables
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.payment_methods (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            TEXT NOT NULL,       -- 'Visa / Mastercard', 'PayPal', 'QR', 'Transferencia'
    code            TEXT NOT NULL UNIQUE, -- 'visa_mc', 'paypal', 'qr', 'transfer'
    description     TEXT,
    icon_name       TEXT,                -- Nombre del icono para la app
    image_url       TEXT,                -- Logo del método de pago
    instructions    TEXT,                -- Instrucciones para el usuario (ej: datos QR)
    sort_order      INTEGER NOT NULL DEFAULT 0,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    requires_proof  BOOLEAN NOT NULL DEFAULT FALSE,  -- Si requiere comprobante de pago
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.payment_methods IS 'Métodos de pago disponibles. El admin puede activar/desactivar y configurar cada método.';

-- ============================================================================
-- 16. PAGES — Páginas de contenido (CMS básico)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.pages (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title       TEXT NOT NULL,
    slug        TEXT NOT NULL UNIQUE,      -- 'about-us', 'terms', 'privacy', 'returns'
    content     TEXT NOT NULL DEFAULT '',   -- Contenido en HTML o Markdown
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order  INTEGER NOT NULL DEFAULT 0,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_by  UUID REFERENCES public.profiles(id)
);

COMMENT ON TABLE public.pages IS 'Páginas de contenido estático editables por el admin. Ej: Términos, Políticas, Sobre nosotros.';

-- ============================================================================
-- 17. COUPONS — Cupones de descuento
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.coupons (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    code                TEXT NOT NULL UNIQUE,            -- Código del cupón (ej: 'NINTEC10')
    description         TEXT,
    discount_type       TEXT NOT NULL CHECK (discount_type IN ('percentage', 'fixed')),
    discount_value      DECIMAL(12,2) NOT NULL CHECK (discount_value > 0),
    min_purchase        DECIMAL(12,2) DEFAULT 0,         -- Compra mínima para aplicar
    max_discount        DECIMAL(12,2),                   -- Tope máximo de descuento (para porcentaje)
    max_uses            INTEGER,                         -- Usos totales permitidos (NULL = ilimitado)
    used_count          INTEGER NOT NULL DEFAULT 0,
    max_uses_per_user   INTEGER DEFAULT 1,               -- Usos por usuario
    is_active           BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at           TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ends_at             TIMESTAMPTZ,                     -- NULL = sin expiración
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.coupons IS 'Cupones de descuento gestionados desde el panel admin.';

CREATE INDEX idx_coupons_code ON public.coupons(code);
CREATE INDEX idx_coupons_active ON public.coupons(is_active);

-- Agregar FK de orders.coupon_id ahora que la tabla coupons existe
ALTER TABLE public.orders
    ADD CONSTRAINT fk_orders_coupon
    FOREIGN KEY (coupon_id) REFERENCES public.coupons(id) ON DELETE SET NULL;

-- ============================================================================
-- 18. COUPON_USAGE — Registro de uso de cupones por usuario
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.coupon_usage (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    coupon_id   UUID NOT NULL REFERENCES public.coupons(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES public.profiles(id) ON DELETE CASCADE,
    order_id    UUID NOT NULL REFERENCES public.orders(id) ON DELETE CASCADE,
    used_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),

    UNIQUE(coupon_id, user_id, order_id)
);

CREATE INDEX idx_coupon_usage_coupon ON public.coupon_usage(coupon_id);
CREATE INDEX idx_coupon_usage_user ON public.coupon_usage(user_id);

-- ============================================================================
-- 19. FAQS — Preguntas frecuentes
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.faqs (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    question    TEXT NOT NULL,
    answer      TEXT NOT NULL,
    category    TEXT DEFAULT 'general',    -- 'general', 'envios', 'pagos', 'productos', 'devoluciones'
    sort_order  INTEGER NOT NULL DEFAULT 0,
    is_active   BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.faqs IS 'Preguntas frecuentes editables desde el panel admin.';

-- ============================================================================
-- 20. NOTIFICATIONS — Notificaciones / Anuncios del admin
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.notifications (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    title           TEXT NOT NULL,
    message         TEXT NOT NULL,
    notification_type TEXT NOT NULL DEFAULT 'general'
                    CHECK (notification_type IN ('general', 'promotion', 'order_update', 'system')),
    target_type     TEXT NOT NULL DEFAULT 'all'
                    CHECK (target_type IN ('all', 'specific_user')),
    target_user_id  UUID REFERENCES public.profiles(id) ON DELETE CASCADE,  -- NULL si es para todos
    action_type     TEXT CHECK (action_type IN ('product', 'category', 'order', 'url', 'none')),
    action_value    TEXT,
    is_read         BOOLEAN NOT NULL DEFAULT FALSE,
    sent_at         TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.notifications IS 'Notificaciones enviadas a usuarios. El admin puede enviar anuncios generales o por usuario.';

CREATE INDEX idx_notifications_target_user ON public.notifications(target_user_id);
CREATE INDEX idx_notifications_type ON public.notifications(notification_type);
CREATE INDEX idx_notifications_unread ON public.notifications(is_read) WHERE is_read = FALSE;

-- ============================================================================
-- 21. ACTIVITY_LOG — Registro de actividad del admin (auditoría)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.activity_log (
    id          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id     UUID REFERENCES public.profiles(id) ON DELETE SET NULL,
    action      TEXT NOT NULL,        -- 'create', 'update', 'delete', 'login', 'status_change'
    entity_type TEXT NOT NULL,        -- 'product', 'order', 'user', 'branch', 'setting', etc.
    entity_id   TEXT,                 -- ID de la entidad afectada
    details     JSONB,               -- Detalles del cambio (old_value, new_value)
    ip_address  TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.activity_log IS 'Log de auditoría de todas las acciones administrativas.';

CREATE INDEX idx_activity_log_user ON public.activity_log(user_id);
CREATE INDEX idx_activity_log_entity ON public.activity_log(entity_type, entity_id);
CREATE INDEX idx_activity_log_created ON public.activity_log(created_at DESC);

-- ============================================================================
-- 22. SHIPPING_ZONES — Zonas de envío con costos (configurables por admin)
-- ============================================================================
CREATE TABLE IF NOT EXISTS public.shipping_zones (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            TEXT NOT NULL,         -- 'Centro', 'Zona Sur', 'El Alto', 'Interior'
    city            TEXT NOT NULL DEFAULT 'La Paz',
    shipping_cost   DECIMAL(12,2) NOT NULL DEFAULT 0,
    estimated_days  INTEGER NOT NULL DEFAULT 1,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE public.shipping_zones IS 'Zonas de envío con costos configurables por el admin.';

-- ============================================================================
-- UPDATED_AT TRIGGER HELPER
-- ============================================================================
CREATE OR REPLACE FUNCTION public.set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Aplicar trigger a todas las tablas con updated_at
DO $$
DECLARE
    tbl TEXT;
BEGIN
    FOR tbl IN
        SELECT unnest(ARRAY[
            'profiles', 'categories', 'products', 'orders', 'cart_items',
            'branches', 'branch_inventory', 'app_settings', 'banners',
            'payment_methods', 'pages', 'coupons', 'faqs'
        ])
    LOOP
        EXECUTE format(
            'CREATE TRIGGER set_%I_updated_at
             BEFORE UPDATE ON public.%I
             FOR EACH ROW EXECUTE FUNCTION public.set_updated_at()',
            tbl, tbl
        );
    END LOOP;
END;
$$;

-- ============================================================================
-- FIN DE 001_schema.sql
-- Continuar con 002_rls_policies.sql
-- ============================================================================
