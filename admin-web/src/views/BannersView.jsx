import React, { useState, useEffect } from 'react'
import {
  Image as ImageIcon,
  Plus,
  Edit2,
  Trash2,
  Smartphone,
  Calendar,
  Link as LinkIcon,
  ArrowUpRight,
  Eye,
  Check,
  Sparkles
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'
import ImageUploader from '../components/ImageUploader'

export default function BannersView() {
  const [banners, setBanners] = useState([])
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)

  // Modal & Form State
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [editingBanner, setEditingBanner] = useState(null)
  const [saving, setSaving] = useState(false)
  const [selectedPreviewBanner, setSelectedPreviewBanner] = useState(null)

  const [formData, setFormData] = useState({
    id: null,
    title: '',
    subtitle: '',
    image_url: '',
    action_type: 'none',
    action_value: '',
    background_color: '#1846D7',
    text_color: '#FFFFFF',
    sort_order: 0,
    is_active: true,
    starts_at: '',
    ends_at: ''
  })

  const loadData = async () => {
    setLoading(true)
    try {
      const [bannersData, productsData, categoriesData] = await Promise.all([
        adminService.getBanners(),
        adminService.getProducts(),
        adminService.getCategories()
      ])
      setBanners(bannersData)
      setProducts(productsData)
      setCategories(categoriesData)
      if (bannersData.length > 0 && !selectedPreviewBanner) {
        setSelectedPreviewBanner(bannersData[0])
      }
    } catch (err) {
      console.error('Error al cargar banners:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleOpenCreate = () => {
    setEditingBanner(null)
    setFormData({
      id: null,
      title: '',
      subtitle: '',
      image_url: '',
      action_type: 'none',
      action_value: '',
      background_color: '#1846D7',
      text_color: '#FFFFFF',
      sort_order: banners.length + 1,
      is_active: true,
      starts_at: '',
      ends_at: ''
    })
    setIsModalOpen(true)
  }

  const handleOpenEdit = (b) => {
    setEditingBanner(b)
    setFormData({
      id: b.id,
      title: b.title,
      subtitle: b.subtitle || '',
      image_url: b.image_url,
      action_type: b.action_type || 'none',
      action_value: b.action_value || '',
      background_color: b.background_color || '#1846D7',
      text_color: b.text_color || '#FFFFFF',
      sort_order: b.sort_order || 0,
      is_active: b.is_active,
      starts_at: b.starts_at ? b.starts_at.slice(0, 10) : '',
      ends_at: b.ends_at ? b.ends_at.slice(0, 10) : ''
    })
    setIsModalOpen(true)
  }

  const handleSaveBanner = async (e) => {
    e.preventDefault()
    if (!formData.title || !formData.image_url) {
      alert('Por favor ingresa un título y una imagen para el banner.')
      return
    }

    setSaving(true)
    try {
      await adminService.saveBanner(formData)
      setIsModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error guardando banner: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id, title) => {
    if (confirm(`¿Estás seguro de que deseas eliminar el banner "${title}"?`)) {
      try {
        await adminService.deleteBanner(id)
        await loadData()
      } catch (err) {
        alert('Error al eliminar banner: ' + err.message)
      }
    }
  }

  const handleToggleActive = async (id, currentStatus) => {
    try {
      await adminService.toggleBannerStatus(id, currentStatus)
      await loadData()
    } catch (err) {
      alert('Error al cambiar estado del banner: ' + err.message)
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }} className="view-enter">
      {/* Header Info & Actions */}
      <div className="toolbar">
        <div>
          <h2 className="brand-font" style={{ fontSize: '1.25rem', color: '#FFFFFF', margin: 0 }}>
            Carrusel de Inicio de la App Android
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', margin: 0 }}>
            Configura los anuncios y promociones que ven los clientes al abrir la app móvil.
          </p>
        </div>

        <button onClick={handleOpenCreate} className="btn btn-primary">
          <Plus size={18} />
          <span>Nuevo Banner</span>
        </button>
      </div>

      {/* Grid: Banners List + Live Phone Mockup Preview */}
      <div style={{ display: 'grid', gridTemplateColumns: '1.7fr 1.1fr', gap: '28px' }}>
        {/* Left Column: Banners Grid */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {banners.length > 0 ? (
            banners.map((b) => {
              const isSelected = selectedPreviewBanner?.id === b.id
              return (
                <div
                  key={b.id}
                  onClick={() => setSelectedPreviewBanner(b)}
                  className="card"
                  style={{
                    cursor: 'pointer',
                    borderColor: isSelected ? 'var(--primary)' : 'var(--border-subtle)',
                    background: isSelected ? 'rgba(24, 70, 215, 0.08)' : 'var(--bg-card)',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '14px',
                    position: 'relative'
                  }}
                >
                  <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
                    <div style={{
                      width: '130px',
                      height: '75px',
                      borderRadius: 'var(--radius-md)',
                      overflow: 'hidden',
                      background: b.background_color || '#1846D7',
                      flexShrink: 0,
                      border: '1px solid var(--border-medium)',
                      position: 'relative'
                    }}>
                      <img
                        src={b.image_url}
                        alt={b.title}
                        style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                        onError={(e) => {
                          e.currentTarget.style.display = 'none'
                        }}
                      />
                    </div>

                    <div style={{ flex: 1, minWidth: 0 }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                        <span style={{
                          background: 'rgba(255, 255, 255, 0.08)',
                          color: '#60A5FA',
                          padding: '2px 8px',
                          borderRadius: '4px',
                          fontSize: '0.72rem',
                          fontWeight: '700'
                        }}>
                          #{b.sort_order}
                        </span>
                        <h4 className="brand-font" style={{ fontSize: '1.05rem', color: '#FFFFFF', margin: 0, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {b.title}
                        </h4>
                      </div>

                      {b.subtitle && (
                        <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0, whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                          {b.subtitle}
                        </p>
                      )}

                      <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginTop: '8px' }}>
                        <span className="badge badge-info" style={{ fontSize: '0.7rem' }}>
                          Acción: {b.action_type === 'product' ? 'Abrir Producto' : b.action_type === 'category' ? 'Abrir Categoría' : b.action_type === 'url' ? 'URL Web' : 'Informativo'}
                        </span>
                        <button
                          type="button"
                          onClick={(e) => {
                            e.stopPropagation()
                            handleToggleActive(b.id, b.is_active)
                          }}
                          className={`badge ${b.is_active ? 'badge-success' : 'badge-danger'}`}
                          style={{ cursor: 'pointer', border: 'none' }}
                        >
                          {b.is_active ? 'Activo en App' : 'Inactivo'}
                        </button>
                      </div>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
                      <button
                        onClick={(e) => {
                          e.stopPropagation()
                          handleOpenEdit(b)
                        }}
                        className="btn btn-secondary btn-icon btn-sm"
                        title="Editar banner"
                      >
                        <Edit2 size={14} />
                      </button>
                      <button
                        onClick={(e) => {
                          e.stopPropagation()
                          handleDelete(b.id, b.title)
                        }}
                        className="btn btn-danger btn-icon btn-sm"
                        title="Eliminar banner"
                      >
                        <Trash2 size={14} />
                      </button>
                    </div>
                  </div>
                </div>
              )
            })
          ) : (
            <div className="card" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-dim)' }}>
              {loading ? 'Cargando banners...' : 'No hay banners configurados en el inicio. ¡Crea el primero!'}
            </div>
          )}
        </div>

        {/* Right Column: Live Mobile App Phone Simulator Preview */}
        <div>
          <div className="card" style={{ position: 'sticky', top: '100px', display: 'flex', flexDirection: 'column', alignItems: 'center' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '16px', width: '100%' }}>
              <Smartphone size={18} color="#34D399" />
              <h3 className="brand-font" style={{ fontSize: '1rem', color: '#FFFFFF', margin: 0 }}>
                Vista Previa en App Android
              </h3>
            </div>

            {/* Mobile Device Frame */}
            <div style={{
              width: '280px',
              borderRadius: '32px',
              border: '6px solid #1E293B',
              background: '#0B132B',
              overflow: 'hidden',
              boxShadow: '0 20px 40px rgba(0, 0, 0, 0.7)',
              padding: '12px'
            }}>
              {/* App Status Bar */}
              <div style={{ display: 'flex', justifyContent: 'space-between', padding: '0 6px 10px', fontSize: '0.65rem', color: 'var(--text-dim)' }}>
                <span>12:00</span>
                <span style={{ letterSpacing: '2px' }}>••• 5G 100%</span>
              </div>

              {/* App Header Search Mock */}
              <div style={{
                background: '#18223C',
                borderRadius: '8px',
                padding: '8px 12px',
                fontSize: '0.72rem',
                color: 'var(--text-dim)',
                marginBottom: '14px',
                display: 'flex',
                alignItems: 'center',
                gap: '6px'
              }}>
                <span>🔍 Buscar en NINTEC...</span>
              </div>

              {/* Banner Carousel Card inside App */}
              {selectedPreviewBanner ? (
                <div style={{
                  width: '100%',
                  aspectRatio: '16 / 9',
                  borderRadius: '14px',
                  overflow: 'hidden',
                  position: 'relative',
                  background: selectedPreviewBanner.background_color || '#1846D7',
                  boxShadow: '0 4px 12px rgba(0, 0, 0, 0.4)'
                }}>
                  <img
                    src={selectedPreviewBanner.image_url}
                    alt={selectedPreviewBanner.title}
                    style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    onError={(e) => {
                      e.currentTarget.style.display = 'none'
                    }}
                  />
                  <div style={{
                    position: 'absolute',
                    inset: 0,
                    background: 'linear-gradient(180deg, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.7) 100%)',
                    display: 'flex',
                    flexDirection: 'column',
                    justifyContent: 'flex-end',
                    padding: '12px'
                  }}>
                    <span style={{
                      fontSize: '0.85rem',
                      fontWeight: '700',
                      color: selectedPreviewBanner.text_color || '#FFFFFF',
                      fontFamily: 'Outfit, sans-serif'
                    }}>
                      {selectedPreviewBanner.title}
                    </span>
                    {selectedPreviewBanner.subtitle && (
                      <span style={{ fontSize: '0.68rem', color: 'rgba(255,255,255,0.85)' }}>
                        {selectedPreviewBanner.subtitle}
                      </span>
                    )}
                  </div>
                </div>
              ) : (
                <div style={{
                  width: '100%',
                  aspectRatio: '16 / 9',
                  borderRadius: '14px',
                  background: '#18223C',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  color: 'var(--text-dim)',
                  fontSize: '0.75rem'
                }}>
                  Selecciona un banner
                </div>
              )}

              {/* Dots indicator */}
              <div style={{ display: 'flex', justifyContent: 'center', gap: '4px', margin: '10px 0 16px' }}>
                <span style={{ width: '16px', height: '4px', borderRadius: '2px', background: '#1846D7' }}></span>
                <span style={{ width: '6px', height: '4px', borderRadius: '2px', background: 'rgba(255,255,255,0.2)' }}></span>
                <span style={{ width: '6px', height: '4px', borderRadius: '2px', background: 'rgba(255,255,255,0.2)' }}></span>
              </div>

              {/* Categories row mock */}
              <div style={{ display: 'flex', justifyContent: 'space-around', padding: '6px 0' }}>
                {['Laptops', 'Celulares', 'Audio'].map((cat, i) => (
                  <div key={i} style={{ textAlign: 'center' }}>
                    <div style={{ width: '36px', height: '36px', borderRadius: '50%', background: '#18223C', margin: '0 auto 4px' }} />
                    <span style={{ fontSize: '0.6rem', color: 'var(--text-muted)' }}>{cat}</span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Banner Create / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingBanner ? 'Editar Banner de Inicio' : 'Crear Nuevo Banner Promocional'}
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveBanner}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando en Supabase...' : 'Guardar Banner'}
            </button>
          </>
        }
      >
        <form onSubmit={handleSaveBanner}>
          <ImageUploader
            bucket="banners"
            folder="home"
            currentUrl={formData.image_url}
            onImageUploaded={(url) => setFormData({ ...formData, image_url: url })}
            onImageRemoved={() => setFormData({ ...formData, image_url: '' })}
            label="Imagen o Banner Arte (Bucket: banners)"
          />

          <div className="form-group">
            <label className="form-label">Título del Banner *</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. ¡Lanzamiento iPhone 15 Pro Titanium!"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
          </div>

          <div className="form-group">
            <label className="form-label">Subtítulo / Texto Promocional</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. Hasta 15% de descuento por tiempo limitado"
              value={formData.subtitle}
              onChange={(e) => setFormData({ ...formData, subtitle: e.target.value })}
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Acción al Tocar el Banner</label>
              <select
                className="input"
                value={formData.action_type}
                onChange={(e) => setFormData({ ...formData, action_type: e.target.value, action_value: '' })}
              >
                <option value="none">Ninguna (Solo Informativo)</option>
                <option value="product">Abrir Detalle de un Producto</option>
                <option value="category">Abrir Catálogo de Categoría</option>
                <option value="url">Abrir Enlace Web Externo</option>
              </select>
            </div>

            <div className="form-group">
              <label className="form-label">Destino de la Acción</label>
              {formData.action_type === 'product' ? (
                <select
                  className="input"
                  value={formData.action_value}
                  onChange={(e) => setFormData({ ...formData, action_value: e.target.value })}
                >
                  <option value="">Seleccionar Producto</option>
                  {products.map(p => (
                    <option key={p.id} value={p.id}>{p.name} (Bs {p.price})</option>
                  ))}
                </select>
              ) : formData.action_type === 'category' ? (
                <select
                  className="input"
                  value={formData.action_value}
                  onChange={(e) => setFormData({ ...formData, action_value: e.target.value })}
                >
                  <option value="">Seleccionar Categoría</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.slug}>{c.name}</option>
                  ))}
                </select>
              ) : formData.action_type === 'url' ? (
                <input
                  type="url"
                  className="input"
                  placeholder="https://ejemplo.com/promo"
                  value={formData.action_value}
                  onChange={(e) => setFormData({ ...formData, action_value: e.target.value })}
                />
              ) : (
                <input
                  type="text"
                  className="input"
                  disabled
                  placeholder="Sin acción configurada"
                />
              )}
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Orden en Carrusel (1 = Primero)</label>
              <input
                type="number"
                min="0"
                className="input"
                value={formData.sort_order}
                onChange={(e) => setFormData({ ...formData, sort_order: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Color de Fondo Respaldo</label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input
                  type="color"
                  value={formData.background_color}
                  onChange={(e) => setFormData({ ...formData, background_color: e.target.value })}
                  style={{ width: '45px', height: '42px', padding: '2px', background: 'none', border: 'none', cursor: 'pointer' }}
                />
                <input
                  type="text"
                  className="input"
                  value={formData.background_color}
                  onChange={(e) => setFormData({ ...formData, background_color: e.target.value })}
                />
              </div>
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Fecha Inicio (Opcional)</label>
              <input
                type="date"
                className="input"
                value={formData.starts_at}
                onChange={(e) => setFormData({ ...formData, starts_at: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Fecha Fin / Vencimiento (Opcional)</label>
              <input
                type="date"
                className="input"
                value={formData.ends_at}
                onChange={(e) => setFormData({ ...formData, ends_at: e.target.value })}
              />
            </div>
          </div>

          <div style={{ marginTop: '10px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={formData.is_active}
                onChange={(e) => setFormData({ ...formData, is_active: e.target.checked })}
                style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
              />
              <span style={{ fontWeight: '600', fontSize: '0.88rem', color: '#FFFFFF' }}>
                Banner Activo (Visible en el Home de la app)
              </span>
            </label>
          </div>
        </form>
      </Modal>
    </div>
  )
}
