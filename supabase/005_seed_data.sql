-- ============================================================================
-- NINTEC — Base de Datos Supabase
-- 005_seed_data.sql — Datos iniciales / Seed
--
-- Ejecutar DESPUÉS de 004_storage.sql
-- Migra los datos hardcodeados de los repositorios Java a la BD real
-- ============================================================================

-- ============================================================================
-- CATEGORÍAS
-- ============================================================================
INSERT INTO public.categories (id, name, slug, icon_name, sort_order, is_active) VALUES
    ('a1000000-0000-0000-0000-000000000001', 'Laptops',     'laptops',     'ic_laptop',        1, TRUE),
    ('a1000000-0000-0000-0000-000000000002', 'Celulares',   'celulares',   'ic_phone_android',  2, TRUE),
    ('a1000000-0000-0000-0000-000000000003', 'Audio',       'audio',       'ic_audio',          3, TRUE),
    ('a1000000-0000-0000-0000-000000000004', 'Accesorios',  'accesorios',  'ic_accessories',    4, TRUE),
    ('a1000000-0000-0000-0000-000000000005', 'Soporte',     'soporte',     'ic_support',        5, TRUE);

-- ============================================================================
-- PRODUCTOS (migrados de ProductRepository.java)
-- ============================================================================
INSERT INTO public.products (id, category_id, name, slug, description, price, old_price, is_new, is_featured, sku, brand) VALUES
    -- Laptops
    ('b1000000-0000-0000-0000-000000000001',
     'a1000000-0000-0000-0000-000000000001',
     'MacBook Pro M3 Max', 'macbook-pro-m3-max',
     'La MacBook Pro de 14 pulgadas con chip M3 Max vuela en flujos de trabajo extremos para programadores y diseñadores.',
     2499.00, 2999.00, TRUE, TRUE, 'NIN-LAP-001', 'Apple'),

    ('b1000000-0000-0000-0000-000000000005',
     'a1000000-0000-0000-0000-000000000001',
     'Laptop ASUS ROG Strix', 'laptop-asus-rog-strix',
     'Rendimiento gaming extremo con procesador de última generación y refrigeración inteligente avanzada.',
     1899.00, NULL, FALSE, FALSE, 'NIN-LAP-002', 'ASUS'),

    -- Celulares
    ('b1000000-0000-0000-0000-000000000002',
     'a1000000-0000-0000-0000-000000000002',
     'iPhone 15 Pro Titanium', 'iphone-15-pro-titanium',
     'Forjado en titanio, el iPhone 15 Pro estrena chip A17 Pro revolucionario y sistema de cámaras avanzado.',
     1099.00, 1199.00, TRUE, TRUE, 'NIN-CEL-001', 'Apple'),

    ('b1000000-0000-0000-0000-000000000006',
     'a1000000-0000-0000-0000-000000000002',
     'Samsung Galaxy S24 Ultra', 'samsung-galaxy-s24-ultra',
     'El buque insignia de Samsung con cámara de 200MP, inteligencia artificial Galaxy AI y S Pen integrado.',
     1299.00, 1399.00, TRUE, TRUE, 'NIN-CEL-002', 'Samsung'),

    -- Audio
    ('b1000000-0000-0000-0000-000000000003',
     'a1000000-0000-0000-0000-000000000003',
     'Audífonos Sony WH-1000XM5', 'audifonos-sony-wh-1000xm5',
     'Audífonos inalámbricos premium con cancelación de ruido inteligente líder en la industria de audio profesional.',
     349.00, NULL, FALSE, FALSE, 'NIN-AUD-001', 'Sony'),

    ('b1000000-0000-0000-0000-000000000008',
     'a1000000-0000-0000-0000-000000000003',
     'Parlante JBL Flip 6', 'parlante-jbl-flip-6',
     'Parlante portátil resistente al agua IP67 con un sonido potente, nítido y graves profundos optimizados.',
     119.00, NULL, FALSE, FALSE, 'NIN-AUD-002', 'JBL'),

    -- Accesorios
    ('b1000000-0000-0000-0000-000000000004',
     'a1000000-0000-0000-0000-000000000004',
     'Teclado Mecánico Nintec RGB', 'teclado-mecanico-nintec-rgb',
     'Teclado mecánico ultra-responsivo con switches brown de alta durabilidad y retroiluminación RGB dinámica.',
     89.00, 120.00, FALSE, FALSE, 'NIN-ACC-001', 'NINTEC'),

    ('b1000000-0000-0000-0000-000000000007',
     'a1000000-0000-0000-0000-000000000004',
     'Mouse Gamer Inalámbrico', 'mouse-gamer-inalambrico',
     'Mouse gamer con sensor óptico de alta precisión de hasta 16000 DPI y conexión libre de latencia.',
     59.00, 75.00, FALSE, FALSE, 'NIN-ACC-002', 'NINTEC');

