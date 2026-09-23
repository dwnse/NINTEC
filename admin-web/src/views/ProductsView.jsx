import React, { useState, useEffect } from 'react'
import {
  Package,
  Plus,
  Search,
  Filter,
  Edit2,
  Trash2,
  Eye,
  EyeOff,
  Star,
  Sparkles,
  Check,
  X,
  Layers,
  Image as ImageIcon,
  Sliders,
  Store
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'
import ImageUploader from '../components/ImageUploader'

export default function ProductsView() {
  const [products, setProducts] = useState([])
  const [categories, setCategories] = useState([])
  const [branches, setBranches] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [selectedCategory, setSelectedCategory] = useState('')

  // Modal State
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [activeTab, setActiveTab] = useState('general') // 'general' | 'pricing' | 'images' | 'specs' | 'stock'
  const [saving, setSaving] = useState(false)
  const [editingProduct, setEditingProduct] = useState(null)

  // Form State
  const [formData, setFormData] = useState({
    id: null,
    name: '',
    category_id: '',
    sku: '',
    brand: '',
    description: '',
    weight_kg: '',
    price: '',
    old_price: '',
    is_new: false,
    is_featured: false,
    is_active: true
  })
  const [productImages, setProductImages] = useState([])
  const [productSpecs, setProductSpecs] = useState([])
  const [branchStockData, setBranchStockData] = useState([])

  const loadData = async () => {
    setLoading(true)
    try {
      const [prods, cats, brs] = await Promise.all([
        adminService.getProducts(),
        adminService.getCategories(),
        adminService.getBranches()
      ])
      setProducts(prods)
      setCategories(cats)
      setBranches(brs)
    } catch (err) {
      console.error('Error cargando artículos:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleOpenCreateModal = () => {
    setEditingProduct(null)
    setFormData({
      id: null,
      name: '',
      category_id: categories[0]?.id || '',
      sku: '',
      brand: '',
      description: '',
      weight_kg: '',
      price: '',
      old_price: '',
      is_new: false,
      is_featured: false,
      is_active: true
    })
    setProductImages([])
    setProductSpecs([
      { key: 'Garantía', value: '12 Meses Oficial' }
    ])
    setBranchStockData(branches.map(b => ({
      branch_id: b.id,
      branch_name: b.name,
      stock: 10,
      min_stock: 3
    })))
    setActiveTab('general')
    setIsModalOpen(true)
  }

  const handleOpenEditModal = async (product) => {
    setEditingProduct(product)
    try {
      const details = await adminService.getProductDetails(product.id)
      setFormData({
        id: details.id,
        name: details.name,
        category_id: details.category_id,
        sku: details.sku || '',
        brand: details.brand || '',
        description: details.description || '',
        weight_kg: details.weight_kg || '',
        price: details.price,
        old_price: details.old_price || '',
        is_new: details.is_new,
        is_featured: details.is_featured,
        is_active: details.is_active
      })

      // Imágenes
      setProductImages((details.product_images || []).map(img => ({
        id: img.id,
        url: img.image_url,
        is_primary: img.is_primary,
        isNew: false
      })))

      // Specs
      setProductSpecs((details.product_specifications || []).map(s => ({
        key: s.spec_key,
        value: s.spec_value
      })))

      // Stock por Sucursal
      setBranchStockData(branches.map(b => {
        const found = (details.branch_inventory || []).find(bi => bi.branch_id === b.id)
        return {
          branch_id: b.id,
          branch_name: b.name,
          stock: found ? found.stock : 0,
          min_stock: found ? found.min_stock : 3
        }
      }))

      setActiveTab('general')
      setIsModalOpen(true)
    } catch (err) {
      alert('Error cargando detalles del producto: ' + err.message)
    }
  }

  const handleSaveProduct = async (e) => {
    e.preventDefault()
    if (!formData.name || !formData.price || !formData.category_id) {
      alert('Por favor completa nombre, categoría y precio')
      return
    }

    setSaving(true)
    try {
      await adminService.saveProduct(formData, productImages, productSpecs, branchStockData)
      setIsModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error al guardar el artículo: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDelete = async (id, name) => {
    if (confirm(`¿Estás seguro de que deseas eliminar el producto "${name}"? Esta acción borrará sus imágenes e inventario.`)) {
      try {
        await adminService.deleteProduct(id)
        await loadData()
      } catch (err) {
        alert('Error al eliminar producto: ' + err.message)
      }
    }
  }

  const handleToggleActive = async (id, currentStatus) => {
    try {
      await adminService.toggleProductStatus(id, currentStatus)
      await loadData()
    } catch (err) {
      alert('Error al actualizar estado: ' + err.message)
    }
  }

  // Especificaciones helpers
  const handleAddSpec = () => {
    setProductSpecs([...productSpecs, { key: '', value: '' }])
  }
  const handleSpecChange = (index, field, val) => {
    const updated = [...productSpecs]
    updated[index][field] = val
    setProductSpecs(updated)
  }
  const handleRemoveSpec = (index) => {
    setProductSpecs(productSpecs.filter((_, i) => i !== index))
  }

  // Filtrado de productos
  const filteredProducts = products.filter(p => {
    const matchesSearch = !searchTerm ||
      p.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (p.sku && p.sku.toLowerCase().includes(searchTerm.toLowerCase()))
    const matchesCat = !selectedCategory || p.category_id === selectedCategory
    return matchesSearch && matchesCat
  })

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }} className="view-enter">
      {/* Action Toolbar */}
      <div className="toolbar">
        <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap', flex: 1 }}>
          <div className="search-box">
            <Search size={18} />
            <input
              type="text"
              className="input"
              placeholder="Buscar por nombre o código SKU..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              className="input"
              value={selectedCategory}
              onChange={(e) => setSelectedCategory(e.target.value)}
            >
              <option value="">Todas las Categorías</option>
              {categories.map(c => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
        </div>

        <button onClick={handleOpenCreateModal} className="btn btn-primary">
          <Plus size={18} />
          <span>Nuevo Artículo</span>
        </button>
      </div>

      {/* Products Table Card */}
      <div className="table-responsive">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Artículo</th>
              <th>Categoría</th>
              <th>Precio (Bs)</th>
              <th>Descuento / Oferta</th>
              <th>Destacado</th>
              <th>Stock Total</th>
              <th>Estado</th>
              <th style={{ textAlign: 'right' }}>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.length > 0 ? (
              filteredProducts.map((product) => {
                const primaryImage = product.product_images?.find(i => i.is_primary)?.image_url ||
                                     product.product_images?.[0]?.image_url ||
                                     'https://placehold.co/100x100/18223C/FFFFFF?text=Sin+Foto'
                const totalStock = (product.branch_inventory || []).reduce((acc, curr) => acc + (curr.stock || 0), 0)
                const hasDiscount = product.old_price && Number(product.old_price) > Number(product.price)
                const discountPct = hasDiscount
                  ? Math.round(((product.old_price - product.price) / product.old_price) * 100)
                  : 0

                return (
                  <tr key={product.id}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                        <img
                          src={primaryImage}
                          alt={product.name}
                          style={{
                            width: '46px',
                            height: '46px',
                            borderRadius: 'var(--radius-sm)',
                            objectFit: 'cover',
                            background: '#0D1527',
                            border: '1px solid var(--border-subtle)'
                          }}
                        />
                        <div>
                          <div style={{ fontWeight: '600', color: '#FFFFFF' }}>{product.name}</div>
                          <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)' }}>
                            SKU: {product.sku || 'N/A'} {product.brand ? `• ${product.brand}` : ''}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td>
                      <span className="badge badge-primary">
                        {product.categories?.name || 'General'}
                      </span>
                    </td>
                    <td>
                      <div style={{ fontWeight: '700', color: '#FFFFFF' }}>
                        Bs {Number(product.price).toFixed(2)}
                      </div>
                      {hasDiscount && (
                        <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)', textDecoration: 'line-through' }}>
                          Bs {Number(product.old_price).toFixed(2)}
                        </div>
                      )}
                    </td>
                    <td>
                      {hasDiscount ? (
                        <span className="badge badge-warning">
                          -{discountPct}% OFF
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Normal</span>
                      )}
                    </td>
                    <td>
                      {product.is_featured ? (
                        <span className="badge badge-info" style={{ display: 'inline-flex', alignItems: 'center', gap: '4px' }}>
                          <Star size={11} fill="#38BDF8" /> Destacado
                        </span>
                      ) : (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>No</span>
                      )}
                    </td>
                    <td>
                      <span className={`badge ${totalStock > 5 ? 'badge-success' : totalStock > 0 ? 'badge-warning' : 'badge-danger'}`}>
                        {totalStock} unid.
                      </span>
                    </td>
                    <td>
                      <button
                        onClick={() => handleToggleActive(product.id, product.is_active)}
                        className={`badge ${product.is_active ? 'badge-success' : 'badge-danger'}`}
                        style={{ cursor: 'pointer', border: 'none' }}
                        title="Clic para cambiar estado"
                      >
                        {product.is_active ? 'Activo en App' : 'Inactivo'}
                      </button>
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <div style={{ display: 'inline-flex', gap: '6px' }}>
                        <button
                          onClick={() => handleOpenEditModal(product)}
                          className="btn btn-secondary btn-icon btn-sm"
                          title="Editar producto"
                        >
                          <Edit2 size={14} />
                        </button>
                        <button
                          onClick={() => handleDelete(product.id, product.name)}
                          className="btn btn-danger btn-icon btn-sm"
                          title="Eliminar producto"
                        >
                          <Trash2 size={14} />
                        </button>
                      </div>
                    </td>
                  </tr>
                )
              })
            ) : (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                  {loading ? 'Cargando catálogo de artículos...' : 'No se encontraron artículos con los filtros aplicados.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Product Create / Edit Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingProduct ? `Editar Artículo: ${editingProduct.name}` : 'Crear Nuevo Artículo'}
        maxWidth="760px"
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
              onClick={handleSaveProduct}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando en Supabase...' : 'Guardar Artículo'}
            </button>
          </>
        }
      >
        {/* Navigation Tabs inside Modal */}
        <div style={{
          display: 'flex',
          gap: '8px',
          borderBottom: '1px solid var(--border-subtle)',
          paddingBottom: '12px',
          marginBottom: '20px',
          overflowX: 'auto'
        }}>
          {[
            { id: 'general', label: 'Datos Básicos', icon: Package },
            { id: 'pricing', label: 'Precios y Oferta', icon: Sliders },
            { id: 'images', label: `Imágenes (${productImages.length})`, icon: ImageIcon },
            { id: 'specs', label: `Especificaciones (${productSpecs.length})`, icon: Layers },
            { id: 'stock', label: 'Stock Sucursales', icon: Store }
          ].map(tab => {
            const Icon = tab.icon
            const isActive = activeTab === tab.id
            return (
              <button
                key={tab.id}
                type="button"
                onClick={() => setActiveTab(tab.id)}
                className={`btn btn-sm ${isActive ? 'btn-primary' : 'btn-secondary'}`}
                style={{ padding: '7px 12px', fontSize: '0.8rem' }}
              >
                <Icon size={14} />
                <span>{tab.label}</span>
              </button>
            )
          })}
        </div>

        {/* Tab 1: General Info */}
        {activeTab === 'general' && (
          <div>
            <div className="form-group">
              <label className="form-label">Nombre del Producto *</label>
              <input
                type="text"
                className="input"
                placeholder="Ej. MacBook Pro M3 Max 14 pulgadas"
                value={formData.name}
                onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                required
              />
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Categoría *</label>
                <select
                  className="input"
                  value={formData.category_id}
                  onChange={(e) => setFormData({ ...formData, category_id: e.target.value })}
                  required
                >
                  <option value="">Seleccionar Categoría</option>
                  {categories.map(c => (
                    <option key={c.id} value={c.id}>{c.name}</option>
                  ))}
                </select>
              </div>

              <div className="form-group">
                <label className="form-label">Marca</label>
                <input
                  type="text"
                  className="input"
                  placeholder="Ej. Apple, Samsung, Sony"
                  value={formData.brand}
                  onChange={(e) => setFormData({ ...formData, brand: e.target.value })}
                />
              </div>
            </div>

            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Código SKU</label>
                <input
                  type="text"
                  className="input"
                  placeholder="Ej. NIN-LAP-001"
                  value={formData.sku}
                  onChange={(e) => setFormData({ ...formData, sku: e.target.value })}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Peso para Envío (kg)</label>
                <input
                  type="number"
                  step="0.01"
                  className="input"
                  placeholder="Ej. 1.6"
                  value={formData.weight_kg}
                  onChange={(e) => setFormData({ ...formData, weight_kg: e.target.value })}
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Descripción Detallada</label>
              <textarea
                className="input"
                rows="4"
                placeholder="Describe las características principales para los clientes de la app móvil..."
                value={formData.description}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              />
            </div>
          </div>
        )}

        {/* Tab 2: Pricing & Flags */}
        {activeTab === 'pricing' && (
          <div>
            <div className="form-row">
              <div className="form-group">
                <label className="form-label">Precio Regular / Venta (Bs) *</label>
                <input
                  type="number"
                  step="0.50"
                  className="input"
                  placeholder="Ej. 2499.00"
                  value={formData.price}
                  onChange={(e) => setFormData({ ...formData, price: e.target.value })}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Precio Anterior / Oferta (Bs tachado)</label>
                <input
                  type="number"
                  step="0.50"
                  className="input"
                  placeholder="Ej. 2999.00 (Opcional)"
                  value={formData.old_price}
                  onChange={(e) => setFormData({ ...formData, old_price: e.target.value })}
                />
                <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)', marginTop: '4px' }}>
                  Si se especifica y es mayor al precio de venta, la app mostrará la etiqueta de descuento.
                </span>
              </div>
            </div>

            <div style={{
              background: 'rgba(255, 255, 255, 0.02)',
              border: '1px solid var(--border-subtle)',
              borderRadius: 'var(--radius-md)',
              padding: '16px',
              display: 'flex',
              flexDirection: 'column',
              gap: '14px',
              marginTop: '10px'
            }}>
              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formData.is_featured}
                  onChange={(e) => setFormData({ ...formData, is_featured: e.target.checked })}
                  style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
                />
                <div>
                  <div style={{ fontWeight: '600', fontSize: '0.88rem', color: '#FFFFFF' }}>
                    Producto Destacado (Ofertas de Inicio)
                  </div>
                  <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                    Aparecerá en el carrusel de "Ofertas Especiales" del Home en la app móvil.
                  </div>
                </div>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formData.is_new}
                  onChange={(e) => setFormData({ ...formData, is_new: e.target.checked })}
                  style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
                />
                <div>
                  <div style={{ fontWeight: '600', fontSize: '0.88rem', color: '#FFFFFF' }}>
                    Marcar como Novedad ("Nuevo")
                  </div>
                  <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                    Muestra el distintivo verde "NUEVO" en la tarjeta del producto.
                  </div>
                </div>
              </label>

              <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
                <input
                  type="checkbox"
                  checked={formData.is_active}
                  onChange={(e) => setFormData({ ...formData, is_active: e.target.checked })}
                  style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
                />
                <div>
                  <div style={{ fontWeight: '600', fontSize: '0.88rem', color: '#FFFFFF' }}>
                    Publicado / Activo
                  </div>
                  <div style={{ fontSize: '0.74rem', color: 'var(--text-muted)' }}>
                    Visible para compra en la app móvil Android.
                  </div>
                </div>
              </label>
            </div>
          </div>
        )}

        {/* Tab 3: Images */}
        {activeTab === 'images' && (
          <div>
            <ImageUploader
              bucket="product-images"
              folder="products"
              onImageUploaded={(url) => {
                setProductImages([...productImages, {
                  url,
                  is_primary: productImages.length === 0,
                  isNew: true
                }])
              }}
              label="Subir Nueva Foto al Bucket de Supabase"
            />

            <div style={{ marginTop: '20px' }}>
              <div style={{ fontSize: '0.82rem', fontWeight: '600', color: 'var(--text-muted)', marginBottom: '10px' }}>
                Galería del Producto ({productImages.length} fotos)
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(130px, 1fr))', gap: '12px' }}>
                {productImages.map((img, idx) => (
                  <div
                    key={idx}
                    style={{
                      position: 'relative',
                      borderRadius: 'var(--radius-md)',
                      overflow: 'hidden',
                      border: img.is_primary ? '2px solid var(--primary)' : '1px solid var(--border-medium)',
                      background: '#0D1527',
                      aspectRatio: '1/1'
                    }}
                  >
                    <img
                      src={img.url}
                      alt={`Foto ${idx}`}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                    {img.is_primary && (
                      <span style={{
                        position: 'absolute',
                        bottom: '4px',
                        left: '4px',
                        background: 'var(--primary)',
                        color: 'white',
                        fontSize: '0.65rem',
                        padding: '2px 6px',
                        borderRadius: '4px',
                        fontWeight: '700'
                      }}>
                        Principal
                      </span>
                    )}
                    <div style={{
                      position: 'absolute',
                      top: '4px',
                      right: '4px',
                      display: 'flex',
                      gap: '4px'
                    }}>
                      {!img.is_primary && (
                        <button
                          type="button"
                          onClick={() => {
                            const updated = productImages.map((im, i) => ({
                              ...im,
                              is_primary: i === idx
                            }))
                            setProductImages(updated)
                          }}
                          className="btn btn-secondary btn-icon btn-sm"
                          style={{ width: '24px', height: '24px', padding: 0 }}
                          title="Hacer foto principal"
                        >
                          <Star size={12} />
                        </button>
                      )}
                      <button
                        type="button"
                        onClick={() => {
                          setProductImages(productImages.filter((_, i) => i !== idx))
                        }}
                        className="btn btn-danger btn-icon btn-sm"
                        style={{ width: '24px', height: '24px', padding: 0 }}
                        title="Quitar foto"
                      >
                        <Trash2 size={12} />
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* Tab 4: Specifications */}
        {activeTab === 'specs' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px' }}>
              <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                Ficha técnica que se muestra en la pantalla de detalle del producto en la app.
              </span>
              <button
                type="button"
                onClick={handleAddSpec}
                className="btn btn-secondary btn-sm"
              >
                <Plus size={14} />
                <span>Añadir Especificación</span>
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {productSpecs.map((spec, idx) => (
                <div key={idx} style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
                  <input
                    type="text"
                    className="input"
                    placeholder="Clave (ej. Procesador, Memoria RAM)"
                    value={spec.key}
                    onChange={(e) => handleSpecChange(idx, 'key', e.target.value)}
                    style={{ flex: 1 }}
                  />
                  <input
                    type="text"
                    className="input"
                    placeholder="Valor (ej. Apple M3 Max 14-core, 36GB)"
                    value={spec.value}
                    onChange={(e) => handleSpecChange(idx, 'value', e.target.value)}
                    style={{ flex: 1.5 }}
                  />
                  <button
                    type="button"
                    onClick={() => handleRemoveSpec(idx)}
                    className="btn btn-danger btn-icon btn-sm"
                  >
                    <X size={14} />
                  </button>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Tab 5: Stock per Branch */}
        {activeTab === 'stock' && (
          <div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', marginBottom: '16px' }}>
              Define cuántas unidades físicas hay de este producto en cada sucursal de NINTEC.
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {branchStockData.map((b, idx) => (
                <div
                  key={b.branch_id}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '14px 18px',
                    background: 'rgba(255, 255, 255, 0.02)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)'
                  }}
                >
                  <div>
                    <div style={{ fontWeight: '600', color: '#FFFFFF' }}>{b.branch_name}</div>
                    <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)' }}>Sucursal física</div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Stock:</span>
                      <input
                        type="number"
                        min="0"
                        className="input"
                        style={{ width: '85px', textAlign: 'center', padding: '6px' }}
                        value={b.stock}
                        onChange={(e) => {
                          const updated = [...branchStockData]
                          updated[idx].stock = e.target.value
                          setBranchStockData(updated)
                        }}
                      />
                    </div>

                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Mínimo Alerta:</span>
                      <input
                        type="number"
                        min="1"
                        className="input"
                        style={{ width: '75px', textAlign: 'center', padding: '6px' }}
                        value={b.min_stock}
                        onChange={(e) => {
                          const updated = [...branchStockData]
                          updated[idx].min_stock = e.target.value
                          setBranchStockData(updated)
                        }}
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
