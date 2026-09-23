import React from 'react'
import {
  LayoutDashboard,
  Package,
  Image as ImageIcon,
  MapPin,
  Tag,
  ShoppingBag,
  Users,
  LogOut,
  ExternalLink,
  ShieldCheck
} from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function Sidebar({ currentView, setCurrentView }) {
  const { profile, logout } = useAuth()

  const navItems = [
    { id: 'dashboard', label: 'Reportes y Métricas', icon: LayoutDashboard },
    { id: 'products', label: 'Artículos e Imágenes', icon: Package },
    { id: 'banners', label: 'Banners de Inicio', icon: ImageIcon },
    { id: 'branches', label: 'Sucursales e Inventario', icon: MapPin },
    { id: 'offers', label: 'Catálogo y Ofertas', icon: Tag },
    { id: 'orders', label: 'Gestión de Pedidos', icon: ShoppingBag },
    { id: 'users', label: 'Usuarios y Cuentas', icon: Users }
  ]

  const userInitial = profile?.full_name ? profile.full_name[0].toUpperCase() : 'A'

  return (
    <aside className="admin-sidebar">
      {/* Brand Header */}
      <div className="sidebar-brand">
        <div className="brand-icon">
          <svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <rect width="18" height="18" x="3" y="3" rx="2" />
            <path d="m9 8 6 4-6 4Z" />
          </svg>
        </div>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center' }}>
            <span className="brand-title brand-font">NINTEC</span>
            <span className="brand-badge">ADMIN</span>
          </div>
          <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)' }}>Control Panel</span>
        </div>
      </div>

      {/* Navigation */}
      <nav className="sidebar-nav">
        <span className="nav-section-title">Módulos de Control</span>
        {navItems.map((item) => {
          const Icon = item.icon
          const isActive = currentView === item.id
          return (
            <button
              key={item.id}
              onClick={() => setCurrentView(item.id)}
              className={`nav-item ${isActive ? 'active' : ''}`}
            >
              <Icon size={19} />
              <span>{item.label}</span>
            </button>
          )
        })}

        <div style={{ marginTop: 'auto', paddingTop: '16px' }}>
          <span className="nav-section-title">Conexión App</span>
          <div style={{
            padding: '12px',
            background: 'rgba(24, 70, 215, 0.08)',
            border: '1px solid rgba(24, 70, 215, 0.2)',
            borderRadius: 'var(--radius-md)',
            margin: '0 8px 12px',
            fontSize: '0.78rem'
          }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px', color: '#60A5FA', fontWeight: '600', marginBottom: '4px' }}>
              <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10B981', display: 'inline-block' }}></span>
              Supabase Sincronizado
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.72rem', margin: 0 }}>
              Los cambios impactan en vivo a la app Android.
            </p>
          </div>
        </div>
      </nav>

      {/* User / Footer */}
      <div className="sidebar-footer">
        <div className="user-profile-widget">
          <div className="user-avatar">{userInitial}</div>
          <div className="user-info">
            <div className="user-name">{profile?.full_name || profile?.username || 'Administrador'}</div>
            <div className="user-role">
              <ShieldCheck size={12} />
              <span>{profile?.role === 'super_admin' ? 'Super Admin' : 'Admin'}</span>
            </div>
          </div>
          <button
            onClick={logout}
            title="Cerrar sesión"
            className="btn btn-secondary btn-icon btn-sm"
            style={{ borderRadius: '8px' }}
          >
            <LogOut size={16} color="#F87171" />
          </button>
        </div>
      </div>
    </aside>
  )
}