-- ============================================================================
-- ESPECIFICACIONES DE PRODUCTOS
-- ============================================================================
-- MacBook Pro M3 Max
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000001', 'Procesador',     'Apple M3 Max 14-core',        1),
    ('b1000000-0000-0000-0000-000000000001', 'Memoria RAM',    '36 GB Unified',               2),
    ('b1000000-0000-0000-0000-000000000001', 'Almacenamiento', '1 TB NVMe SSD',               3),
    ('b1000000-0000-0000-0000-000000000001', 'Pantalla',       '14.2" Liquid Retina XDR',     4);

-- iPhone 15 Pro Titanium
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000002', 'Procesador',     'Apple A17 Pro',                     1),
    ('b1000000-0000-0000-0000-000000000002', 'Almacenamiento', '256 GB',                            2),
    ('b1000000-0000-0000-0000-000000000002', 'Pantalla',       '6.1" Super Retina XDR',             3),
    ('b1000000-0000-0000-0000-000000000002', 'Material',       'Titanio de grado aeroespacial',     4);

-- Audífonos Sony WH-1000XM5
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000003', 'Conectividad',     'Bluetooth 5.2 / Jack 3.5mm',          1),
    ('b1000000-0000-0000-0000-000000000003', 'Autonomía',        'Hasta 30 horas continuas',            2),
    ('b1000000-0000-0000-0000-000000000003', 'Cancelación Ruido','Active Noise Cancelling (ANC)',       3);

-- Teclado Mecánico Nintec RGB
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000004', 'Tipo Switch',   'Mecánico Brown',              1),
    ('b1000000-0000-0000-0000-000000000004', 'Formato',       'TKL (Tenkeyless 80%)',        2),
    ('b1000000-0000-0000-0000-000000000004', 'Iluminación',   'RGB Custom por tecla',        3);

-- Samsung Galaxy S24 Ultra
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000006', 'Procesador',     'Snapdragon 8 Gen 3',          1),
    ('b1000000-0000-0000-0000-000000000006', 'Cámara',         '200MP principal',              2),
    ('b1000000-0000-0000-0000-000000000006', 'Pantalla',       '6.8" Dynamic AMOLED 2X',      3),
    ('b1000000-0000-0000-0000-000000000006', 'Batería',        '5000 mAh',                    4);

-- Specs por defecto para productos sin especificaciones detalladas
INSERT INTO public.product_specifications (product_id, spec_key, spec_value, sort_order) VALUES
    ('b1000000-0000-0000-0000-000000000005', 'Garantía',        '12 meses oficial NINTECLP', 1),
    ('b1000000-0000-0000-0000-000000000005', 'Disponibilidad',  'Inmediata',                 2),
    ('b1000000-0000-0000-0000-000000000007', 'Garantía',        '12 meses oficial NINTECLP', 1),
    ('b1000000-0000-0000-0000-000000000007', 'Disponibilidad',  'Inmediata',                 2),
    ('b1000000-0000-0000-0000-000000000008', 'Garantía',        '12 meses oficial NINTECLP', 1),
    ('b1000000-0000-0000-0000-000000000008', 'Disponibilidad',  'Inmediata',                 2);

-- ============================================================================
-- SUCURSALES (migradas de BranchRepository.java)
-- ============================================================================
INSERT INTO public.branches (id, name, address, city, latitude, longitude, phone, is_active) VALUES
    ('c1000000-0000-0000-0000-000000000001',
     'Sucursal Central', 'Av. San Martín #123, Centro', 'La Paz',
     -16.5000, -68.1500, '+591 2 1234567', TRUE),

    ('c1000000-0000-0000-0000-000000000002',
     'NINTECLP Sur', 'Calle 21 de Calacoto, Edif. Arce', 'La Paz',
     -16.5390, -68.0864, '+591 2 7654321', TRUE),

    ('c1000000-0000-0000-0000-000000000003',
     'NINTECLP El Alto', 'Av. 6 de Marzo, C.C. El Ceibo', 'El Alto',
     -16.5122, -68.1603, '+591 2 2334455', TRUE),

    ('c1000000-0000-0000-0000-000000000004',
     'Sucursal Miraflores', 'Plaza Villarroel, Edif. Mirador', 'La Paz',
     -16.4880, -68.1180, '+591 2 9988776', TRUE);

