import React from 'react'

export default function StatCard({ title, value, subtitle, icon: Icon, color = '#1846D7', glowColor, onClick }) {
  return (
    <div
      className="stat-card"
      onClick={onClick}
      style={{
        '--glow-color': glowColor || `${color}25`,
        cursor: onClick ? 'pointer' : 'default'
      }}
    >
      <div className="stat-header">
        <span className="stat-title">{title}</span>
        <div
          className="stat-icon-wrap"
          style={{
            background: `${color}18`,
            color: color,
            border: `1px solid ${color}35`
          }}
        >
          {Icon && <Icon size={20} />}
        </div>
      </div>
      <div className="stat-value">{value}</div>
      {subtitle && <div className="stat-footer">{subtitle}</div>}
    </div>
  )
}
