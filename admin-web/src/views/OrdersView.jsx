import React, { useState, useEffect } from 'react'
import {
  ShoppingBag,
  Search,
  Filter,
  CheckCircle,
  Clock,
  Truck,
  XCircle,
  Eye,
  FileText,
  User,
  MapPin,
  Calendar,
  DollarSign
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'

export default function OrdersView() {
  const [orders, setOrders] = useState([])
  const [loading, setLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState('all')
  const [searchTerm, setSearchTerm] = useState('')

  // Order Details Modal
  const [selectedOrder, setSelectedOrder] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [adminNotes, setAdminNotes] = useState('')
  const [updating, setUpdating] = useState(false)

  const loadOrders = async () => {
    setLoading(true)
    try {
      const data = await adminService.getOrders({
        status: statusFilter,
        search: searchTerm
      })
      setOrders(data)
    } catch (err) {
      console.error('Error cargando pedidos:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadOrders()
  }, [statusFilter])

  const handleOpenDetails = (order) => {
    setSelectedOrder(order)
    setAdminNotes(order.admin_notes || '')
    setIsModalOpen(true)
  }

  const handleStatusChange = async (newStatus) => {
    if (!selectedOrder) return
    setUpdating(true)
    try {
      await adminService.updateOrderStatus(selectedOrder.id, newStatus, adminNotes)
      setIsModalOpen(false)
      await loadOrders()
    } catch (err) {
      alert('Error actualizando pedido: ' + err.message)
    } finally {
      setUpdating(false)
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'pending':
        return <span className="badge badge-warning">Pendiente</span>
      case 'confirmed':
        return <span className="badge badge-info">Confirmado</span>
      case 'processing':
        return <span className="badge badge-primary">En Preparación</span>
      case 'shipped':
        return <span className="badge badge-primary">En Camino</span>
      case 'delivered':
      case 'completed':
        return <span className="badge badge-success">Entregado</span>
      case 'cancelled':
      case 'rejected':
        return <span className="badge badge-danger">Cancelado</span>
      default:
        return <span className="badge">{status}</span>
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }} className="view-enter">
      {/* Top Toolbar */}
      <div className="toolbar">
        <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap', flex: 1 }}>
          <div className="search-box">
            <Search size={18} />
            <input
              type="text"
              className="input"
              placeholder="Buscar por N° Orden (ej. NIN-0001)..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && loadOrders()}
            />
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              className="input"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="all">Todos los Estados</option>
              <option value="pending">Pendientes</option>
              <option value="confirmed">Confirmados</option>
              <option value="processing">En Preparación</option>
              <option value="shipped">En Camino (Despachados)</option>
              <option value="delivered">Entregados / Completados</option>
              <option value="cancelled">Cancelados</option>
            </select>
          </div>
        </div>

        <button onClick={loadOrders} className="btn btn-secondary btn-sm">
          Actualizar Lista
        </button>
      </div>

      {/* Orders Table */}
      <div className="table-responsive">
        <table className="admin-table">
          <thead>
            <tr>
              <th>N° Orden</th>
              <th>Cliente</th>
              <th>Sucursal / Destino</th>
              <th>Método de Pago</th>
              <th>Total (Bs)</th>
              <th>Estado</th>
              <th>Fecha de Creación</th>
              <th style={{ textAlign: 'right' }}>Detalle</th>
            </tr>
          </thead>
          <tbody>
            {orders.length > 0 ? (
              orders.map((o) => (
                <tr key={o.id}>
                  <td style={{ fontFamily: 'monospace', fontWeight: '700', color: '#60A5FA' }}>
                    {o.order_number}
                  </td>
                  <td>
                    <div style={{ fontWeight: '600', color: '#FFFFFF' }}>
                      {o.profiles?.full_name || 'Cliente NINTEC'}
                    </div>
                    <div style={{ fontSize: '0.74rem', color: 'var(--text-dim)' }}>
                      {o.profiles?.phone || o.profiles?.email || 'Sin contacto'}
                    </div>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                      {o.branches?.name ? `Recojo: ${o.branches.name}` : 'Envío a Domicilio'}
                    </span>
                  </td>
                  <td>
                    <span style={{ fontSize: '0.82rem', textTransform: 'capitalize' }}>
                      {o.payment_method?.replace('_', ' ') || 'Efectivo / QR'}
                    </span>
                  </td>
                  <td style={{ fontWeight: '700', color: '#34D399' }}>
                    Bs {Number(o.total || 0).toFixed(2)}
                  </td>
                  <td>
                    {getStatusBadge(o.status)}
                  </td>
                  <td style={{ fontSize: '0.78rem', color: 'var(--text-dim)' }}>
                    {new Date(o.created_at).toLocaleDateString('es-BO', {
                      day: '2-digit',
                      month: 'short',
                      year: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </td>
                  <td style={{ textAlign: 'right' }}>
                    <button
                      onClick={() => handleOpenDetails(o)}
                      className="btn btn-secondary btn-icon btn-sm"
                      title="Ver detalles del pedido"
                    >
                      <Eye size={15} />
                    </button>
                  </td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="8" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                  {loading ? 'Cargando órdenes...' : 'No se encontraron pedidos con los filtros aplicados.'}
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Order Details & Status Changer Modal */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={`Detalle de la Orden: ${selectedOrder?.order_number}`}
        maxWidth="720px"
        footer={
          <div style={{ display: 'flex', justifyContent: 'space-between', width: '100%', alignItems: 'center' }}>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-dim)' }}>
              Al cambiar de estado, la app móvil notificará al cliente automáticamente.
            </div>

            <div style={{ display: 'flex', gap: '8px' }}>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="btn btn-secondary"
              >
                Cerrar
              </button>

              {selectedOrder?.status === 'pending' && (
                <button
                  type="button"
                  onClick={() => handleStatusChange('confirmed')}
                  disabled={updating}
                  className="btn btn-primary"
                >
                  Confirmar Pedido
                </button>
              )}

              {selectedOrder?.status === 'confirmed' && (
                <button
                  type="button"
                  onClick={() => handleStatusChange('shipped')}
                  disabled={updating}
                  className="btn btn-primary"
                >
                  Marcar En Camino
                </button>
              )}

              {['shipped', 'processing'].includes(selectedOrder?.status) && (
                <button
                  type="button"
                  onClick={() => handleStatusChange('delivered')}
                  disabled={updating}
                  className="btn btn-primary"
                  style={{ background: '#10B981' }}
                >
                  Marcar Entregado
                </button>
              )}

              {!['cancelled', 'delivered'].includes(selectedOrder?.status) && (
                <button
                  type="button"
                  onClick={() => handleStatusChange('cancelled')}
                  disabled={updating}
                  className="btn btn-danger btn-sm"
                >
                  Cancelar Pedido
                </button>
              )}
            </div>
          </div>
        }
      >
        {selectedOrder && (
          <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
            {/* Order Status & Amounts Summary */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))',
              gap: '12px',
              padding: '16px',
              background: 'rgba(255, 255, 255, 0.02)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-subtle)'
            }}>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>Estado Actual</span>
                <div style={{ marginTop: '4px' }}>{getStatusBadge(selectedOrder.status)}</div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>Subtotal</span>
                <div style={{ fontWeight: '600', color: '#FFFFFF' }}>Bs {Number(selectedOrder.subtotal || 0).toFixed(2)}</div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>Descuento Cupón</span>
                <div style={{ fontWeight: '600', color: '#F87171' }}>-Bs {Number(selectedOrder.discount || 0).toFixed(2)}</div>
              </div>
              <div>
                <span style={{ fontSize: '0.72rem', color: 'var(--text-dim)' }}>Total a Pagar</span>
                <div style={{ fontWeight: '700', color: '#34D399', fontSize: '1.1rem' }}>
                  Bs {Number(selectedOrder.total || 0).toFixed(2)}
                </div>
              </div>
            </div>

            {/* Customer Details */}
            <div style={{ padding: '14px', background: '#0D1527', borderRadius: 'var(--radius-md)', border: '1px solid var(--border-subtle)' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#60A5FA', marginBottom: '8px' }}>
                <User size={16} />
                <span style={{ fontWeight: '600', fontSize: '0.88rem' }}>Datos del Cliente</span>
              </div>
              <div style={{ fontSize: '0.82rem', color: 'var(--text-muted)' }}>
                <strong>Nombre:</strong> {selectedOrder.profiles?.full_name || 'No especificado'} <br />
                <strong>Email:</strong> {selectedOrder.profiles?.email || 'No especificado'} <br />
                <strong>Teléfono:</strong> {selectedOrder.profiles?.phone || 'No especificado'} <br />
                {selectedOrder.notes && (
                  <p style={{ marginTop: '6px', color: '#FBBF24', fontSize: '0.78rem' }}>
                    <strong>Notas del Cliente:</strong> "{selectedOrder.notes}"
                  </p>
                )}
              </div>
            </div>

            {/* Order Items Table */}
            <div>
              <h4 className="brand-font" style={{ fontSize: '0.95rem', color: '#FFFFFF', marginBottom: '10px' }}>
                Artículos Comprados ({selectedOrder.order_items?.length || 0})
              </h4>
              <div className="table-responsive">
                <table className="admin-table">
                  <thead>
                    <tr>
                      <th>Artículo</th>
                      <th>Precio Unit.</th>
                      <th>Cant.</th>
                      <th style={{ textAlign: 'right' }}>Total</th>
                    </tr>
                  </thead>
                  <tbody>
                    {(selectedOrder.order_items || []).map((item) => (
                      <tr key={item.id}>
                        <td>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                            {item.product_image && (
                              <img
                                src={item.product_image}
                                alt={item.product_name}
                                style={{ width: '34px', height: '34px', borderRadius: '6px', objectFit: 'cover' }}
                              />
                            )}
                            <span style={{ fontWeight: '600', color: '#FFFFFF' }}>{item.product_name}</span>
                          </div>
                        </td>
                        <td>Bs {Number(item.unit_price).toFixed(2)}</td>
                        <td>x{item.quantity}</td>
                        <td style={{ textAlign: 'right', fontWeight: '700', color: '#34D399' }}>
                          Bs {Number(item.line_total).toFixed(2)}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Admin Notes Field */}
            <div className="form-group" style={{ margin: 0 }}>
              <label className="form-label">Notas Internas del Administrador</label>
              <textarea
                className="input"
                rows="2"
                placeholder="Ej. Cliente solicitó entrega por la tarde, pagado vía QR BNB..."
                value={adminNotes}
                onChange={(e) => setAdminNotes(e.target.value)}
              />
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