-- ============================================================================
-- HORARIOS DE SUCURSALES
-- ============================================================================
-- Sucursal Central: 09:00 - 20:00 (Lunes a Sábado), Domingo cerrado
INSERT INTO public.branch_schedules (branch_id, day_of_week, open_time, close_time, is_closed) VALUES
    ('c1000000-0000-0000-0000-000000000001', 0, '00:00', '00:00', TRUE),  -- Domingo cerrado
    ('c1000000-0000-0000-0000-000000000001', 1, '09:00', '20:00', FALSE),
    ('c1000000-0000-0000-0000-000000000001', 2, '09:00', '20:00', FALSE),
    ('c1000000-0000-0000-0000-000000000001', 3, '09:00', '20:00', FALSE),
    ('c1000000-0000-0000-0000-000000000001', 4, '09:00', '20:00', FALSE),
    ('c1000000-0000-0000-0000-000000000001', 5, '09:00', '20:00', FALSE),
    ('c1000000-0000-0000-0000-000000000001', 6, '09:00', '20:00', FALSE);

-- NINTECLP Sur: 10:00 - 21:00 (Lunes a Sábado), Domingo cerrado
INSERT INTO public.branch_schedules (branch_id, day_of_week, open_time, close_time, is_closed) VALUES
    ('c1000000-0000-0000-0000-000000000002', 0, '00:00', '00:00', TRUE),
    ('c1000000-0000-0000-0000-000000000002', 1, '10:00', '21:00', FALSE),
    ('c1000000-0000-0000-0000-000000000002', 2, '10:00', '21:00', FALSE),
    ('c1000000-0000-0000-0000-000000000002', 3, '10:00', '21:00', FALSE),
    ('c1000000-0000-0000-0000-000000000002', 4, '10:00', '21:00', FALSE),
    ('c1000000-0000-0000-0000-000000000002', 5, '10:00', '21:00', FALSE),
    ('c1000000-0000-0000-0000-000000000002', 6, '10:00', '21:00', FALSE);

-- NINTECLP El Alto: 09:00 - 19:00 (Lunes a Sábado), Domingo cerrado
INSERT INTO public.branch_schedules (branch_id, day_of_week, open_time, close_time, is_closed) VALUES
    ('c1000000-0000-0000-0000-000000000003', 0, '00:00', '00:00', TRUE),
    ('c1000000-0000-0000-0000-000000000003', 1, '09:00', '19:00', FALSE),
    ('c1000000-0000-0000-0000-000000000003', 2, '09:00', '19:00', FALSE),
    ('c1000000-0000-0000-0000-000000000003', 3, '09:00', '19:00', FALSE),
    ('c1000000-0000-0000-0000-000000000003', 4, '09:00', '19:00', FALSE),
    ('c1000000-0000-0000-0000-000000000003', 5, '09:00', '19:00', FALSE),
    ('c1000000-0000-0000-0000-000000000003', 6, '09:00', '19:00', FALSE);

-- Sucursal Miraflores: 08:30 - 18:30 (Lunes a Sábado), Domingo cerrado
INSERT INTO public.branch_schedules (branch_id, day_of_week, open_time, close_time, is_closed) VALUES
    ('c1000000-0000-0000-0000-000000000004', 0, '00:00', '00:00', TRUE),
    ('c1000000-0000-0000-0000-000000000004', 1, '08:30', '18:30', FALSE),
    ('c1000000-0000-0000-0000-000000000004', 2, '08:30', '18:30', FALSE),
    ('c1000000-0000-0000-0000-000000000004', 3, '08:30', '18:30', FALSE),
    ('c1000000-0000-0000-0000-000000000004', 4, '08:30', '18:30', FALSE),
    ('c1000000-0000-0000-0000-000000000004', 5, '08:30', '18:30', FALSE),
    ('c1000000-0000-0000-0000-000000000004', 6, '08:30', '18:30', FALSE);

