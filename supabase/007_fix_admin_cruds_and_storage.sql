-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 007_fix_admin_cruds_and_storage.sql — Solución Definitiva de CRUDs y Storage
--
-- INSTRUCCIONES:
-- 1. Abre tu panel de Supabase: https://supabase.com/dashboard/project/mcegdtcufzxpmezpncab
-- 2. Ve a "SQL Editor" en el menú lateral.
-- 3. Abre una "New Query", pega TODO este script y haz clic en "RUN".
-- ============================================================================

-- ============================================================================
-- 1. HABILITAR PERMISOS COMPLETOS DE CRUD EN TABLAS DEL PANEL DE ADMINISTRACIÓN
-- ============================================================================

-- CATEGORÍAS (categories)
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "categories_select_all" ON public.categories;
DROP POLICY IF EXISTS "categories_insert_all" ON public.categories;
DROP POLICY IF EXISTS "categories_update_all" ON public.categories;
DROP POLICY IF EXISTS "categories_delete_all" ON public.categories;
CREATE POLICY "categories_select_all" ON public.categories FOR SELECT USING (TRUE);
CREATE POLICY "categories_insert_all" ON public.categories FOR INSERT WITH CHECK (TRUE);
CREATE POLICY "categories_update_all" ON public.categories FOR UPDATE USING (TRUE) WITH CHECK (TRUE);
CREATE POLICY "categories_delete_all" ON public.categories FOR DELETE USING (TRUE);

-- BANNERS PROMOCIONALES (banners)
ALTER TABLE public.banners ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "banners_select_all" ON public.banners;
DROP POLICY IF EXISTS "banners_insert_all" ON public.banners;
DROP POLICY IF EXISTS "banners_update_all" ON public.banners;
DROP POLICY IF EXISTS "banners_delete_all" ON public.banners;
CREATE POLICY "banners_select_all" ON public.banners FOR SELECT USING (TRUE);
CREATE POLICY "banners_insert_all" ON public.banners FOR INSERT WITH CHECK (TRUE);
CREATE POLICY "banners_update_all" ON public.banners FOR UPDATE USING (TRUE) WITH CHECK (TRUE);
CREATE POLICY "banners_delete_all" ON public.banners FOR DELETE USING (TRUE);

-- SUCURSALES (branches)
ALTER TABLE public.branches ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branches_select_all" ON public.branches;
DROP POLICY IF EXISTS "branches_insert_all" ON public.branches;
DROP POLICY IF EXISTS "branches_update_all" ON public.branches;
DROP POLICY IF EXISTS "branches_delete_all" ON public.branches;
CREATE POLICY "branches_select_all" ON public.branches FOR SELECT USING (TRUE);
CREATE POLICY "branches_insert_all" ON public.branches FOR INSERT WITH CHECK (TRUE);
CREATE POLICY "branches_update_all" ON public.branches FOR UPDATE USING (TRUE) WITH CHECK (TRUE);
CREATE POLICY "branches_delete_all" ON public.branches FOR DELETE USING (TRUE);

-- HORARIOS DE SUCURSAL (branch_schedules)
ALTER TABLE public.branch_schedules ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branch_schedules_all" ON public.branch_schedules;
CREATE POLICY "branch_schedules_all" ON public.branch_schedules FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- INVENTARIO POR SUCURSAL (branch_inventory)
ALTER TABLE public.branch_inventory ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branch_inventory_all" ON public.branch_inventory;
CREATE POLICY "branch_inventory_all" ON public.branch_inventory FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- IMÁGENES DE PRODUCTO (product_images)
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "product_images_all" ON public.product_images;
CREATE POLICY "product_images_all" ON public.product_images FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ESPECIFICACIONES DE PRODUCTO (product_specifications)
ALTER TABLE public.product_specifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "product_specifications_all" ON public.product_specifications;
CREATE POLICY "product_specifications_all" ON public.product_specifications FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- CUPONES Y OFERTAS (coupons)
ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "coupons_all" ON public.coupons;
CREATE POLICY "coupons_all" ON public.coupons FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- PEDIDOS E HISTORIAL (orders)
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "orders_all" ON public.orders;
CREATE POLICY "orders_all" ON public.orders FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ITEMS DE PEDIDOS (order_items)
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "order_items_all" ON public.order_items;
CREATE POLICY "order_items_all" ON public.order_items FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- AJUSTES GLOBALES (app_settings)
ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "app_settings_all" ON public.app_settings;
CREATE POLICY "app_settings_all" ON public.app_settings FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 2. CREAR Y CONFIGURAR BUCKETS DE SUPABASE STORAGE
-- ============================================================================

-- Crear / asegurar buckets públicos con 20MB de límite
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES 
    ('product-images', 'product-images', TRUE, 20971520, ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif', 'image/svg+xml']),
    ('banners', 'banners', TRUE, 20971520, ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']),
    ('categories', 'categories', TRUE, 20971520, ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/svg+xml']),
    ('avatars', 'avatars', TRUE, 10485760, ARRAY['image/jpeg', 'image/png', 'image/webp']),
    ('brand-assets', 'brand-assets', TRUE, 20971520, ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/svg+xml', 'image/x-icon'])
ON CONFLICT (id) DO UPDATE SET 
    public = TRUE, 
    file_size_limit = 20971520;

-- Limpiar políticas previas en storage.objects para evitar colisiones
DROP POLICY IF EXISTS "nintec_storage_public_read" ON storage.objects;
DROP POLICY IF EXISTS "nintec_storage_public_insert" ON storage.objects;
DROP POLICY IF EXISTS "nintec_storage_public_update" ON storage.objects;
DROP POLICY IF EXISTS "nintec_storage_public_delete" ON storage.objects;

-- Limpiar políticas restrictivas heredadas
DROP POLICY IF EXISTS "product_images_public_read" ON storage.objects;
DROP POLICY IF EXISTS "product_images_admin_upload" ON storage.objects;
DROP POLICY IF EXISTS "product_images_admin_update" ON storage.objects;
DROP POLICY IF EXISTS "product_images_admin_delete" ON storage.objects;
DROP POLICY IF EXISTS "banners_public_read" ON storage.objects;
DROP POLICY IF EXISTS "banners_admin_upload" ON storage.objects;
DROP POLICY IF EXISTS "banners_admin_update" ON storage.objects;
DROP POLICY IF EXISTS "banners_admin_delete" ON storage.objects;

-- Crear políticas universales y abiertas para los buckets de NINTEC
CREATE POLICY "nintec_storage_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id IN ('product-images', 'banners', 'categories', 'avatars', 'brand-assets'));

CREATE POLICY "nintec_storage_public_insert"
    ON storage.objects FOR INSERT
    WITH CHECK (bucket_id IN ('product-images', 'banners', 'categories', 'avatars', 'brand-assets'));

CREATE POLICY "nintec_storage_public_update"
    ON storage.objects FOR UPDATE
    USING (bucket_id IN ('product-images', 'banners', 'categories', 'avatars', 'brand-assets'))
    WITH CHECK (bucket_id IN ('product-images', 'banners', 'categories', 'avatars', 'brand-assets'));

CREATE POLICY "nintec_storage_public_delete"
    ON storage.objects FOR DELETE
    USING (bucket_id IN ('product-images', 'banners', 'categories', 'avatars', 'brand-assets'));

-- ============================================================================
-- 3. RECARGAR EL MOTOR POSTGREST DE SUPABASE
-- ============================================================================
NOTIFY pgrst, 'reload schema';
