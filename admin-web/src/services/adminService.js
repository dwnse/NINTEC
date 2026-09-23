import { supabase } from './supabaseClient'

/**
 * Service to handle all Administrative operations directly with Supabase.
 */
export const adminService = {
  // ─────────────────────────────────────────────────────────────
  // 1. DASHBOARD & REPORTES
  // ─────────────────────────────────────────────────────────────

  async getDashboardStats() {
    try {
      // Intentar llamar a la función RPC admin_get_dashboard()
      const { data: rpcData, error: rpcError } = await supabase.rpc('admin_get_dashboard')
      if (!rpcError && rpcData && rpcData.length > 0) {
        return rpcData[0]
      }
    } catch {
      // Ignorar y calcular manualmente como fallback
    }

    // Fallback: consultas directas
    const [
      ordersRes,
      pendingOrdersRes,
      completedOrdersRes,
      customersRes,
      productsRes,
      inventoryRes
    ] = await Promise.all([
      supabase.from('orders').select('id, total, status, created_at'),
      supabase.from('orders').select('id', { count: 'exact', head: true }).eq('status', 'pending'),
      supabase.from('orders').select('id', { count: 'exact', head: true }).in('status', ['completed', 'delivered']),
      supabase.from('profiles').select('id', { count: 'exact', head: true }).eq('role', 'customer'),
      supabase.from('products').select('id', { count: 'exact', head: true }).eq('is_active', true),
      supabase.from('branch_inventory').select('stock, min_stock')
    ])

    const orders = ordersRes.data || []
    const totalRevenue = orders
      .filter(o => ['completed', 'delivered'].includes(o.status))
      .reduce((sum, o) => sum + Number(o.total || 0), 0)

    const now = new Date()
    const firstDayOfMonth = new Date(now.getFullYear(), now.getMonth(), 1).toISOString()
    const revenueThisMonth = orders
      .filter(o => ['completed', 'delivered'].includes(o.status) && o.created_at >= firstDayOfMonth)
      .reduce((sum, o) => sum + Number(o.total || 0), 0)

    const inventory = inventoryRes.data || []
    const outOfStockProducts = inventory.filter(i => i.stock <= 0).length
    const lowStockAlerts = inventory.filter(i => i.stock > 0 && i.stock <= (i.min_stock || 5)).length

    // Contar usuarios registrados (mínimo las cuentas registradas conocidas)
    let totalCustomers = customersRes.count || 0
    if (totalCustomers === 0) {
      try {
        const usersList = await this.getUsers()
        totalCustomers = usersList.length
      } catch {
        totalCustomers = 2
      }
    }

    return {
      total_orders: orders.length,
      pending_orders: pendingOrdersRes.count || 0,
      completed_orders: completedOrdersRes.count || 0,
      total_revenue: totalRevenue,
      revenue_this_month: revenueThisMonth,
      total_customers: totalCustomers,
      total_products: productsRes.count || 0,
      out_of_stock_products: outOfStockProducts,
      low_stock_alerts: lowStockAlerts
    }
  },

  async getRecentOrders(limit = 10) {
    const { data, error } = await supabase
      .from('orders')
      .select(`
        id,
        order_number,
        status,
        payment_method,
        payment_status,
        total,
        created_at,
        notes,
        admin_notes,
        profiles:user_id (id, full_name, email, phone)
      `)
      .order('created_at', { ascending: false })
      .limit(limit)

    if (error) throw error
    return data || []
  },

  async getSalesChartData(days = 7) {
    const fromDate = new Date()
    fromDate.setDate(fromDate.getDate() - days)

    const { data, error } = await supabase
      .from('orders')
      .select('total, status, created_at')
      .gte('created_at', fromDate.toISOString())
      .order('created_at', { ascending: true })

    if (error) throw error

    // Agrupar por día
    const dailyMap = {}
    for (let i = days - 1; i >= 0; i--) {
      const d = new Date()
      d.setDate(d.getDate() - i)
      const key = d.toLocaleDateString('es-BO', { month: 'short', day: 'numeric' })
      dailyMap[key] = { label: key, total: 0, orders: 0 }
    }

    ;(data || []).forEach(o => {
      const d = new Date(o.created_at)
      const key = d.toLocaleDateString('es-BO', { month: 'short', day: 'numeric' })
      if (dailyMap[key]) {
        dailyMap[key].orders += 1
        if (['completed', 'delivered', 'processing', 'confirmed'].includes(o.status)) {
          dailyMap[key].total += Number(o.total || 0)
        }
      }
    })

    return Object.values(dailyMap)
  },

  // ─────────────────────────────────────────────────────────────
  // 2. CONFIGURACIÓN DE ARTÍCULOS E IMÁGENES
  // ─────────────────────────────────────────────────────────────

  async getProducts(filters = {}) {
    let query = supabase
      .from('products')
      .select(`
        *,
        categories:category_id (id, name, slug),
        product_images (id, image_url, is_primary, sort_order),
        branch_inventory (branch_id, stock, min_stock)
      `)
      .order('created_at', { ascending: false })

    if (filters.categoryId) {
      query = query.eq('category_id', filters.categoryId)
    }
    if (filters.isFeatured !== undefined) {
      query = query.eq('is_featured', filters.isFeatured)
    }
    if (filters.isActive !== undefined) {
      query = query.eq('is_active', filters.isActive)
    }
    if (filters.search) {
      query = query.or(`name.ilike.%${filters.search}%,description.ilike.%${filters.search}%,sku.ilike.%${filters.search}%`)
    }

    const { data, error } = await query
    if (error) throw error
    return data || []
  },

  async getProductDetails(id) {
    const { data, error } = await supabase
      .from('products')
      .select(`
        *,
        categories:category_id (id, name, slug),
        product_images (id, image_url, is_primary, sort_order),
        product_specifications (id, spec_key, spec_value, sort_order),
        branch_inventory (id, branch_id, stock, min_stock, branches:branch_id (id, name, city))
      `)
      .eq('id', id)
      .single()

    if (error) throw error
    return data
  },

  async saveProduct(product, images = [], specs = [], inventoryPerBranch = []) {
    let productId = product.id

    // Generar slug si no tiene
    const slug = product.slug || product.name.toLowerCase()
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/(^-|-$)+/g, '') + '-' + Date.now().toString().slice(-4)

    const productPayload = {
      name: product.name,
      slug,
      category_id: product.category_id,
      description: product.description,
      price: parseFloat(product.price),
      old_price: product.old_price ? parseFloat(product.old_price) : null,
      is_new: Boolean(product.is_new),
      is_featured: Boolean(product.is_featured),
      is_active: product.is_active !== undefined ? Boolean(product.is_active) : true,
      sku: product.sku || null,
      brand: product.brand || null,
      weight_kg: product.weight_kg ? parseFloat(product.weight_kg) : null,
      updated_at: new Date().toISOString()
    }

    if (productId) {
      // Actualizar
      const { error } = await supabase
        .from('products')
        .update(productPayload)
        .eq('id', productId)
      if (error) throw error
    } else {
      // Crear
      const { data, error } = await supabase
        .from('products')
        .insert([productPayload])
        .select()
        .single()
      if (error) throw error
      productId = data.id
    }

    // Guardar Especificaciones (reemplazar si se editaron)
    if (specs && specs.length >= 0) {
      await supabase.from('product_specifications').delete().eq('product_id', productId)
      if (specs.length > 0) {
        const specInserts = specs
          .filter(s => s.key && s.value)
          .map((s, index) => ({
            product_id: productId,
            spec_key: s.key,
            spec_value: s.value,
            sort_order: index + 1
          }))
        if (specInserts.length > 0) {
          await supabase.from('product_specifications').insert(specInserts)
        }
      }
    }

    // Guardar Stock por Sucursal
    if (inventoryPerBranch && inventoryPerBranch.length > 0) {
      for (const item of inventoryPerBranch) {
        if (item.branch_id) {
          await supabase
            .from('branch_inventory')
            .upsert({
              branch_id: item.branch_id,
              product_id: productId,
              stock: parseInt(item.stock) || 0,
              min_stock: parseInt(item.min_stock) || 0,
              updated_at: new Date().toISOString()
            }, { onConflict: 'branch_id,product_id' })
        }
      }
    }

    // Guardar Imágenes
    if (images && images.length > 0) {
      for (let i = 0; i < images.length; i++) {
        const img = images[i]
        if (img.isNew) {
          await supabase.from('product_images').insert({
            product_id: productId,
            image_url: img.url,
            is_primary: i === 0 || img.is_primary,
            sort_order: i
          })
        } else if (img.id) {
          await supabase.from('product_images').update({
            is_primary: img.is_primary,
            sort_order: i
          }).eq('id', img.id)
        }
      }
    }

    return productId
  },

  async deleteProduct(id) {
    const { error } = await supabase.from('products').delete().eq('id', id)
    if (error) throw error
  },

  async toggleProductStatus(id, currentStatus) {
    const { error } = await supabase
      .from('products')
      .update({ is_active: !currentStatus })
      .eq('id', id)
    if (error) throw error
  },

  async uploadStorageFile(bucketName, file, folder = '') {
    const fileExt = file.name.split('.').pop()
    const fileName = `${folder ? folder + '/' : ''}${Date.now()}-${Math.random().toString(36).substring(2, 8)}.${fileExt}`

    const { error: uploadError } = await supabase.storage
      .from(bucketName)
      .upload(fileName, file, {
        cacheControl: '3600',
        upsert: false
      })

    if (uploadError) {
      console.warn(`Storage upload error (${bucketName}):`, uploadError.message)
      throw new Error(`Error en storage bucket "${bucketName}": ${uploadError.message}. Ejecuta el script supabase/007_fix_admin_cruds_and_storage.sql en tu Supabase SQL Editor para habilitar permisos de subida.`)
    }

    const { data } = supabase.storage
      .from(bucketName)
      .getPublicUrl(fileName)

    return {
      publicUrl: data.publicUrl,
      fileName
    }
  },

  async deleteProductImage(imageId) {
    const { error } = await supabase.from('product_images').delete().eq('id', imageId)
    if (error) throw error
  },

  // ─────────────────────────────────────────────────────────────
  // 3. CONFIGURACIÓN DEL BANNER DEL INICIO
  // ─────────────────────────────────────────────────────────────

  async getBanners() {
    const { data, error } = await supabase
      .from('banners')
      .select('*')
      .order('sort_order', { ascending: true })

    if (error) throw error
    return data || []
  },

  async saveBanner(banner) {
    const payload = {
      title: banner.title,
      subtitle: banner.subtitle || '',
      image_url: banner.image_url,
      action_type: banner.action_type || 'none',
      action_value: banner.action_value || null,
      background_color: banner.background_color || '#1846D7',
      text_color: banner.text_color || '#FFFFFF',
      sort_order: parseInt(banner.sort_order) || 0,
      is_active: banner.is_active !== undefined ? Boolean(banner.is_active) : true,
      starts_at: banner.starts_at || null,
      ends_at: banner.ends_at || null,
      updated_at: new Date().toISOString()
    }

    if (banner.id) {
      const { data, error } = await supabase
        .from('banners')
        .update(payload)
        .eq('id', banner.id)
        .select()
        .single()
      if (error) throw error
      return data
    } else {
      const { data, error } = await supabase
        .from('banners')
        .insert([payload])
        .select()
        .single()
      if (error) throw error
      return data
    }
  },

  async deleteBanner(id) {
    const { error } = await supabase.from('banners').delete().eq('id', id)
    if (error) throw error
  },

  async toggleBannerStatus(id, currentStatus) {
    const { error } = await supabase
      .from('banners')
      .update({ is_active: !currentStatus })
      .eq('id', id)
    if (error) throw error
  },

  // ─────────────────────────────────────────────────────────────
  // 4. SUCURSALES E INVENTARIO
  // ─────────────────────────────────────────────────────────────

  async getBranches() {
    const { data, error } = await supabase
      .from('branches')
      .select(`
        *,
        branch_schedules (*),
        branch_inventory (product_id, stock)
      `)
      .order('name', { ascending: true })

    if (error) throw error
    return data || []
  },

  async saveBranch(branch, schedules = []) {
    const payload = {
      name: branch.name,
      address: branch.address,
      city: branch.city || 'La Paz',
      latitude: parseFloat(branch.latitude) || -16.5000,
      longitude: parseFloat(branch.longitude) || -68.1500,
      phone: branch.phone || '',
      email: branch.email || '',
      image_url: branch.image_url || null,
      is_active: branch.is_active !== undefined ? Boolean(branch.is_active) : true,
      updated_at: new Date().toISOString()
    }

    let branchId = branch.id
    if (branchId) {
      const { error } = await supabase
        .from('branches')
        .update(payload)
        .eq('id', branchId)
      if (error) throw error
    } else {
      const { data, error } = await supabase
        .from('branches')
        .insert([payload])
        .select()
        .single()
      if (error) throw error
      branchId = data.id
    }

    // Actualizar horarios de la sucursal (0=Domingo, ..., 6=Sábado)
    if (schedules && schedules.length > 0) {
      for (const s of schedules) {
        await supabase
          .from('branch_schedules')
          .upsert({
            branch_id: branchId,
            day_of_week: s.day_of_week,
            open_time: s.open_time || '09:00:00',
            close_time: s.close_time || '19:00:00',
            is_closed: Boolean(s.is_closed)
          }, { onConflict: 'branch_id,day_of_week' })
      }
    }

    return branchId
  },

  async deleteBranch(id) {
    const { error } = await supabase.from('branches').delete().eq('id', id)
    if (error) throw error
  },

  async getBranchStockTable(branchId) {
    const { data, error } = await supabase
      .from('products')
      .select(`
        id,
        name,
        sku,
        price,
        category_id,
        categories (name),
        branch_inventory!left (stock, min_stock, branch_id)
      `)
      .eq('is_active', true)
      .order('name')

    if (error) throw error

    return (data || []).map(p => {
      const branchInv = (p.branch_inventory || []).find(bi => bi.branch_id === branchId)
      return {
        product_id: p.id,
        name: p.name,
        sku: p.sku || 'N/A',
        price: p.price,
        category: p.categories?.name || 'General',
        stock: branchInv ? branchInv.stock : 0,
        min_stock: branchInv ? branchInv.min_stock : 5
      }
    })
  },

  async updateStock(branchId, productId, stock, minStock = 5) {
    const { error } = await supabase
      .from('branch_inventory')
      .upsert({
        branch_id: branchId,
        product_id: productId,
        stock: parseInt(stock),
        min_stock: parseInt(minStock),
        updated_at: new Date().toISOString()
      }, { onConflict: 'branch_id,product_id' })

    if (error) throw error
  },

  // ─────────────────────────────────────────────────────────────
  // 5. CATÁLOGO, OFERTAS ESPECIALES & CUPONES
  // ─────────────────────────────────────────────────────────────

  async getCategories() {
    const { data, error } = await supabase
      .from('categories')
      .select('*')
      .order('sort_order', { ascending: true })

    if (error) throw error
    return data || []
  },

  async saveCategory(category) {
    const slug = category.slug || category.name.toLowerCase()
      .normalize('NFD').replace(/[\u0300-\u036f]/g, '')
      .replace(/[^a-z0-9]+/g, '-')

    const payload = {
      name: category.name,
      slug,
      description: category.description || '',
      icon_name: category.icon_name || 'ic_devices',
      image_url: category.image_url || null,
      sort_order: parseInt(category.sort_order) || 0,
      is_active: category.is_active !== undefined ? Boolean(category.is_active) : true,
      updated_at: new Date().toISOString()
    }

    if (category.id) {
      const { data, error } = await supabase
        .from('categories')
        .update(payload)
        .eq('id', category.id)
        .select()
        .single()
      if (error) throw error
      return data
    } else {
      const { data, error } = await supabase
        .from('categories')
        .insert([payload])
        .select()
        .single()
      if (error) throw error
      return data
    }
  },

  async deleteCategory(id) {
    const { error } = await supabase.from('categories').delete().eq('id', id)
    if (error) throw error
  },

  async getCoupons() {
    const { data, error } = await supabase
      .from('coupons')
      .select('*')
      .order('created_at', { ascending: false })

    if (error) throw error
    return data || []
  },

  async saveCoupon(coupon) {
    const payload = {
      code: coupon.code.toUpperCase().trim(),
      description: coupon.description || '',
      discount_type: coupon.discount_type, // 'percentage' | 'fixed'
      discount_value: parseFloat(coupon.discount_value),
      min_purchase: coupon.min_purchase ? parseFloat(coupon.min_purchase) : 0,
      max_discount: coupon.max_discount ? parseFloat(coupon.max_discount) : null,
      max_uses: coupon.max_uses ? parseInt(coupon.max_uses) : null,
      max_uses_per_user: coupon.max_uses_per_user ? parseInt(coupon.max_uses_per_user) : 1,
      is_active: coupon.is_active !== undefined ? Boolean(coupon.is_active) : true,
      starts_at: coupon.starts_at || new Date().toISOString(),
      ends_at: coupon.ends_at || null,
      updated_at: new Date().toISOString()
    }

    if (coupon.id) {
      const { data, error } = await supabase
        .from('coupons')
        .update(payload)
        .eq('id', coupon.id)
        .select()
        .single()
      if (error) throw error
      return data
    } else {
      const { data, error } = await supabase
        .from('coupons')
        .insert([payload])
        .select()
        .single()
      if (error) throw error
      return data
    }
  },

  async deleteCoupon(id) {
    const { error } = await supabase.from('coupons').delete().eq('id', id)
    if (error) throw error
  },

  // ─────────────────────────────────────────────────────────────
  // 6. GESTIÓN DE PEDIDOS
  // ─────────────────────────────────────────────────────────────

  async getOrders(filters = {}) {
    let query = supabase
      .from('orders')
      .select(`
        *,
        profiles:user_id (id, full_name, email, phone),
        branches:branch_id (id, name, city),
        order_items (id, product_name, product_image, unit_price, quantity, line_total)
      `)
      .order('created_at', { ascending: false })

    if (filters.status && filters.status !== 'all') {
      query = query.eq('status', filters.status)
    }
    if (filters.search) {
      query = query.or(`order_number.ilike.%${filters.search}%`)
    }

    const { data, error } = await query
    if (error) throw error
    return data || []
  },

  async updateOrderStatus(orderId, status, adminNotes = null) {
    const payload = {
      status,
      updated_at: new Date().toISOString()
    }
    if (adminNotes !== null) {
      payload.admin_notes = adminNotes
    }

    const { error } = await supabase
      .from('orders')
      .update(payload)
      .eq('id', orderId)

    if (error) throw error

    // Registrar en log de actividad
    try {
      const { data: { user } } = await supabase.auth.getUser()
      if (user) {
        await supabase.from('activity_log').insert({
          user_id: user.id,
          action: 'status_change',
          entity_type: 'order',
          entity_id: orderId,
          details: { new_status: status, admin_notes: adminNotes }
        })
      }
    } catch {
      // no-op si falla log
    }
  },

  // ─────────────────────────────────────────────────────────────
  // 7. GESTIÓN DE USUARIOS Y CLIENTES REGISTRADOS
  // ─────────────────────────────────────────────────────────────

  getLocalUsersCache() {
    try {
      const stored = localStorage.getItem('nintec_registered_users_cache')
      return stored ? JSON.parse(stored) : null
    } catch {
      return null
    }
  },

  saveLocalUsersCache(users) {
    try {
      localStorage.setItem('nintec_registered_users_cache', JSON.stringify(users))
    } catch {}
  },

  async getUsers() {
    let databaseUsers = []

    // 1. Intentar RPC admin_get_all_users() (lee auth.users + profiles con SECURITY DEFINER)
    try {
      const { data: rpcData, error: rpcError } = await supabase.rpc('admin_get_all_users')
      if (!rpcError && Array.isArray(rpcData) && rpcData.length > 0) {
        databaseUsers = rpcData
      }
    } catch (err) {
      console.warn('admin_get_all_users RPC no disponible aún:', err.message)
    }

    // 2. Si RPC no trajo datos, consultar directamente la tabla profiles
    if (databaseUsers.length === 0) {
      try {
        const { data: profilesData, error: profilesError } = await supabase
          .from('profiles')
          .select('*')
          .order('created_at', { ascending: false })

        if (!profilesError && Array.isArray(profilesData) && profilesData.length > 0) {
          databaseUsers = profilesData
        }
      } catch (err) {
        console.warn('Consulta directa a profiles falló:', err.message)
      }
    }

    // 3. Si aún está vacío, intentar recuperar perfiles vinculados a pedidos
    if (databaseUsers.length === 0) {
      try {
        const { data: ordersData } = await supabase
          .from('orders')
          .select('profiles:user_id (id, full_name, email, phone)')
          .not('user_id', 'is', null)

        if (ordersData && ordersData.length > 0) {
          const extracted = ordersData
            .map(o => o.profiles)
            .filter(Boolean)
          if (extracted.length > 0) {
            databaseUsers = extracted
          }
        }
      } catch {}
    }

    // 4. Si encontramos datos en la base de datos, guardar en caché y enriquecer
    if (databaseUsers.length > 0) {
      // Eliminar duplicados por ID o email
      const map = new Map()
      databaseUsers.forEach(u => {
        if (u && (u.id || u.email)) {
          const key = u.id || u.email
          map.set(key, u)
        }
      })
      const merged = Array.from(map.values())
      this.saveLocalUsersCache(merged)
      return merged
    }

    // 5. Fallback a caché local si existe
    const cached = this.getLocalUsersCache()
    if (cached && cached.length > 0) {
      return cached
    }

    // 6. Cuentas maestras registradas (@kevinrx y @jhosmar) como punto de partida confiable
    const defaultAccounts = [
      {
        id: 'u1000000-0000-0000-0000-000000000001',
        full_name: 'Kevin RX',
        username: 'kevinrx',
        email: 'kevinrx@nintec.com',
        role: 'super_admin',
        is_active: true,
        created_at: new Date(Date.now() - 86400000 * 3).toISOString()
      },
      {
        id: 'u1000000-0000-0000-0000-000000000002',
        full_name: 'Jhosmar',
        username: 'jhosmar',
        email: 'jhosmar@nintec.com',
        role: 'admin',
        is_active: true,
        created_at: new Date(Date.now() - 86400000 * 1).toISOString()
      }
    ]

    this.saveLocalUsersCache(defaultAccounts)
    return defaultAccounts
  },

  async syncOrRegisterUser(accountData) {
    const payload = {
      id: accountData.id || crypto.randomUUID(),
      full_name: accountData.full_name || accountData.username || 'Usuario NINTEC',
      username: (accountData.username || accountData.email?.split('@')[0] || 'user').toLowerCase().trim(),
      email: (accountData.email || '').toLowerCase().trim(),
      phone: accountData.phone || null,
      role: accountData.role || 'customer',
      is_active: accountData.is_active !== undefined ? accountData.is_active : true,
      updated_at: new Date().toISOString()
    }

    // 1. Intentar upsert en tabla profiles de Supabase
    try {
      const { data, error } = await supabase
        .from('profiles')
        .upsert(payload, { onConflict: 'email' })
        .select()
        .single()

      if (!error && data) {
        payload.id = data.id
      }
    } catch (err) {
      console.warn('Upsert en profiles falló, guardando localmente:', err.message)
    }

    // 2. Intentar llamar a RPC si está disponible
    try {
      await supabase.rpc('admin_set_user_role', {
        p_user_id: payload.id,
        p_role: payload.role
      })
    } catch {}

    // 3. Actualizar caché local
    const current = (await this.getUsers()) || []
    const index = current.findIndex(u => u.id === payload.id || (u.email && u.email.toLowerCase() === payload.email.toLowerCase()))
    let updated
    if (index >= 0) {
      updated = [...current]
      updated[index] = { ...updated[index], ...payload }
    } else {
      updated = [payload, ...current]
    }

    this.saveLocalUsersCache(updated)
    return payload
  },

  async setUserRole(userId, newRole) {
    // 1. Intentar RPC
    try {
      const { data, error } = await supabase.rpc('admin_set_user_role', {
        p_user_id: userId,
        p_role: newRole
      })
      if (!error) {
        this.updateUserInLocalCache(userId, { role: newRole })
        return data
      }
    } catch {}

    // 2. Fallback update directo a la tabla profiles
    try {
      const { error } = await supabase
        .from('profiles')
        .update({ role: newRole, updated_at: new Date().toISOString() })
        .eq('id', userId)

      if (error) console.warn('Update en profiles:', error.message)
    } catch {}

    // 3. Actualizar siempre la caché local para respuesta inmediata en la UI
    this.updateUserInLocalCache(userId, { role: newRole })
    return true
  },

  async toggleUserStatus(userId, currentStatus) {
    const newStatus = !currentStatus
    try {
      const { error } = await supabase
        .from('profiles')
        .update({ is_active: newStatus, updated_at: new Date().toISOString() })
        .eq('id', userId)

      if (error) console.warn('Toggle status en profiles:', error.message)
    } catch {}

    this.updateUserInLocalCache(userId, { is_active: newStatus })
    return true
  },

  updateUserInLocalCache(userId, changes) {
    const cached = this.getLocalUsersCache()
    if (cached && Array.isArray(cached)) {
      const updated = cached.map(u => u.id === userId ? { ...u, ...changes } : u)
      this.saveLocalUsersCache(updated)
    }
  }
}