-- ============================================================================
-- INVENTARIO POR SUCURSAL (stock distribuido)
-- ============================================================================
-- Sucursal Central (stock principal)
INSERT INTO public.branch_inventory (branch_id, product_id, stock, min_stock) VALUES
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000001', 3, 2),   -- MacBook Pro
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000002', 4, 2),   -- iPhone 15 Pro
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000003', 6, 3),   -- Sony WH-1000XM5
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000004', 10, 3),  -- Teclado Nintec
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000005', 0, 2),   -- ASUS ROG (agotado)
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000006', 2, 2),   -- Samsung S24
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000007', 8, 3),   -- Mouse Gamer
    ('c1000000-0000-0000-0000-000000000001', 'b1000000-0000-0000-0000-000000000008', 5, 3);   -- JBL Flip 6

-- NINTECLP Sur
INSERT INTO public.branch_inventory (branch_id, product_id, stock, min_stock) VALUES
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000001', 1, 1),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000002', 2, 1),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000003', 3, 2),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000004', 5, 2),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000005', 0, 1),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000006', 1, 1),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000007', 4, 2),
    ('c1000000-0000-0000-0000-000000000002', 'b1000000-0000-0000-0000-000000000008', 3, 2);

-- NINTECLP El Alto
INSERT INTO public.branch_inventory (branch_id, product_id, stock, min_stock) VALUES
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000001', 1, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000002', 1, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000003', 2, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000004', 3, 2),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000005', 0, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000006', 1, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000007', 2, 1),
    ('c1000000-0000-0000-0000-000000000003', 'b1000000-0000-0000-0000-000000000008', 1, 1);

-- Sucursal Miraflores
INSERT INTO public.branch_inventory (branch_id, product_id, stock, min_stock) VALUES
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000001', 0, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000002', 1, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000003', 1, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000004', 2, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000005', 0, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000006', 0, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000007', 1, 1),
    ('c1000000-0000-0000-0000-000000000004', 'b1000000-0000-0000-0000-000000000008', 1, 1);

-- ============================================================================
-- MÉTODOS DE PAGO
-- ============================================================================
INSERT INTO public.payment_methods (name, code, description, icon_name, sort_order, is_active, requires_proof) VALUES
    ('Visa / Mastercard', 'visa_mc',   'Pago con tarjeta de crédito o débito',                    'ic_credit_card', 1, TRUE, FALSE),
    ('PayPal',            'paypal',    'Pago seguro a través de PayPal',                           'ic_paypal',      2, TRUE, FALSE),
    ('QR',                'qr',        'Pago mediante código QR desde tu app bancaria',            'ic_qr_code',     3, TRUE, TRUE),
    ('Transferencia',     'transfer',  'Transferencia bancaria directa. Enviar comprobante.',      'ic_bank',        4, TRUE, TRUE),
    ('Efectivo',          'cash',      'Pago en efectivo al recoger en sucursal',                  'ic_cash',        5, TRUE, FALSE);

