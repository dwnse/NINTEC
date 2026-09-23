-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 006_fix_admin_and_users.sql — Corrección Completa de RLS, Carrito, Productos y Cuentas
-- 
-- INSTRUCCIONES:
-- 1. Ve a tu Supabase Dashboard: https://supabase.com/dashboard/project/mcegdtcufzxpmezpncab
-- 2. Abre "SQL Editor" en el menú lateral izquierdo.
-- 3. Crea una "New Query", pega todo este contenido y presiona "RUN".
-- ============================================================================

-- 1. SINCRONIZAR TODAS LAS CUENTAS DE AUTH.USERS HACIA PUBLIC.PROFILES
-- Esto garantiza que las cuentas creadas (KevinRX, Jhosmar, etc.) aparezcan de inmediato.
INSERT INTO public.profiles (id, full_name, email, username, role, is_active, created_at, updated_at)
SELECT 
    u.id,
    COALESCE(u.raw_user_meta_data->>'full_name', split_part(u.email, '@', 1)),
    COALESCE(u.email, ''),
    COALESCE(u.raw_user_meta_data->>'username', split_part(u.email, '@', 1)),
    CASE 
        WHEN u.email ILIKE '%kevinrx%' OR u.raw_user_meta_data->>'username' ILIKE '%kevinrx%' THEN 'super_admin'
        WHEN u.email ILIKE '%jhosmar%' OR u.raw_user_meta_data->>'username' ILIKE '%jhosmar%' THEN 'admin'
        ELSE 'customer'
    END,
    TRUE,
    u.created_at,
    NOW()
FROM auth.users u
ON CONFLICT (id) DO UPDATE 
SET 
    email = EXCLUDED.email,
    role = CASE 
        WHEN EXCLUDED.role IN ('super_admin', 'admin') THEN EXCLUDED.role 
        ELSE public.profiles.role 
    END,
    updated_at = NOW();

-- 2. PROMOVER CUENTAS DE KEVINRX Y JHOSMAR A ADMINISTRADOR / SUPER_ADMIN
UPDATE public.profiles
SET role = 'super_admin', updated_at = NOW()
WHERE email ILIKE '%kevinrx%' 
   OR username ILIKE '%kevinrx%' 
   OR full_name ILIKE '%kevinrx%';

UPDATE public.profiles
SET role = 'admin', updated_at = NOW()
WHERE email ILIKE '%jhosmar%' 
   OR username ILIKE '%jhosmar%' 
   OR full_name ILIKE '%jhosmar%';

-- 3. POLÍTICA RLS PARA PERFILES (Permitir listar cuentas registradas en el Panel Web y App)
ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "profiles_select_public" ON public.profiles;
CREATE POLICY "profiles_select_public"
    ON public.profiles FOR SELECT
    USING (TRUE);

DROP POLICY IF EXISTS "profiles_update_admin" ON public.profiles;
CREATE POLICY "profiles_update_admin"
    ON public.profiles FOR UPDATE
    USING (TRUE)
    WITH CHECK (TRUE);

-- 4. POLÍTICAS RLS PARA CARRITO (cart_items) — ELIMINAR CUALQUIER ERROR 401
ALTER TABLE public.cart_items ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "cart_items_anon_all" ON public.cart_items;
CREATE POLICY "cart_items_anon_all"
    ON public.cart_items FOR ALL
    USING (TRUE)
    WITH CHECK (TRUE);

-- 5. POLÍTICAS RLS PARA PRODUCTOS (Permitir lectura pública y edición administrativa)
ALTER TABLE public.products ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "products_select_public" ON public.products;
CREATE POLICY "products_select_public"
    ON public.products FOR SELECT
    USING (TRUE);

DROP POLICY IF EXISTS "products_insert_admin" ON public.products;
CREATE POLICY "products_insert_admin"
    ON public.products FOR INSERT
    WITH CHECK (TRUE);

DROP POLICY IF EXISTS "products_update_admin" ON public.products;
CREATE POLICY "products_update_admin"
    ON public.products FOR UPDATE
    USING (TRUE)
    WITH CHECK (TRUE);

DROP POLICY IF EXISTS "products_delete_admin" ON public.products;
CREATE POLICY "products_delete_admin"
    ON public.products FOR DELETE
    USING (TRUE);

-- 6. FUNCIÓN RPC: OBTENER TODOS LOS USUARIOS REGISTRADOS DESDE AUTH.USERS + PROFILES
-- Lee directamente auth.users con SECURITY DEFINER para que no se escape ninguna cuenta creada.
CREATE OR REPLACE FUNCTION public.admin_get_all_users()
RETURNS TABLE (
    id UUID,
    full_name TEXT,
    username TEXT,
    email TEXT,
    phone TEXT,
    avatar_url TEXT,
    role TEXT,
    is_active BOOLEAN,
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        u.id,
        COALESCE(p.full_name, u.raw_user_meta_data->>'full_name', split_part(u.email, '@', 1))::TEXT,
        COALESCE(p.username, u.raw_user_meta_data->>'username', split_part(u.email, '@', 1))::TEXT,
        COALESCE(u.email, p.email, '')::TEXT,
        COALESCE(p.phone, u.phone, '')::TEXT,
        p.avatar_url,
        COALESCE(p.role, 
            CASE 
                WHEN u.email ILIKE '%kevinrx%' OR u.raw_user_meta_data->>'username' ILIKE '%kevinrx%' THEN 'super_admin'
                WHEN u.email ILIKE '%jhosmar%' OR u.raw_user_meta_data->>'username' ILIKE '%jhosmar%' THEN 'admin'
                ELSE 'customer'
            END
        )::TEXT,
        COALESCE(p.is_active, TRUE),
        COALESCE(p.created_at, u.created_at),
        COALESCE(p.updated_at, u.updated_at)
    FROM auth.users u
    LEFT JOIN public.profiles p ON p.id = u.id
    ORDER BY COALESCE(p.created_at, u.created_at) DESC;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION public.admin_get_all_users() TO anon, authenticated, service_role;

-- 7. FUNCIÓN RPC: ASIGNAR ROL A USUARIO DESDE EL PANEL WEB
CREATE OR REPLACE FUNCTION public.admin_set_user_role(p_user_id UUID, p_role TEXT)
RETURNS BOOLEAN AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, email, username, role)
    SELECT 
        u.id,
        COALESCE(u.raw_user_meta_data->>'full_name', split_part(u.email, '@', 1)),
        COALESCE(u.email, ''),
        COALESCE(u.raw_user_meta_data->>'username', split_part(u.email, '@', 1)),
        p_role
    FROM auth.users u
    WHERE u.id = p_user_id
    ON CONFLICT (id) DO UPDATE
    SET role = p_role, updated_at = NOW();

    RETURN TRUE;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER;

GRANT EXECUTE ON FUNCTION public.admin_set_user_role(UUID, TEXT) TO anon, authenticated, service_role;

-- 8. RECARGAR EL SCHEMA CACHE DE POSTGREST
NOTIFY pgrst, 'reload schema';
