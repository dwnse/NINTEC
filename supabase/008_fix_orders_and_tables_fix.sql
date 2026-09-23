-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 008_fix_orders_and_tables_fix.sql — Permisos Completos de Tablas (Sin Error 42501)
--
-- INSTRUCCIONES:
-- 1. Abre tu panel de Supabase: https://supabase.com/dashboard/project/mcegdtcufzxpmezpncab
-- 2. Ve a "SQL Editor" en el menú lateral.
-- 3. Abre una "New Query", pega este script y haz clic en "RUN".
--
-- NOTA: Este script NO toca storage.objects (que causaba el error 42501).
-- Para Storage, simplemente crea los buckets desde el menú "Storage" de Supabase como "Public".
-- ============================================================================

-- ============================================================================
-- 1. PEDIDOS E HISTORIAL (orders)
-- Permite que los pedidos se guarden y consulten sin restricciones RLS
-- ============================================================================
ALTER TABLE public.orders ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "orders_all" ON public.orders;
DROP POLICY IF EXISTS "orders_select_own" ON public.orders;
DROP POLICY IF EXISTS "orders_insert_own" ON public.orders;
DROP POLICY IF EXISTS "orders_update_admin" ON public.orders;
CREATE POLICY "orders_all" ON public.orders FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 2. ITEMS DE PEDIDOS (order_items)
-- ============================================================================
ALTER TABLE public.order_items ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "order_items_all" ON public.order_items;
DROP POLICY IF EXISTS "order_items_select_own" ON public.order_items;
DROP POLICY IF EXISTS "order_items_insert_own" ON public.order_items;
CREATE POLICY "order_items_all" ON public.order_items FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 3. CATEGORÍAS (categories)
-- ============================================================================
ALTER TABLE public.categories ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "categories_all" ON public.categories;
DROP POLICY IF EXISTS "categories_select_all" ON public.categories;
DROP POLICY IF EXISTS "categories_insert_all" ON public.categories;
DROP POLICY IF EXISTS "categories_update_all" ON public.categories;
DROP POLICY IF EXISTS "categories_delete_all" ON public.categories;
CREATE POLICY "categories_all" ON public.categories FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 4. BANNERS PROMOCIONALES (banners)
-- ============================================================================
ALTER TABLE public.banners ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "banners_all" ON public.banners;
DROP POLICY IF EXISTS "banners_select_all" ON public.banners;
DROP POLICY IF EXISTS "banners_insert_all" ON public.banners;
DROP POLICY IF EXISTS "banners_update_all" ON public.banners;
DROP POLICY IF EXISTS "banners_delete_all" ON public.banners;
CREATE POLICY "banners_all" ON public.banners FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 5. SUCURSALES (branches)
-- ============================================================================
ALTER TABLE public.branches ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branches_all" ON public.branches;
DROP POLICY IF EXISTS "branches_select_all" ON public.branches;
DROP POLICY IF EXISTS "branches_insert_all" ON public.branches;
DROP POLICY IF EXISTS "branches_update_all" ON public.branches;
DROP POLICY IF EXISTS "branches_delete_all" ON public.branches;
CREATE POLICY "branches_all" ON public.branches FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 6. HORARIOS E INVENTARIO DE SUCURSAL
-- ============================================================================
ALTER TABLE public.branch_schedules ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branch_schedules_all" ON public.branch_schedules;
CREATE POLICY "branch_schedules_all" ON public.branch_schedules FOR ALL USING (TRUE) WITH CHECK (TRUE);

ALTER TABLE public.branch_inventory ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "branch_inventory_all" ON public.branch_inventory;
CREATE POLICY "branch_inventory_all" ON public.branch_inventory FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 7. IMÁGENES Y ESPECIFICACIONES DE PRODUCTO
-- ============================================================================
ALTER TABLE public.product_images ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "product_images_all" ON public.product_images;
CREATE POLICY "product_images_all" ON public.product_images FOR ALL USING (TRUE) WITH CHECK (TRUE);

ALTER TABLE public.product_specifications ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "product_specifications_all" ON public.product_specifications;
CREATE POLICY "product_specifications_all" ON public.product_specifications FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 8. CUPONES Y AJUSTES GLOBALES
-- ============================================================================
ALTER TABLE public.coupons ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "coupons_all" ON public.coupons;
CREATE POLICY "coupons_all" ON public.coupons FOR ALL USING (TRUE) WITH CHECK (TRUE);

ALTER TABLE public.app_settings ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "app_settings_all" ON public.app_settings;
CREATE POLICY "app_settings_all" ON public.app_settings FOR ALL USING (TRUE) WITH CHECK (TRUE);

-- ============================================================================
-- 9. NOTIFICAR RECARGA DE ESQUEMA A POSTGREST
-- ============================================================================
NOTIFY pgrst, 'reload schema';
