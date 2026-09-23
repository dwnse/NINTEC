import React, { useState } from 'react'
import { AuthProvider, useAuth } from './context/AuthContext'
import Sidebar from './components/Sidebar'
import Header from './components/Header'
import LoginView from './views/LoginView'
import DashboardView from './views/DashboardView'
import ProductsView from './views/ProductsView'
import BannersView from './views/BannersView'
import BranchesView from './views/BranchesView'
import OffersView from './views/OffersView'
import OrdersView from './views/OrdersView'
import UsersView from './views/UsersView'

function AdminApp() {
  const { user, profile, loading, isAdmin } = useAuth()
  const [currentView, setCurrentView] = useState('dashboard')
  const [guestMode, setGuestMode] = useState(false)
  const [isRefreshing, setIsRefreshing] = useState(false)
  const [refreshTrigger, setRefreshTrigger] = useState(0)

  // Vista de Login si no está autenticado y no está en modo invitado
  if (!loading && (!user || !isAdmin) && !guestMode) {
    return <LoginView onGuestAccess={() => setGuestMode(true)} />
  }

  if (loading) {
    return (
      <div style={{
        minHeight: '100vh',
        width: '100vw',
        background: '#0A0F1D',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        flexDirection: 'column',
        gap: '16px'
      }}>
        <div style={{
          width: '46px',
          height: '46px',
          border: '3px solid rgba(24, 70, 215, 0.2)',
          borderTopColor: '#1846D7',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite'
        }} />
        <span style={{ color: '#94A3B8', fontSize: '0.9rem', fontFamily: 'Outfit, sans-serif' }}>
          Conectando con NINTEC Supabase Cloud...
        </span>
      </div>
    )
  }

  const getViewDetails = () => {
    switch (currentView) {
      case 'dashboard':
        return {
          title: 'Reportes y Métricas',
          subtitle: 'Visión general de ventas, pedidos y operaciones en la app móvil'
        }
      case 'products':
        return {
          title: 'Configuración de Artículos e Imágenes',
          subtitle: 'Administra catálogo, especificaciones técnicas y fotos en Supabase Storage'
        }
      case 'banners':
        return {
          title: 'Configuración del Banner del Inicio',
          subtitle: 'Promociones, enlaces directos y carrusel de la pantalla principal'
        }
      case 'branches':
        return {
          title: 'Sucursales e Inventario',
          subtitle: 'Puntos físicos, horarios de atención y stock por tienda'
        }
      case 'offers':
        return {
          title: 'Catálogo y Ofertas Especiales',
          subtitle: 'Artículos destacados, descuentos y cupones promocionales'
        }
      case 'orders':
        return {
          title: 'Gestión de Pedidos',
          subtitle: 'Seguimiento en tiempo real y cambio de estados para los clientes'
        }
      case 'users':
        return {
          title: 'Gestión de Usuarios y Cuentas',
          subtitle: 'Control de clientes registrados, roles de administrador y permisos'
        }
      default:
        return { title: 'Panel de Control', subtitle: '' }
    }
  }

  const { title, subtitle } = getViewDetails()

  const handleRefresh = () => {
    setIsRefreshing(true)
    setRefreshTrigger(prev => prev + 1)
    setTimeout(() => setIsRefreshing(false), 600)
  }

  return (
    <div className="admin-layout">
      <Sidebar
        currentView={currentView}
        setCurrentView={setCurrentView}
      />

      <div className="admin-main">
        <Header
          title={title}
          subtitle={subtitle}
          onRefresh={handleRefresh}
          isRefreshing={isRefreshing}
        />

        <main className="admin-content" key={refreshTrigger}>
          {currentView === 'dashboard' && <DashboardView onNavigate={setCurrentView} />}
          {currentView === 'products' && <ProductsView />}
          {currentView === 'banners' && <BannersView />}
          {currentView === 'branches' && <BranchesView />}
          {currentView === 'offers' && <OffersView />}
          {currentView === 'orders' && <OrdersView />}
          {currentView === 'users' && <UsersView />}
        </main>
      </div>
    </div>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AdminApp />
    </AuthProvider>
  )
}