-- ============================================================================
-- CONFIGURACIÓN DE LA APP (app_settings)
-- El admin puede modificar TODO esto desde el panel web
-- ============================================================================
INSERT INTO public.app_settings (setting_key, value, value_type, category, label, description, is_public) VALUES
    -- ─── Identidad de marca ───
    ('app_name',                'NINTECLP',                              'string',  'brand',    'Nombre de la App',           'Nombre que se muestra en toda la aplicación',    TRUE),
    ('app_tagline',             'Tu Tienda de Tecnología en Bolivia',    'string',  'brand',    'Eslogan',                    'Frase de eslogan de la marca',                   TRUE),
    ('app_logo_url',            '',                                     'url',     'brand',    'Logo URL',                   'URL del logo principal de la app',                TRUE),
    ('app_logo_dark_url',       '',                                     'url',     'brand',    'Logo URL (modo oscuro)',     'URL del logo para modo oscuro',                  TRUE),
    ('app_favicon_url',         '',                                     'url',     'brand',    'Favicon URL',                'URL del favicon',                                TRUE),

    -- ─── Colores del tema ───
    ('color_primary',           '#1A237E',                              'color',   'theme',    'Color Primario',             'Color principal de la marca',                    TRUE),
    ('color_primary_light',     '#534BAE',                              'color',   'theme',    'Color Primario Claro',       'Variante clara del color primario',              TRUE),
    ('color_primary_dark',      '#000051',                              'color',   'theme',    'Color Primario Oscuro',      'Variante oscura del color primario',             TRUE),
    ('color_accent',            '#FFD600',                              'color',   'theme',    'Color Acento',               'Color de acento / botones destacados',           TRUE),
    ('color_background',        '#F5F5F5',                              'color',   'theme',    'Color de Fondo',             'Color de fondo general de la app',               TRUE),
    ('color_surface',           '#FFFFFF',                              'color',   'theme',    'Color de Superficie',        'Color de cards y superficies',                   TRUE),
    ('color_text_primary',      '#212121',                              'color',   'theme',    'Color Texto Principal',      'Color del texto principal',                      TRUE),
    ('color_text_secondary',    '#757575',                              'color',   'theme',    'Color Texto Secundario',     'Color del texto secundario',                     TRUE),

    -- ─── Contacto ───
    ('contact_email',           'contacto@ninteclp.com',                'string',  'contact',  'Email de Contacto',          'Email principal de contacto',                    TRUE),
    ('contact_phone',           '+591 2 1234567',                       'string',  'contact',  'Teléfono de Contacto',       'Teléfono principal de atención',                 TRUE),
    ('contact_whatsapp',        '+59172123456',                         'string',  'contact',  'WhatsApp',                   'Número de WhatsApp para soporte',                TRUE),
    ('contact_address',         'Av. San Martín #123, La Paz, Bolivia', 'string',  'contact',  'Dirección Principal',        'Dirección física principal',                     TRUE),

    -- ─── Redes sociales ───
    ('social_facebook',         'https://facebook.com/ninteclp',        'url',     'social',   'Facebook',                   'URL de la página de Facebook',                   TRUE),
    ('social_instagram',        'https://instagram.com/ninteclp',       'url',     'social',   'Instagram',                  'URL del perfil de Instagram',                    TRUE),
    ('social_tiktok',           'https://tiktok.com/@ninteclp',         'url',     'social',   'TikTok',                     'URL del perfil de TikTok',                       TRUE),
    ('social_twitter',          '',                                     'url',     'social',   'Twitter / X',                'URL del perfil de Twitter/X',                    TRUE),
    ('social_youtube',          '',                                     'url',     'social',   'YouTube',                    'URL del canal de YouTube',                       TRUE),

    -- ─── Configuración de negocio ───
    ('currency_code',           'BOB',                                  'string',  'business', 'Código de Moneda',           'Código ISO de la moneda',                        TRUE),
    ('currency_symbol',         'Bs',                                   'string',  'business', 'Símbolo de Moneda',          'Símbolo que se muestra antes del precio',        TRUE),
    ('tax_percentage',          '0',                                    'number',  'business', 'Porcentaje de Impuesto',     'Porcentaje de IVA/impuesto a aplicar',           FALSE),
    ('min_order_amount',        '0',                                    'number',  'business', 'Monto Mínimo de Pedido',     'Monto mínimo para realizar un pedido (0=sin mínimo)', TRUE),
    ('max_items_per_product',   '10',                                   'number',  'business', 'Máx. Unidades por Producto', 'Máximo de unidades del mismo producto por pedido', TRUE),
    ('free_shipping_threshold', '500',                                  'number',  'business', 'Envío Gratis Desde (Bs)',    'Monto a partir del cual el envío es gratis',     TRUE),

    -- ─── Funcionalidades on/off ───
    ('feature_delivery_enabled',  'true',                               'boolean', 'features', 'Envío a Domicilio',         'Habilitar envío a domicilio',                    TRUE),
    ('feature_pickup_enabled',    'true',                               'boolean', 'features', 'Recogida en Sucursal',      'Habilitar recogida en sucursal',                 TRUE),
    ('feature_coupons_enabled',   'true',                               'boolean', 'features', 'Cupones de Descuento',      'Habilitar sistema de cupones',                   TRUE),
    ('feature_reviews_enabled',   'false',                              'boolean', 'features', 'Reseñas de Productos',      'Habilitar reseñas de clientes',                  TRUE),
    ('feature_wishlist_enabled',  'false',                              'boolean', 'features', 'Lista de Deseos',           'Habilitar lista de deseos',                      TRUE),
    ('feature_notifications',     'true',                               'boolean', 'features', 'Notificaciones Push',       'Habilitar notificaciones push',                  TRUE),

    -- ─── Textos legales / configurables ───
    ('checkout_disclaimer',     'Al realizar tu pedido aceptas nuestros Términos y Condiciones.', 'string', 'legal', 'Texto de Checkout', 'Texto legal que se muestra antes de confirmar pedido', TRUE),
    ('order_success_message',   '¡Tu pedido ha sido recibido! Te notificaremos cuando esté listo.', 'string', 'messages', 'Mensaje de Éxito', 'Mensaje al completar una orden exitosamente', TRUE),

    -- ─── Mantenimiento ───
    ('maintenance_mode',        'false',                                'boolean', 'system',   'Modo Mantenimiento',        'Activar modo mantenimiento (bloquea la app)',    TRUE),
    ('maintenance_message',     'Estamos realizando mejoras. Volvemos pronto.', 'string', 'system', 'Mensaje Mantenimiento', 'Mensaje que se muestra durante el mantenimiento', TRUE),
    ('app_min_version',         '1.0.0',                                'string',  'system',   'Versión Mínima de App',     'Versión mínima de la app para forzar actualización', TRUE);

