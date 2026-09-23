import React, { useState, useEffect } from 'react'
import {
  MapPin,
  Plus,
  Clock,
  Store,
  Phone,
  Mail,
  Edit2,
  Trash2,
  Save,
  CheckCircle,
  AlertTriangle,
  Search,
  ExternalLink
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'

const DAYS_OF_WEEK = [
  'Domingo', 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes', 'Sábado'
]

export default function BranchesView() {
  const [branches, setBranches] = useState([])
  const [selectedBranch, setSelectedBranch] = useState(null)
  const [stockTable, setStockTable] = useState([])
  const [stockSearch, setStockSearch] = useState('')
  const [loading, setLoading] = useState(true)
  const [savingStock, setSavingStock] = useState(false)

  // Modals
  const [isBranchModalOpen, setIsBranchModalOpen] = useState(false)
  const [isScheduleModalOpen, setIsScheduleModalOpen] = useState(false)
  const [editingBranch, setEditingBranch] = useState(null)
  const [saving, setSaving] = useState(false)

  // Branch Form
  const [branchForm, setBranchForm] = useState({
    id: null,
    name: '',
    address: '',
    city: 'La Paz',
    phone: '',
    email: '',
    latitude: -16.5000,
    longitude: -68.1500,
    is_active: true
  })

  // Schedule Form
  const [schedules, setSchedules] = useState([])

  const loadData = async () => {
    setLoading(true)
    try {
      const branchList = await adminService.getBranches()
      setBranches(branchList)
      if (branchList.length > 0) {
        const branchToSelect = selectedBranch
          ? branchList.find(b => b.id === selectedBranch.id) || branchList[0]
          : branchList[0]
        setSelectedBranch(branchToSelect)
        await loadBranchStock(branchToSelect.id)
      }
    } catch (err) {
      console.error('Error cargando sucursales:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadBranchStock = async (branchId) => {
    try {
      const items = await adminService.getBranchStockTable(branchId)
      setStockTable(items)
    } catch (err) {
      console.error('Error cargando inventario de sucursal:', err)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleSelectBranch = async (b) => {
    setSelectedBranch(b)
    await loadBranchStock(b.id)
  }

  const handleOpenCreateBranch = () => {
    setEditingBranch(null)
    setBranchForm({
      id: null,
      name: '',
      address: '',
      city: 'La Paz',
      phone: '',
      email: '',
      latitude: -16.5000,
      longitude: -68.1500,
      is_active: true
    })
    setIsBranchModalOpen(true)
  }

  const handleOpenEditBranch = (b) => {
    setEditingBranch(b)
    setBranchForm({
      id: b.id,
      name: b.name,
      address: b.address,
      city: b.city || 'La Paz',
      phone: b.phone || '',
      email: b.email || '',
      latitude: b.latitude,
      longitude: b.longitude,
      is_active: b.is_active
    })
    setIsBranchModalOpen(true)
  }

  const handleSaveBranch = async (e) => {
    e.preventDefault()
    if (!branchForm.name || !branchForm.address) {
      alert('Por favor ingresa nombre y dirección de la sucursal.')
      return
    }

    setSaving(true)
    try {
      await adminService.saveBranch(branchForm)
      setIsBranchModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error guardando sucursal: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  const handleDeleteBranch = async (id, name) => {
    if (confirm(`¿Estás seguro de que deseas eliminar la sucursal "${name}"?`)) {
      try {
        await adminService.deleteBranch(id)
        await loadData()
      } catch (err) {
        alert('Error eliminando sucursal: ' + err.message)
      }
    }
  }

  // Horarios
  const handleOpenSchedules = (b) => {
    setEditingBranch(b)
    // Inicializar los 7 días
    const existing = b.branch_schedules || []
    const week = [0, 1, 2, 3, 4, 5, 6].map(day => {
      const match = existing.find(s => s.day_of_week === day)
      return {
        day_of_week: day,
        open_time: match ? match.open_time.slice(0, 5) : '09:00',
        close_time: match ? match.close_time.slice(0, 5) : '19:00',
        is_closed: match ? match.is_closed : day === 0
      }
    })
    setSchedules(week)
    setIsScheduleModalOpen(true)
  }

  const handleSaveSchedules = async () => {
    setSaving(true)
    try {
      await adminService.saveBranch(editingBranch, schedules)
      setIsScheduleModalOpen(false)
      await loadData()
    } catch (err) {
      alert('Error guardando horarios: ' + err.message)
    } finally {
      setSaving(false)
    }
  }

  // Guardar inventario editado
  const handleStockInputChange = (productId, field, value) => {
    const updated = stockTable.map(item => {
      if (item.product_id === productId) {
        return { ...item, [field]: parseInt(value) || 0 }
      }
      return item
    })
    setStockTable(updated)
  }

  const handleSaveStockItem = async (item) => {
    try {
      await adminService.updateStock(selectedBranch.id, item.product_id, item.stock, item.min_stock)
      alert(`Stock de "${item.name}" actualizado en ${selectedBranch.name}`)
    } catch (err) {
      alert('Error al actualizar stock: ' + err.message)
    }
  }

  const handleSaveAllStock = async () => {
    setSavingStock(true)
    try {
      for (const item of stockTable) {
        await adminService.updateStock(selectedBranch.id, item.product_id, item.stock, item.min_stock)
      }
      alert('Inventario completo actualizado exitosamente')
      await loadBranchStock(selectedBranch.id)
    } catch (err) {
      alert('Error guardando inventario: ' + err.message)
    } finally {
      setSavingStock(false)
    }
  }

  const filteredStock = stockTable.filter(item =>
    item.name.toLowerCase().includes(stockSearch.toLowerCase()) ||
    item.sku.toLowerCase().includes(stockSearch.toLowerCase())
  )

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }} className="view-enter">
      {/* Top Header & Toolbar */}
      <div className="toolbar">
        <div>
          <h2 className="brand-font" style={{ fontSize: '1.25rem', color: '#FFFFFF', margin: 0 }}>
            Sucursales Físicas e Inventario
          </h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', margin: 0 }}>
            Administra los puntos de entrega física de NINTEC y las existencias por tienda.
          </p>
        </div>

        <button onClick={handleOpenCreateBranch} className="btn btn-primary">
          <Plus size={18} />
          <span>Nueva Sucursal</span>
        </button>
      </div>

      {/* Branches Horizontal Cards Bar */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
        gap: '16px'
      }}>
        {branches.map(b => {
          const isSelected = selectedBranch?.id === b.id
          return (
            <div
              key={b.id}
              onClick={() => handleSelectBranch(b)}
              className="card"
              style={{
                cursor: 'pointer',
                borderColor: isSelected ? 'var(--primary)' : 'var(--border-subtle)',
                background: isSelected ? 'rgba(24, 70, 215, 0.12)' : 'var(--bg-card)',
                transition: 'all var(--transition-fast)',
                position: 'relative'
              }}
            >
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '8px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <div style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: '8px',
                    background: isSelected ? 'var(--primary)' : 'rgba(255,255,255,0.06)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#FFFFFF'
                  }}>
                    <Store size={16} />
                  </div>
                  <div>
                    <h4 className="brand-font" style={{ fontSize: '0.98rem', color: '#FFFFFF', margin: 0 }}>
                      {b.name}
                    </h4>
                    <span style={{ fontSize: '0.72rem', color: '#60A5FA' }}>{b.city}</span>
                  </div>
                </div>

                <div style={{ display: 'flex', gap: '4px' }}>
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      handleOpenEditBranch(b)
                    }}
                    className="btn btn-secondary btn-icon btn-sm"
                    title="Editar sucursal"
                  >
                    <Edit2 size={13} />
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation()
                      handleDeleteBranch(b.id, b.name)
                    }}
                    className="btn btn-danger btn-icon btn-sm"
                    title="Eliminar sucursal"
                  >
                    <Trash2 size={13} />
                  </button>
                </div>
              </div>

              <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginBottom: '4px' }}>
                  <MapPin size={13} color="var(--text-dim)" />
                  <span style={{ whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                    {b.address}
                  </span>
                </div>
                {b.phone && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <Phone size={13} color="var(--text-dim)" />
                    <span>{b.phone}</span>
                  </div>
                )}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '10px', borderTop: '1px solid var(--border-subtle)' }}>
                <button
                  type="button"
                  onClick={(e) => {
                    e.stopPropagation()
                    handleOpenSchedules(b)
                  }}
                  className="btn btn-secondary btn-sm"
                  style={{ fontSize: '0.72rem', padding: '4px 8px' }}
                >
                  <Clock size={12} />
                  <span>Horarios</span>
                </button>

                <span className={`badge ${b.is_active ? 'badge-success' : 'badge-danger'}`} style={{ fontSize: '0.68rem' }}>
                  {b.is_active ? 'Operando' : 'Cerrada'}
                </span>
              </div>
            </div>
          )
        })}
      </div>

      {/* Selected Branch Inventory Table */}
      {selectedBranch && (
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '14px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Store size={20} color="#60A5FA" />
                <h3 className="brand-font" style={{ fontSize: '1.15rem', color: '#FFFFFF', margin: 0 }}>
                  Inventario en {selectedBranch.name}
                </h3>
              </div>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.78rem', margin: 0 }}>
                Ajusta las unidades físicas disponibles para recogida o despacho desde esta tienda.
              </p>
            </div>

            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
              <div className="search-box" style={{ minWidth: '220px' }}>
                <Search size={16} />
                <input
                  type="text"
                  className="input"
                  placeholder="Filtrar por artículo..."
                  value={stockSearch}
                  onChange={(e) => setStockSearch(e.target.value)}
                  style={{ padding: '8px 12px 8px 38px', fontSize: '0.85rem' }}
                />
              </div>

              <button
                onClick={handleSaveAllStock}
                disabled={savingStock}
                className="btn btn-primary btn-sm"
              >
                <Save size={15} />
                <span>{savingStock ? 'Guardando...' : 'Guardar Todo el Stock'}</span>
              </button>
            </div>
          </div>

          <div className="table-responsive">
            <table className="admin-table">
              <thead>
                <tr>
                  <th>Artículo</th>
                  <th>SKU</th>
                  <th>Categoría</th>
                  <th>Precio Venta</th>
                  <th style={{ width: '130px' }}>Stock Físico</th>
                  <th style={{ width: '130px' }}>Alerta Mínima</th>
                  <th>Estado de Stock</th>
                  <th style={{ textAlign: 'right' }}>Guardar</th>
                </tr>
              </thead>
              <tbody>
                {filteredStock.length > 0 ? (
                  filteredStock.map((item) => {
                    const isOutOfStock = item.stock <= 0
                    const isLow = !isOutOfStock && item.stock <= item.min_stock

                    return (
                      <tr key={item.product_id}>
                        <td style={{ fontWeight: '600', color: '#FFFFFF' }}>{item.name}</td>
                        <td style={{ fontFamily: 'monospace', fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                          {item.sku}
                        </td>
                        <td>
                          <span className="badge badge-primary">{item.category}</span>
                        </td>
                        <td style={{ fontWeight: '600', color: '#34D399' }}>
                          Bs {Number(item.price).toFixed(2)}
                        </td>
                        <td>
                          <input
                            type="number"
                            min="0"
                            className="input"
                            value={item.stock}
                            onChange={(e) => handleStockInputChange(item.product_id, 'stock', e.target.value)}
                            style={{
                              width: '90px',
                              textAlign: 'center',
                              padding: '6px',
                              fontWeight: '700',
                              color: isOutOfStock ? '#F87171' : isLow ? '#FBBF24' : '#34D399',
                              borderColor: isOutOfStock ? 'rgba(239, 68, 68, 0.4)' : 'var(--border-medium)'
                            }}
                          />
                        </td>
                        <td>
                          <input
                            type="number"
                            min="1"
                            className="input"
                            value={item.min_stock}
                            onChange={(e) => handleStockInputChange(item.product_id, 'min_stock', e.target.value)}
                            style={{ width: '80px', textAlign: 'center', padding: '6px' }}
                          />
                        </td>
                        <td>
                          {isOutOfStock ? (
                            <span className="badge badge-danger">Agotado</span>
                          ) : isLow ? (
                            <span className="badge badge-warning">Stock Bajo</span>
                          ) : (
                            <span className="badge badge-success">Disponible</span>
                          )}
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            onClick={() => handleSaveStockItem(item)}
                            className="btn btn-secondary btn-icon btn-sm"
                            title="Guardar este stock"
                          >
                            <Save size={14} />
                          </button>
                        </td>
                      </tr>
                    )
                  })
                ) : (
                  <tr>
                    <td colSpan="8" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                      No se encontraron artículos para esta sucursal.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* Modal: Create / Edit Branch */}
      <Modal
        isOpen={isBranchModalOpen}
        onClose={() => setIsBranchModalOpen(false)}
        title={editingBranch ? `Editar Sucursal: ${editingBranch.name}` : 'Crear Nueva Sucursal'}
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsBranchModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveBranch}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando en Supabase...' : 'Guardar Sucursal'}
            </button>
          </>
        }
      >
        <form onSubmit={handleSaveBranch}>
          <div className="form-group">
            <label className="form-label">Nombre de la Sucursal *</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. Sucursal Central, NINTECLP Sur"
              value={branchForm.name}
              onChange={(e) => setBranchForm({ ...branchForm, name: e.target.value })}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Ciudad *</label>
              <input
                type="text"
                className="input"
                placeholder="Ej. La Paz, El Alto"
                value={branchForm.city}
                onChange={(e) => setBranchForm({ ...branchForm, city: e.target.value })}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Teléfono de Contacto</label>
              <input
                type="tel"
                className="input"
                placeholder="Ej. +591 2 2445566"
                value={branchForm.phone}
                onChange={(e) => setBranchForm({ ...branchForm, phone: e.target.value })}
              />
            </div>
          </div>

          <div className="form-group">
            <label className="form-label">Dirección Completa *</label>
            <input
              type="text"
              className="input"
              placeholder="Ej. Av. San Martín #123, Centro"
              value={branchForm.address}
              onChange={(e) => setBranchForm({ ...branchForm, address: e.target.value })}
              required
            />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label className="form-label">Latitud (Coordenada GPS)</label>
              <input
                type="number"
                step="0.000001"
                className="input"
                value={branchForm.latitude}
                onChange={(e) => setBranchForm({ ...branchForm, latitude: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="form-label">Longitud (Coordenada GPS)</label>
              <input
                type="number"
                step="0.000001"
                className="input"
                value={branchForm.longitude}
                onChange={(e) => setBranchForm({ ...branchForm, longitude: e.target.value })}
              />
            </div>
          </div>

          <div style={{ marginTop: '10px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '10px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={branchForm.is_active}
                onChange={(e) => setBranchForm({ ...branchForm, is_active: e.target.checked })}
                style={{ width: '18px', height: '18px', accentColor: 'var(--primary)' }}
              />
              <span style={{ fontWeight: '600', fontSize: '0.88rem', color: '#FFFFFF' }}>
                Sucursal Activa (Visible en el mapa y checkout de la app)
              </span>
            </label>
          </div>
        </form>
      </Modal>

      {/* Modal: Edit Opening Schedules */}
      <Modal
        isOpen={isScheduleModalOpen}
        onClose={() => setIsScheduleModalOpen(false)}
        title={`Horarios de Atención: ${editingBranch?.name}`}
        footer={
          <>
            <button
              type="button"
              onClick={() => setIsScheduleModalOpen(false)}
              className="btn btn-secondary"
            >
              Cancelar
            </button>
            <button
              type="button"
              onClick={handleSaveSchedules}
              disabled={saving}
              className="btn btn-primary"
            >
              {saving ? 'Guardando...' : 'Guardar Horarios'}
            </button>
          </>
        }
      >
        <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
          {schedules.map((s, idx) => (
            <div
              key={s.day_of_week}
              style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                padding: '12px 16px',
                background: 'rgba(255, 255, 255, 0.02)',
                borderRadius: 'var(--radius-md)',
                border: '1px solid var(--border-subtle)'
              }}
            >
              <div style={{ width: '110px', fontWeight: '600', color: '#FFFFFF' }}>
                {DAYS_OF_WEEK[s.day_of_week]}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <label style={{ display: 'flex', alignItems: 'center', gap: '6px', cursor: 'pointer', fontSize: '0.78rem', color: s.is_closed ? '#F87171' : 'var(--text-muted)' }}>
                  <input
                    type="checkbox"
                    checked={s.is_closed}
                    onChange={(e) => {
                      const updated = [...schedules]
                      updated[idx].is_closed = e.target.checked
                      setSchedules(updated)
                    }}
                    style={{ accentColor: '#EF4444' }}
                  />
                  <span>Cerrado</span>
                </label>

                {!s.is_closed && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                    <input
                      type="time"
                      className="input"
                      style={{ padding: '6px 10px', fontSize: '0.82rem' }}
                      value={s.open_time}
                      onChange={(e) => {
                        const updated = [...schedules]
                        updated[idx].open_time = e.target.value
                        setSchedules(updated)
                      }}
                    />
                    <span style={{ color: 'var(--text-dim)' }}>a</span>
                    <input
                      type="time"
                      className="input"
                      style={{ padding: '6px 10px', fontSize: '0.82rem' }}
                      value={s.close_time}
                      onChange={(e) => {
                        const updated = [...schedules]
                        updated[idx].close_time = e.target.value
                        setSchedules(updated)
                      }}
                    />
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      </Modal>
    </div>
  )
}
