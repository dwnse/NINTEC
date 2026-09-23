import React, { useState, useEffect } from 'react'
import {
  Tag,
  Percent,
  Plus,
  Edit2,
  Trash2,
  Star,
  Sparkles,
  Ticket,
  DollarSign,
  Calendar,
  Layers,
  Check,
  CheckCircle2,
  ArrowRight
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'

export default function OffersView() {
  const [subTab, setSubTab] = useState('offers') // 'offers' | 'coupons' | 'categories'
  const [products, setProducts] = useState([])
  const [coupons, setCoupons] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)

  // Modals
  const [isCouponModalOpen, setIsCouponModalOpen] = useState(false)
  const [isCategoryModalOpen, setIsCategoryModalOpen] = useState(false)
  const [saving, setSaving] = useState(false)

  // Coupon Form
  const [couponForm, setCouponForm] = useState({
    id: null,
    code: '',
    description: '',
    discount_type: 'percentage', // 'percentage' | 'fixed'
    discount_value: '',
    min_purchase: '',
    max_discount: '',
    max_uses: '',
    is_active: true,
    ends_at: ''
  })

  // Category Form
  const [categoryForm, setCategoryForm] = useState({
    id: null,
    name: '',
    slug: '',
    description: '',
    icon_name: 'ic_devices',
    sort_order: 1,
    is_active: true
  })

  const loadData = async () => {
    setLoading(true)
    try {
      const [prods, coups, cats] = await Promise.all([
        adminService.getProducts(),
        adminService.getCoupons(),
        adminService.getCategories()
      ])
      setProducts(prods)
      setCoupons(coups)
      setCategories(cats)
    } catch (err) {
      console.error('Error cargando ofertas y promociones:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  // Toggle Featured status on a product
  const handleToggleFeatured = async (product) => {
    try {
      await adminService.saveProduct({
        ...product,
        is_featured: !product.is_featured
      })
      await loadData()
    } catch (err) {
      alert('Error al actualizar estado destacado: ' + err.message)
    }
  }

  // Cupones Handlers
  const handleOpenCreateCoupon = () => {
    setCouponForm({
      id: null,
      code: '',
      description: '',
      discount_type: 'percentage',
      discount_value: '10',
      min_purchase: '100',
      max_discount: '150',
      max_uses: '100',
      is_active: true,
      ends_at: ''
    })
    setIsCouponModalOpen(true)
  }

  const handleSaveCoupon = async (e) => {
    e.preventDefault()
    if (!couponForm.code || !couponForm.discount_value) {
      alert('Ingresa el código y el valor del descuento')
      return
    }

    setSaving(true)
    try {
      await adminService.saveCoupon(couponForm)
      setIsCouponModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error guardando cupón: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteCoupon = async (id, code) => {
    if (confirm(`¿Eliminar el cupón promocional "${code}"?`)) {
      try {
        await adminService.deleteCoupon(id)
        await loadData()
      } catch (err) {
        alert('Error eliminando cupón: ' + err.message)
      }
    }
  }

  // Categorías Handlers
  const handleOpenCreateCategory = () => {
    setCategoryForm({
      id: null,
      name: '',
      slug: '',
      description: '',
      icon_name: 'ic_devices',
      sort_order: categories.length + 1,
      is_active: true
    })
    setIsCategoryModalOpen(true)
  }

  const handleOpenEditCategory = (c) => {
    setCategoryForm({
      id: c.id,
      name: c.name,
      slug: c.slug,
      description: c.description || '',
      icon_name: c.icon_name || 'ic_devices',
      sort_order: c.sort_order || 1,
      is_active: c.is_active
    })
    setIsCategoryModalOpen(true)
  }

  const handleSaveCategory = async (e) => {
    e.preventDefault()
    if (!categoryForm.name) {
      alert('Ingresa el nombre de la categoría')
      return
    }

    setSaving(true)
    try {
      await adminService.saveCategory(categoryForm)
      setIsCategoryModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error guardando categoría: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteCategory = async (id, name) => {
    if (confirm(`¿Eliminar la categoría "${name}"?`)) {
      try {
        await adminService.deleteCategory(id)
        await loadData()
      } catch (err) {
        alert('Error eliminando categoría: ' + err.message)
      }
    }
  }

  // Productos con oferta o destacados
  const discountedProducts = products.filter(p => (p.old_price && p.old_price > p.price) || p.is_featured)

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }} className="view-enter">
      {/* Sub Tabs Selector */}
      <div style={{
        display: 'flex',
        gap: '12px',
        borderBottom: '1px solid var(--border-subtle)',
        paddingBottom: '14px'
      }}>
        <button
          onClick={() => setSubTab('offers')}
          className={`btn ${subTab === 'offers' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Percent size={16} />
          <span>Ofertas Especiales y Destacados ({discountedProducts.length})</span>
        </button>

        <button
          onClick={() => setSubTab('coupons')}
          className={`btn ${subTab === 'coupons' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Ticket size={16} />
          <span>Cupones de Descuento ({coupons.length})</span>
        </button>

        <button
          onClick={() => setSubTab('categories')}
          className={`btn ${subTab === 'categories' ? 'btn-primary' : 'btn-secondary'}`}
        >
          <Layers size={16} />
          <span>Categorías del Catálogo ({categories.length})</span>
        </button>
      </div>

      {/* SubTab 1: Ofertas Especiales y Destacados */}
      {subTab === 'offers' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="card" style={{ background: 'linear-gradient(135deg, rgba(24, 70, 215, 0.12) 0%, rgba(139, 92, 246, 0.08) 100%)' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', color: '#60A5FA', marginBottom: '8px' }}>
              <Sparkles size={20} />
              <h3 className="brand-font" style={{ fontSize: '1.15rem', color: '#FFFFFF', margin: 0 }}>
                Control de Ofertas Especiales para la App Móvil
              </h3>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', lineHeight: '1.5', margin: 0 }}>
              Los productos marcados como <strong>Destacados</strong> aparecen en el carrusel de Ofertas de la pantalla de inicio de los clientes. Los productos con <strong>Precio Anterior</strong> muestran el porcentaje de rebaja tachado.
            </p>
          </div>

          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Artículo</th>
                  <th>Precio Venta</th>
                  <th>Precio Anterior</th>
                  <th>Rebaja (%)</th>
                  <th>Destacado en Home</th>
                  <th style={{ textAlign: 'right' }}>Acción Rápida</th>
                </tr>
              </thead>
              <tbody>
                {discountedProducts.length > 0 ? (
                  discountedProducts.map(p => {
                    const hasDiscount = p.old_price && Number(p.old_price) > Number(p.price)
                    const discountPct = hasDiscount
                      ? Math.round(((p.old_price - p.price) / p.old_price) * 100)
                      : 0

                    return (
                      <tr key={p.id}>
                        <td style={{ fontWeight: '600', color: '#FFFFFF' }}>{p.name}</td>
                        <td style={{ fontWeight: '700', color: '#34D399' }}>
                          Bs {Number(p.price).toFixed(2)}
                        </td>
                        <td style={{ color: 'var(--text-dim)', textDecoration: 'line-through' }}>
                          {p.old_price ? `Bs ${Number(p.old_price).toFixed(2)}` : '—'}
                        </td>
                        <td>
                          {hasDiscount ? (
                            <span className="badge badge-warning">
                              -{discountPct}% OFF
                            </span>
                          ) : (
                            <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Sin rebaja</span>
                          )}
                        </td>
                        <td>
                          <button
                            onClick={() => handleToggleFeatured(p)}
                            className={`badge ${p.is_featured ? 'badge-info' : 'badge-secondary'}`}
                            style={{ cursor: 'pointer', border: 'none', display: 'inline-flex', alignItems: 'center', gap: '4px' }}
                          >
                            <Star size={11} fill={p.is_featured ? '#38BDF8' : 'none'} />
                            <span>{p.is_featured ? 'En Home Ofertas' : 'No destacado'}</span>
                          </button>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            onClick={() => handleToggleFeatured(p)}
                            className="btn btn-secondary btn-sm"
                          >
                            {p.is_featured ? 'Quitar de Home' : 'Fijar en Home'}
                          </button>
                        </td>
                      </tr>
                    )
                  })
                ) : (
                  <tr>
                    <td colSpan="6" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                      No hay artículos con ofertas especiales activas. Puedes editarlos en "Artículos e Imágenes" y asignarles un precio anterior.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SubTab 2: Cupones de Descuento */}
      {subTab === 'coupons' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="toolbar" style={{ margin: 0 }}>
            <div>
              <h3 className="brand-font" style={{ fontSize: '1.15rem', color: '#FFFFFF', margin: 0 }}>
                Cupones de Descuento para Carrito
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', margin: 0 }}>
                Los usuarios pueden canjear estos códigos en el carrito de compras de la app.
              </p>
            </div>

            <button onClick={handleOpenCreateCoupon} className="btn btn-primary">
              <Plus size={18} />
              <span>Nuevo Cupón</span>
            </button>
          </div>

          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Código de Cupón</th>
                  <th>Descripción</th>
                  <th>Descuento</th>
                  <th>Compra Mínima</th>
                  <th>Usos Realizados</th>
                  <th>Estado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {coupons.length > 0 ? (
                  coupons.map(c => (
                    <tr key={c.id}>
                      <td>
                        <span style={{
                          fontFamily: 'monospace',
                          fontSize: '0.95rem',
                          fontWeight: '700',
                          color: '#60A5FA',
                          background: 'rgba(24, 70, 215, 0.15)',
                          padding: '4px 10px',
                          borderRadius: '6px',
                          border: '1px solid rgba(24, 70, 215, 0.3)'
                        }}>
                          {c.code}
                        </span>
                      </td>
                      <td style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
                        {c.description || 'Promoción general'}
                      </td>
                      <td>
                        <span className="badge badge-success">
                          {c.discount_type === 'percentage'
                            ? `${c.discount_value}% OFF`
                            : `Bs ${c.discount_value} OFF`}
                        </span>
                      </td>
                      <td>
                        Bs {Number(c.min_purchase || 0).toFixed(2)}
                      </td>
                      <td>
                        <span style={{ fontSize: '0.82rem', color: 'var(--text-dim)' }}>
                          {c.used_count || 0} {c.max_uses ? `/ ${c.max_uses}` : '(Ilimitado)'}
                        </span>
                      </td>
                      <td>
                        <span className={`badge ${c.is_active ? 'badge-success' : 'badge-danger'}`}>
                          {c.is_active ? 'Vigente' : 'Inactivo'}
                        </span>
                      </td>
                      <td style={{ textAlign: 'right' }}>
                        <button
                          onClick={() => handleDeleteCoupon(c.id, c.code)}
                          className="btn btn-danger btn-icon btn-sm"
                          title="Eliminar cupón"
                        >
                          <Trash2 size={14} />
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan="7" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                      No hay cupones creados. Crea cupones como 'BIENVENIDO10' o 'NINTEC50'.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* SubTab 3: Categorías */}
      {subTab === 'categories' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div className="toolbar" style={{ margin: 0 }}>
            <div>
              <h3 className="brand-font" style={{ fontSize: '1.15rem', color: '#FFFFFF', margin: 0 }}>
                Categorías de Productos
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.8rem', margin: 0 }}>
                Estructura de secciones navegables en la barra de categorías de la app móvil.
              </p>
            </div>

            <button onClick={handleOpenCreateCategory} className="btn btn-primary">
              <Plus size={18} />
              <span>Nueva Categoría</span>
            </button>
          </div>

          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Orden</th>
                  <th>Nombre Categoría</th>
                  <th>Slug (Ruta)</th>
                  <th>Icono App</th>
                  <th>Estado</th>
                  <th style={{ textAlign: 'right' }}>Acciones</th>
                </tr>
              </thead>
              <tbody>
                {categories.map((c, idx) => (
                  <tr key={c.id}>
                    <td style={{ fontWeight: '600', color: '#60A5FA' }}>#{c.sort_order || idx + 1}</td>
                    <td style={{ fontWeight: '600', color: '#FFFFFF' }}>{c.name}</td>
                    <td style={{ fontFamily: 'monospace', color: 'var(--text-dim)' }}>{c.slug}</td>
                    <td>
                      <code style={{ background: '#18223C', padding: '3px 8px', borderRadius: '4px', color: '#93C5FD', fontSize: '0.75rem' }}>
                        {c.icon_name || 'ic_devices'}
                      </code>
                    </td>
                    <td>
                      <span className={`badge ${c.is_active ? 'badge-success' : 'badge-danger'}`}>
                        {c.is_active ? 'Visible en App' : 'Oculta'}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '6px' }}>
                        <button
                          onClick={() => handleOpenEditCategory(c)}
                          className="btn btn-secondary btn-icon btn-sm"
                        >
                          <Edit2 size={13} />
                        </button>
                        <button
                          onClick={() => handleDeleteCategory(c.id, c.name)}
                          className="btn btn-danger btn-icon btn-sm"
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal: Create Coupon */}
      <Modal
        isOpen={isCouponModalOpen}
        onClose={() => setIsCouponModalOpen(false)}
        title="Crear Nuevo Cupón de Descuento"
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsCouponModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveCoupon}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando...' : 'Crear Cupón'}
            </button>
          </>
        }
      >
        <form onSubmit={handleSaveCoupon}>
          <div className="form-group">
            <label className="form-label">Código del Cupón (Mayúsculas) *</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. NINTEC20, VERANO2026"
              value={couponForm.code}
              onChange={(e) => setCouponForm({ ...couponForm, code: e.target.value.toUpperCase() })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Descripción</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. 20% de descuento en tu primera compra"
              value={couponForm.description}
              onChange={(e) => setCouponForm({ ...couponForm, description: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Tipo de Descuento</label>
              <select
                className="input"
                value={couponForm.discount_type}
                onChange={(e) => setCouponForm({ ...couponForm, discount_type: e.target.value })}
              >
                <option value="percentage">Porcentaje (%)</option>
                <option value="fixed">Monto Fijo en Bolivianos (Bs)</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Valor del Descuento *</label>
              <input
                type="number"
                step="0.5"
                className="input"
                placeholder="Ej. 10 para 10% o 50 para Bs 50"
                value={couponForm.discount_value}
                onChange={(e) => setCouponForm({ ...couponForm, discount_value: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Compra Mínima Requerida (Bs)</label>
              <input
                type="number"
                className="input"
                placeholder="Ej. 100 (0 = sin mínimo)"
                value={couponForm.min_purchase}
                onChange={(e) => setCouponForm({ ...couponForm, min_purchase: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Descuento Tope Máximo (Bs)</label>
              <input
                type="number"
                className="input"
                placeholder="Ej. 250 (Para porcentaje)"
                value={couponForm.max_discount}
                onChange={(e) => setCouponForm({ ...couponForm, max_discount: e.target.value })}
              />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Límite Total de Usos</label>
              <input
                type="number"
                className="input"
                placeholder="Ej. 100 (Vacío = ilimitado)"
                value={couponForm.max_uses}
                onChange={(e) => setCouponForm({ ...couponForm, max_uses: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Fecha de Vencimiento</label>
              <input
                type="date"
                className="input"
                value={couponForm.ends_at}
                onChange={(e) => setCouponForm({ ...couponForm, ends_at: e.target.value })}
              />
            </div>
          </div>
        </form>
      </Modal>

      {/* Modal: Category */}
      <Modal
        isOpen={isCategoryModalOpen}
        onClose={() => setIsCategoryModalOpen(false)}
        title={categoryForm.id ? 'Editar Categoría' : 'Nueva Categoría'}
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsCategoryModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveCategory}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando...' : 'Guardar Categoría'}
            </button>
          </>
        }
      >
        <form onSubmit={handleSaveCategory}>
          <div className="form-group">
            <label className="form-label">Nombre de Categoría *</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. Laptops, Celulares, Consolas"
              value={categoryForm.name}
              onChange={(e) => setCategoryForm({ ...categoryForm, name: e.target.value })}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Identificador / Icono Android</label>
              <input
                type="text"
                className="input"
                placeholder="Ej. ic_laptop, ic_phone_android, ic_headset"
                value={categoryForm.icon_name}
                onChange={(e) => setCategoryForm({ ...categoryForm, icon_name: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Orden de Visualización</label>
              <input
                type="number"
                className="input"
                value={categoryForm.sort_order}
                onChange={(e) => setCategoryForm({ ...categoryForm, sort_order: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Descripción</label>
            <textarea
              className="input"
              rows="2"
              placeholder="Descripción breve de la categoría..."
              value={categoryForm.description}
              onChange={(e) => setCategoryForm({ ...categoryForm, description: e.target.value })}
            />
          </div>
        </form>
      </Modal>
    </div>
  )
}