-- ============================================================================
-- BANNERS INICIALES
-- ============================================================================
INSERT INTO public.banners (title, subtitle, action_type, action_value, background_color, text_color, sort_order, is_active, image_url) VALUES
    ('Ofertas de Temporada',    'Hasta 20% de descuento en laptops',   'category', 'laptops',    '#1A237E', '#FFFFFF', 1, TRUE, ''),
    ('iPhone 15 Pro',           'Nuevo. Forjado en titanio.',          'product',  'b1000000-0000-0000-0000-000000000002', '#000000', '#FFFFFF', 2, TRUE, ''),
    ('Galaxy AI Llegó',         'Samsung Galaxy S24 Ultra disponible', 'product',  'b1000000-0000-0000-0000-000000000006', '#1B0A3C', '#FFFFFF', 3, TRUE, '');

-- ============================================================================
-- PÁGINAS DE CONTENIDO (CMS)
-- ============================================================================
INSERT INTO public.pages (title, slug, content, is_active, sort_order) VALUES
    ('Sobre Nosotros', 'about-us',
     '# Sobre NINTECLP

NINTECLP es tu tienda de tecnología de confianza en Bolivia. Desde nuestra fundación, nos hemos dedicado a ofrecer los mejores productos tecnológicos con la mejor atención al cliente.

## Nuestra Misión
Democratizar el acceso a la tecnología de calidad en Bolivia, ofreciendo productos originales con garantía y servicio posventa excepcional.

## Nuestra Visión
Ser la tienda de tecnología líder en Bolivia, reconocida por nuestra calidad, innovación y compromiso con el cliente.',
     TRUE, 1),

    ('Términos y Condiciones', 'terms',
     '# Términos y Condiciones

Última actualización: Septiembre 2026

## 1. Aceptación de Términos
Al utilizar la aplicación NINTECLP, aceptas estos términos y condiciones.

## 2. Uso de la Aplicación
La aplicación está destinada para la compra de productos tecnológicos disponibles en nuestro catálogo.

## 3. Precios y Pagos
Todos los precios están expresados en Bolivianos (Bs). Los precios pueden cambiar sin previo aviso.

## 4. Envíos y Entregas
Los tiempos de entrega son estimados y pueden variar según la disponibilidad y ubicación.

## 5. Devoluciones
Aceptamos devoluciones dentro de los 7 días posteriores a la compra con el producto en condiciones originales.',
     TRUE, 2),

    ('Política de Privacidad', 'privacy',
     '# Política de Privacidad

NINTECLP se compromete a proteger tu información personal.

## Datos que Recopilamos
- Nombre y datos de contacto
- Dirección de envío
- Historial de compras

## Uso de Datos
Utilizamos tus datos únicamente para procesar tus pedidos y mejorar tu experiencia de compra.

## Seguridad
Implementamos medidas de seguridad estándar de la industria para proteger tu información.',
     TRUE, 3),

    ('Política de Devoluciones', 'returns',
     '# Política de Devoluciones

## Plazo de Devolución
Tienes 7 días calendario desde la recepción del producto para solicitar una devolución.

## Condiciones
- El producto debe estar en su empaque original
- No debe presentar señales de uso
- Debe incluir todos los accesorios

## Proceso
1. Contacta a nuestro servicio al cliente
2. Recibe un número de autorización de devolución
3. Entrega el producto en cualquiera de nuestras sucursales
4. El reembolso se procesa en 5-10 días hábiles',
     TRUE, 4);

