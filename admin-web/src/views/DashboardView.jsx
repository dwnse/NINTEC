import React, { useState, useEffect } from 'react'
import {
  DollarSign,
  ShoppingBag,
  Clock,
  CheckCircle,
  AlertTriangle,
  Users,
  TrendingUp,
  ArrowRight,
  Eye,
  Check
} from 'lucide-react'
import { adminService } from '../services/adminService'
import StatCard from '../components/StatCard'

export default function DashboardView({ onNavigate }) {
  const [stats, setStats] = useState(null)
  const [recentOrders, setRecentOrders] = useState([])
  const [chartData, setChartData] = useState([])
  const [loading, setLoading] = useState(true)
  const [updatingOrderId, setUpdatingOrderId] = useState(null)

  const loadData = async () => {
    setLoading(true)
    try {
      const [statsData, ordersData, salesData] = await Promise.all([
        adminService.getDashboardStats(),
        adminService.getRecentOrders(8),
        adminService.getSalesChartData(7)
      ])
      setStats(statsData)
      setRecentOrders(ordersData)
      setChartData(salesData)
    } catch (err) {
      console.error('Error al cargar datos del dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadData()
  }, [])

  const handleQuickStatusChange = async (orderId, newStatus) => {
    setUpdatingOrderId(orderId)
    try {
      await adminService.updateOrderStatus(orderId, newStatus)
      await loadData()
    } catch (err) {
      alert('Error al actualizar orden: ' + err.message)
    } finally {
      setUpdatingOrderId(null)
    }
  }

  const getStatusBadge = (status) => {
    switch (status) {
      case 'pending':
        return <span className="badge badge-warning">Pendiente</span>
      case 'confirmed':
        return <span className="badge badge-info">Confirmado</span>
      case 'processing':
        return <span className="badge badge-primary">En Proceso</span>
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

  // Máximo valor para escalar el gráfico SVG
  const maxSales = Math.max(...chartData.map(d => d.total), 100)

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '28px' }} className="view-enter">
      {/* Metrics Row */}
      <div style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
        gap: '20px'
      }}>
        <StatCard
          title="Total Ventas (Bs)"
          value={`Bs ${(stats?.total_revenue || 0).toLocaleString('es-BO', { minimumFractionDigits: 2 })}`}
          subtitle="Histórico en la app móvil"
          icon={DollarSign}
          color="#10B981"
        />
        <StatCard
          title="Ventas del Mes"
          value={`Bs ${(stats?.revenue_this_month || 0).toLocaleString('es-BO', { minimumFractionDigits: 2 })}`}
          subtitle="Mes actual en curso"
          icon={TrendingUp}
          color="#1846D7"
        />
        <StatCard
          title="Pedidos Pendientes"
          value={stats?.pending_orders || 0}
          subtitle="Requieren atención"
          icon={Clock}
          color="#F59E0B"
          onClick={() => onNavigate('orders')}
        />
        <StatCard
          title="Pedidos Completados"
          value={stats?.completed_orders || 0}
          subtitle="Entregados exitosamente"
          icon={CheckCircle}
          color="#06B6D4"
          onClick={() => onNavigate('orders')}
        />
        <StatCard
          title="Alertas de Stock"
          value={stats?.low_stock_alerts || 0}
          subtitle={`${stats?.out_of_stock_products || 0} artículos agotados`}
          icon={AlertTriangle}
          color="#EF4444"
          onClick={() => onNavigate('branches')}
        />
        <StatCard
          title="Clientes en App"
          value={stats?.total_customers || 0}
          subtitle="Ver cuentas registradas"
          icon={Users}
          color="#8B5CF6"
          onClick={() => onNavigate('users')}
        />
      </div>

      {/* Main Grid: Sales Graph + Low Stock Callout */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '24px' }}>
        {/* Sales Trend Chart Card */}
        <div className="card">
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
            <div>
              <h3 className="brand-font" style={{ fontSize: '1.1rem', color: '#FFFFFF', margin: 0 }}>
                Evolución de Ventas (Últimos 7 Días)
              </h3>
              <p style={{ color: 'var(--text-muted)', fontSize: '0.78rem', margin: 0 }}>
                Ingresos diarios generados desde la aplicación
              </p>
            </div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8rem', color: '#60A5FA' }}>
              <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: '#1846D7' }}></span>
              Ventas Bs
            </div>
          </div>

          {/* Custom Responsive SVG Chart */}
          <div style={{ width: '100%', height: '220px', position: 'relative', marginTop: '10px' }}>
            {chartData.length > 0 ? (
              <svg width="100%" height="100%" viewBox="0 0 700 200" preserveAspectRatio="none">
                <defs>
                  <linearGradient id="salesGrad" x1="0%" y1="0%" x2="0%" y2="100%">
                    <stop offset="0%" stopColor="#1846D7" stopOpacity="0.45" />
                    <stop offset="100%" stopColor="#1846D7" stopOpacity="0.0" />
                  </linearGradient>
                </defs>

                {/* Grid horizontal lines */}
                <line x1="0" y1="40" x2="700" y2="40" stroke="rgba(255,255,255,0.05)" strokeDasharray="4 4" />
                <line x1="0" y1="100" x2="700" y2="100" stroke="rgba(255,255,255,0.05)" strokeDasharray="4 4" />
                <line x1="0" y1="160" x2="700" y2="160" stroke="rgba(255,255,255,0.05)" strokeDasharray="4 4" />

                {/* Area Fill */}
                <polygon
                  fill="url(#salesGrad)"
                  points={`
                    0,180
                    ${chartData.map((d, idx) => {
                      const x = (idx / (chartData.length - 1 || 1)) * 680 + 10
                      const y = 180 - (d.total / maxSales) * 150
                      return `${x},${y}`
                    }).join(' ')}
                    700,180
                  `}
                />

                {/* Smooth Curve Line */}
                <polyline
                  fill="none"
                  stroke="#3B82F6"
                  strokeWidth="3.5"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  points={chartData.map((d, idx) => {
                    const x = (idx / (chartData.length - 1 || 1)) * 680 + 10
                    const y = 180 - (d.total / maxSales) * 150
                    return `${x},${y}`
                  }).join(' ')}
                />

                {/* Data Points */}
                {chartData.map((d, idx) => {
                  const x = (idx / (chartData.length - 1 || 1)) * 680 + 10
                  const y = 180 - (d.total / maxSales) * 150
                  return (
                    <g key={idx}>
                      <circle cx={x} cy={y} r="5" fill="#1846D7" stroke="#FFFFFF" strokeWidth="2" />
                      <text
                        x={x}
                        y={195}
                        fontSize="11"
                        fill="var(--text-dim)"
                        textAnchor="middle"
                      >
                        {d.label}
                      </text>
                    </g>
                  )
                })}
              </svg>
            ) : (
              <div style={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', color: 'var(--text-dim)' }}>
                Cargando métricas de ventas...
              </div>
            )}
          </div>
        </div>

        {/* Quick Action & Inventory Alert Widget */}
        <div className="card" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#F59E0B', marginBottom: '12px' }}>
              <AlertTriangle size={20} />
              <h4 className="brand-font" style={{ fontSize: '1.05rem', color: '#FFFFFF', margin: 0 }}>
                Control de Sucursales
              </h4>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.84rem', lineHeight: '1.5' }}>
              El inventario de NINTEC está descentralizado en 4 sucursales físicas (Centro, Sur, El Alto, Miraflores).
            </p>

            <div style={{
              marginTop: '16px',
              padding: '14px',
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.2)',
              borderRadius: 'var(--radius-md)'
            }}>
              <div style={{ fontSize: '0.8rem', color: '#FCA5A5', fontWeight: '600', marginBottom: '4px' }}>
                Stock Crítico
              </div>
              <div style={{ fontSize: '1.35rem', fontWeight: '700', color: '#FFFFFF' }}>
                {stats?.low_stock_alerts || 0} artículos
              </div>
              <p style={{ fontSize: '0.72rem', color: 'var(--text-dim)', margin: 0 }}>
                con existencias menores al mínimo configurado
              </p>
            </div>
          </div>

          <button
            onClick={() => onNavigate('branches')}
            className="btn btn-secondary"
            style={{ width: '100%', marginTop: '20px' }}
          >
            <span>Gestionar Sucursales</span>
            <ArrowRight size={16} />
          </button>
        </div>
      </div>

      {/* Recent Orders Section */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
          <div>
            <h3 className="brand-font" style={{ fontSize: '1.15rem', color: '#FFFFFF', margin: 0 }}>
              Últimos Pedidos Recibidos
            </h3>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.78rem', margin: 0 }}>
              Compras realizadas por clientes en la aplicación móvil
            </p>
          </div>
          <button
            onClick={() => onNavigate('orders')}
            className="btn btn-secondary btn-sm"
          >
            <span>Ver Todos los Pedidos</span>
            <ArrowRight size={14} />
          </button>
        </div>

        <div className="table-responsive">
          <table className="admin-table">
            <thead>
              <tr>
                <th>N° Orden</th>
                <th>Cliente</th>
                <th>Método de Pago</th>
                <th>Total</th>
                <th>Estado</th>
                <th>Fecha</th>
                <th style={{ textAlign: 'right' }}>Acciones Rápidas</th>
              </tr>
            </thead>
            <tbody>
              {recentOrders.length > 0 ? (
                recentOrders.map((order) => (
                  <tr key={order.id}>
                    <td style={{ fontWeight: '600', color: '#60A5FA', fontFamily: 'monospace' }}>
                      {order.order_number}
                    </td>
                    <td>
                      <div style={{ fontWeight: '500', color: '#FFFFFF' }}>
                        {order.profiles?.full_name || 'Cliente NINTEC'}
                      </div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                        {order.profiles?.phone || order.profiles?.email || 'Sin contacto'}
                      </div>
                    </td>
                    <td>
                      <span style={{ fontSize: '0.82rem', textTransform: 'capitalize' }}>
                        {order.payment_method?.replace('_', ' ') || 'QR / Efectivo'}
                      </span>
                    </td>
                    <td style={{ fontWeight: '700', color: '#34D399' }}>
                      Bs {Number(order.total || 0).toFixed(2)}
                    </td>
                    <td>
                      {getStatusBadge(order.status)}
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>
                      {new Date(order.created_at).toLocaleDateString('es-BO', {
                        day: '2-digit',
                        month: 'short',
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      {order.status === 'pending' && (
                        <button
                          onClick={() => handleQuickStatusChange(order.id, 'confirmed')}
                          disabled={updatingOrderId === order.id}
                          className="btn btn-primary btn-sm"
                          title="Confirmar Pedido"
                        >
                          <Check size={14} />
                          <span>Confirmar</span>
                        </button>
                      )}
                      {order.status === 'confirmed' && (
                        <button
                          onClick={() => handleQuickStatusChange(order.id, 'shipped')}
                          disabled={updatingOrderId === order.id}
                          className="btn btn-secondary btn-sm"
                          title="Marcar como Enviado"
                        >
                          <span>Despachar</span>
                        </button>
                      )}
                      {['shipped', 'processing'].includes(order.status) && (
                        <button
                          onClick={() => handleQuickStatusChange(order.id, 'delivered')}
                          disabled={updatingOrderId === order.id}
                          className="btn btn-secondary btn-sm"
                          style={{ color: '#34D399' }}
                          title="Marcar como Entregado"
                        >
                          <span>Entregar</span>
                        </button>
                      )}
                      {['delivered', 'completed', 'cancelled'].includes(order.status) && (
                        <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                          Finalizado
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="7" style={{ textAlign: 'center', padding: '36px', color: 'var(--text-dim)' }}>
                    No hay pedidos registrados todavía en la aplicación.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  )
}
