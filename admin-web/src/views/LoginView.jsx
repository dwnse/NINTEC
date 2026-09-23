import React, { useState } from 'react'
import { ShieldCheck, Lock, Mail, AlertCircle, ArrowRight, Database, Terminal, CheckCircle2 } from 'lucide-react'
import { useAuth } from '../context/AuthContext'

export default function LoginView({ onGuestAccess }) {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [errorMsg, setErrorMsg] = useState('')
  const [showSqlGuide, setShowSqlGuide] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!email || !password) {
      setErrorMsg('Por favor ingresa tu correo y contraseña')
      return
    }

    setLoading(true)
    setErrorMsg('')

    try {
      await login(email, password)
    } catch (err) {
      setErrorMsg(err.message || 'Error al autenticar en Supabase')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      minHeight: '100vh',
      width: '100vw',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'radial-gradient(circle at 50% 30%, rgba(24, 70, 215, 0.15) 0%, rgba(13, 21, 39, 0.95) 70%), #0A0F1D',
      padding: '20px'
    }}>
      <div style={{
        width: '100%',
        maxWidth: '440px',
        background: 'rgba(19, 28, 53, 0.85)',
        backdropFilter: 'blur(16px)',
        border: '1px solid rgba(255, 255, 255, 0.1)',
        borderRadius: 'var(--radius-xl)',
        padding: '36px 32px',
        boxShadow: '0 20px 50px rgba(0, 0, 0, 0.6)'
      }}>
        {/* Header Branding */}
        <div style={{ textAlign: 'center', marginBottom: '32px' }}>
          <div style={{
            width: '54px',
            height: '54px',
            background: 'linear-gradient(135deg, #1846D7 0%, #3B82F6 100%)',
            borderRadius: '14px',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'white',
            boxShadow: '0 8px 24px rgba(24, 70, 215, 0.4)',
            marginBottom: '16px'
          }}>
            <ShieldCheck size={30} />
          </div>
          <h1 className="brand-font" style={{ fontSize: '1.75rem', color: '#FFFFFF', marginBottom: '6px' }}>
            NINTEC Control
          </h1>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem' }}>
            Panel Administrativo Web para la App Móvil
          </p>
        </div>

        {/* Error Alert */}
        {errorMsg && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.3)',
            borderRadius: 'var(--radius-md)',
            padding: '12px 16px',
            marginBottom: '20px',
            display: 'flex',
            gap: '10px',
            alignItems: 'flex-start'
          }}>
            <AlertCircle size={18} color="#F87171" style={{ flexShrink: 0, marginTop: '2px' }} />
            <div style={{ fontSize: '0.8rem', color: '#FCA5A5', lineHeight: '1.4' }}>
              {errorMsg}
            </div>
          </div>
        )}

        {/* Form */}
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">Correo Electrónico de Administrador</label>
            <div style={{ position: 'relative' }}>
              <Mail size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dim)' }} />
              <input
                type="email"
                className="input"
                style={{ paddingLeft: '42px' }}
                placeholder="admin@nintec.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                autoComplete="email"
              />
            </div>
          </div>

          <div className="form-group" style={{ marginBottom: '24px' }}>
            <label className="form-label">Contraseña</label>
            <div style={{ position: 'relative' }}>
              <Lock size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dim)' }} />
              <input
                type="password"
                className="input"
                style={{ paddingLeft: '42px' }}
                placeholder="••••••••••••"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                autoComplete="current-password"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="btn btn-primary"
            style={{ width: '100%', padding: '12px', fontSize: '0.95rem' }}
          >
            {loading ? 'Verificando credenciales...' : (
              <>
                <span>Acceder al Panel</span>
                <ArrowRight size={18} />
              </>
            )}
          </button>
        </form>

        {/* Helper for Admin Promotion in Supabase */}
        <div style={{ marginTop: '24px', borderTop: '1px solid var(--border-subtle)', paddingTop: '18px' }}>
          <button
            type="button"
            onClick={() => setShowSqlGuide(!showSqlGuide)}
            style={{
              background: 'none',
              border: 'none',
              color: 'var(--text-muted)',
              fontSize: '0.78rem',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '6px',
              width: '100%',
              cursor: 'pointer'
            }}
          >
            <Database size={14} color="#60A5FA" />
            <span>¿Cómo asignar rol de Administrador en Supabase?</span>
          </button>

          {showSqlGuide && (
            <div style={{
              marginTop: '12px',
              padding: '12px',
              background: 'rgba(0, 0, 0, 0.3)',
              borderRadius: 'var(--radius-md)',
              border: '1px solid var(--border-subtle)',
              fontSize: '0.75rem',
              color: 'var(--text-muted)'
            }}>
              <p style={{ marginBottom: '6px', color: '#93C5FD' }}>
                Ejecuta esto en el <strong>SQL Editor</strong> de Supabase:
              </p>
              <pre style={{
                background: '#070C18',
                padding: '8px 10px',
                borderRadius: '6px',
                color: '#34D399',
                fontFamily: 'monospace',
                overflowX: 'auto',
                fontSize: '0.72rem',
                border: '1px solid rgba(255, 255, 255, 0.05)'
              }}>
{`UPDATE public.profiles
SET role = 'super_admin'
WHERE email = 'tu_correo@gmail.com';`}
              </pre>
            </div>
          )}

          {/* Quick preview bypass button for instant inspection */}
          {onGuestAccess && (
            <div style={{ marginTop: '14px', textAlign: 'center' }}>
              <button
                type="button"
                onClick={onGuestAccess}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#60A5FA',
                  fontSize: '0.78rem',
                  textDecoration: 'underline',
                  cursor: 'pointer'
                }}
              >
                Modo Exploración Rápida (Sin Login previo)
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