-- ============================================================================
-- FAQs
-- ============================================================================
INSERT INTO public.faqs (question, answer, category, sort_order, is_active) VALUES
    ('¿Cuáles son los métodos de pago aceptados?',
     'Aceptamos Visa, Mastercard, PayPal, pagos por QR bancario, transferencia bancaria y efectivo en sucursal.',
     'pagos', 1, TRUE),

    ('¿Cuánto tarda el envío?',
     'Los envíos dentro de La Paz se realizan en 24-48 horas. Para otras ciudades, el tiempo estimado es de 3-5 días hábiles.',
     'envios', 2, TRUE),

    ('¿Los productos tienen garantía?',
     'Sí, todos nuestros productos cuentan con garantía oficial de 12 meses.',
     'productos', 3, TRUE),

    ('¿Puedo recoger mi pedido en sucursal?',
     'Sí, puedes seleccionar la opción de recogida en sucursal al momento de realizar tu pedido.',
     'envios', 4, TRUE),

    ('¿Cómo puedo hacer seguimiento de mi pedido?',
     'Desde la sección "Historial de Pedidos" en tu perfil puedes ver el estado actualizado de todos tus pedidos.',
     'general', 5, TRUE),

    ('¿Cómo solicito una devolución?',
     'Puedes solicitar una devolución dentro de los 7 días posteriores a la compra contactándonos por WhatsApp o email.',
     'devoluciones', 6, TRUE);

-- ============================================================================
-- CUPÓN DE EJEMPLO
-- ============================================================================
INSERT INTO public.coupons (code, description, discount_type, discount_value, min_purchase, max_discount, max_uses, max_uses_per_user, is_active) VALUES
    ('BIENVENIDO10', 'Descuento de bienvenida: 10% en tu primera compra', 'percentage', 10, 100, 250, NULL, 1, TRUE),
    ('NINTEC50',     'Bs 50 de descuento en compras mayores a Bs 500',     'fixed',      50, 500, NULL, 100, 1, TRUE);

-- ============================================================================
-- ZONAS DE ENVÍO
-- ============================================================================
INSERT INTO public.shipping_zones (name, city, shipping_cost, estimated_days, is_active) VALUES
    ('Centro',        'La Paz',     15.00,  1, TRUE),
    ('Zona Sur',      'La Paz',     20.00,  1, TRUE),
    ('Miraflores',    'La Paz',     15.00,  1, TRUE),
    ('El Alto',       'El Alto',    25.00,  1, TRUE),
    ('Cochabamba',    'Cochabamba',  45.00,  3, TRUE),
    ('Santa Cruz',    'Santa Cruz',  50.00,  3, TRUE),
    ('Otros',         'Interior',   60.00,  5, TRUE);

-- ============================================================================
-- VERIFICACIÓN DE SEED DATA
-- ============================================================================
DO $$
DECLARE
    v_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO v_count FROM public.categories;
    RAISE NOTICE '✅ Categorías insertadas: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.products;
    RAISE NOTICE '✅ Productos insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.product_specifications;
    RAISE NOTICE '✅ Especificaciones insertadas: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.branches;
    RAISE NOTICE '✅ Sucursales insertadas: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.branch_schedules;
    RAISE NOTICE '✅ Horarios insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.branch_inventory;
    RAISE NOTICE '✅ Inventario por sucursal insertado: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.payment_methods;
    RAISE NOTICE '✅ Métodos de pago insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.app_settings;
    RAISE NOTICE '✅ Configuraciones insertadas: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.banners;
    RAISE NOTICE '✅ Banners insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.pages;
    RAISE NOTICE '✅ Páginas CMS insertadas: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.faqs;
    RAISE NOTICE '✅ FAQs insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.coupons;
    RAISE NOTICE '✅ Cupones insertados: %', v_count;

    SELECT COUNT(*) INTO v_count FROM public.shipping_zones;
    RAISE NOTICE '✅ Zonas de envío insertadas: %', v_count;

    RAISE NOTICE '';
    RAISE NOTICE '🎉 Seed data completado exitosamente!';
    RAISE NOTICE '📌 Recuerda: El primer usuario que registres con rol admin será el super_admin.';
    RAISE NOTICE '   Usa el siguiente SQL para promoverlo:';
    RAISE NOTICE '   UPDATE public.profiles SET role = ''super_admin'' WHERE email = ''tu@email.com'';';
END;
$$;

-- ============================================================================
-- FIN DE 005_seed_data.sql
-- ============================================================================
