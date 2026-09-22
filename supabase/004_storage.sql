-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 004_storage.sql — Configuración de Supabase Storage
--
-- Ejecutar DESPUÉS de 003_functions.sql
--
-- NOTA: Los buckets de Storage se crean desde el Dashboard de Supabase:
--   Settings → Storage → Create a new bucket
-- 
-- Este script configura las POLÍTICAS de acceso a los buckets.
-- Primero debes crear los buckets manualmente:
--   1. "product-images"  (público)
--   2. "avatars"         (público)
--   3. "banners"         (público)
--   4. "brand-assets"    (público) — Logos, favicon, etc.
--   5. "payment-proofs"  (privado) — Comprobantes de pago
--   6. "page-content"    (público) — Imágenes de páginas CMS
-- ============================================================================

-- ============================================================================
-- BUCKET: product-images
-- ============================================================================
-- Crear bucket (si se puede via SQL - depende de la versión de Supabase)
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'product-images',
    'product-images',
    TRUE,                              -- Público (lectura sin auth)
    5242880,                           -- 5MB máximo por archivo
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

-- Lectura pública
CREATE POLICY "product_images_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'product-images');

-- Solo admins pueden subir
CREATE POLICY "product_images_admin_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'product-images'
        AND public.is_admin()
    );

-- Solo admins pueden actualizar
CREATE POLICY "product_images_admin_update"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (
        bucket_id = 'product-images'
        AND public.is_admin()
    );

-- Solo admins pueden eliminar
CREATE POLICY "product_images_admin_delete"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (
        bucket_id = 'product-images'
        AND public.is_admin()
    );

-- ============================================================================
-- BUCKET: avatars
-- ============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'avatars',
    'avatars',
    TRUE,
    2097152,                           -- 2MB máximo
    ARRAY['image/jpeg', 'image/png', 'image/webp']
) ON CONFLICT (id) DO NOTHING;

-- Lectura pública
CREATE POLICY "avatars_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'avatars');

-- Cada usuario sube su propio avatar (carpeta = su user id)
CREATE POLICY "avatars_user_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::TEXT
    );

-- Cada usuario actualiza su propio avatar
CREATE POLICY "avatars_user_update"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::TEXT
    );

-- Cada usuario elimina su propio avatar
CREATE POLICY "avatars_user_delete"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (
        bucket_id = 'avatars'
        AND (storage.foldername(name))[1] = auth.uid()::TEXT
    );

-- ============================================================================
-- BUCKET: banners
-- ============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'banners',
    'banners',
    TRUE,
    10485760,                          -- 10MB máximo (banners grandes)
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

-- Lectura pública
CREATE POLICY "banners_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'banners');

-- Solo admins gestionan banners
CREATE POLICY "banners_admin_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'banners' AND public.is_admin());

CREATE POLICY "banners_admin_update"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (bucket_id = 'banners' AND public.is_admin());

CREATE POLICY "banners_admin_delete"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'banners' AND public.is_admin());

-- ============================================================================
-- BUCKET: brand-assets (Logo, favicon, imágenes de marca)
-- ============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'brand-assets',
    'brand-assets',
    TRUE,
    5242880,
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/svg+xml', 'image/x-icon']
) ON CONFLICT (id) DO NOTHING;

CREATE POLICY "brand_assets_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'brand-assets');

CREATE POLICY "brand_assets_admin_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'brand-assets' AND public.is_admin());

CREATE POLICY "brand_assets_admin_update"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (bucket_id = 'brand-assets' AND public.is_admin());

CREATE POLICY "brand_assets_admin_delete"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'brand-assets' AND public.is_admin());

-- ============================================================================
-- BUCKET: payment-proofs (Comprobantes de pago — PRIVADO)
-- ============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'payment-proofs',
    'payment-proofs',
    FALSE,                             -- PRIVADO
    10485760,
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'application/pdf']
) ON CONFLICT (id) DO NOTHING;

-- Usuarios suben comprobantes en su carpeta
CREATE POLICY "payment_proofs_user_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (
        bucket_id = 'payment-proofs'
        AND (storage.foldername(name))[1] = auth.uid()::TEXT
    );

-- Usuarios ven sus propios comprobantes
CREATE POLICY "payment_proofs_user_read"
    ON storage.objects FOR SELECT
    TO authenticated
    USING (
        bucket_id = 'payment-proofs'
        AND (
            (storage.foldername(name))[1] = auth.uid()::TEXT
            OR public.is_admin()
        )
    );

-- Admins pueden ver todos los comprobantes (ya cubierto arriba con is_admin())

-- ============================================================================
-- BUCKET: page-content (Imágenes para páginas CMS)
-- ============================================================================
INSERT INTO storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
VALUES (
    'page-content',
    'page-content',
    TRUE,
    5242880,
    ARRAY['image/jpeg', 'image/png', 'image/webp', 'image/gif']
) ON CONFLICT (id) DO NOTHING;

CREATE POLICY "page_content_public_read"
    ON storage.objects FOR SELECT
    USING (bucket_id = 'page-content');

CREATE POLICY "page_content_admin_upload"
    ON storage.objects FOR INSERT
    TO authenticated
    WITH CHECK (bucket_id = 'page-content' AND public.is_admin());

CREATE POLICY "page_content_admin_update"
    ON storage.objects FOR UPDATE
    TO authenticated
    USING (bucket_id = 'page-content' AND public.is_admin());

CREATE POLICY "page_content_admin_delete"
    ON storage.objects FOR DELETE
    TO authenticated
    USING (bucket_id = 'page-content' AND public.is_admin());

-- ============================================================================
-- FIN DE 004_storage.sql
-- Continuar con 005_seed_data.sql
-- ============================================================================
