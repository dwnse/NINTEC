import React from 'react'
import { Bell, RefreshCw, Smartphone } from 'lucide-react'

export default function Header({ title, subtitle, onRefresh, isRefreshing }) {
  return (
    <header className="admin-header">
      <div className="header-title-wrap">
        <div>
          <h1 className="header-title brand-font">{title}</h1>
          {subtitle && (
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', margin: 0 }}>
              {subtitle}
            </p>
          )}
        </div>
      </div>

      <div className="header-actions">
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 12px',
          background: 'rgba(255, 255, 255, 0.04)',
          borderRadius: 'var(--radius-full)',
          border: '1px solid var(--border-subtle)',
          fontSize: '0.8rem',
          color: 'var(--text-muted)'
        }}>
          <Smartphone size={15} color="#34D399" />
          <span>App Móvil NINTEC v1.0</span>
        </div>

        {onRefresh && (
          <button
            onClick={onRefresh}
            disabled={isRefreshing}
            className="btn btn-secondary btn-icon"
            title="Recargar datos"
          >
            <RefreshCw size={17} className={isRefreshing ? 'animate-spin' : ''} />
          </button>
        )}
      </div>
    </header>
  )
}
