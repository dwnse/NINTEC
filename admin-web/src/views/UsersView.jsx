import React, { useState, useEffect } from 'react'
import {
  Users,
  Search,
  Shield,
  ShieldCheck,
  UserCheck,
  UserX,
  Copy,
  Check,
  Sparkles,
  Database,
  ExternalLink,
  RefreshCw,
  Mail,
  Calendar,
  UserPlus,
  AlertCircle
} from 'lucide-react'
import { adminService } from '../services/adminService'
import Modal from '../components/Modal'

export default function UsersView() {
  const [users, setUsers] = useState([])
  const [loading, setLoading] = useState(true)
  const [searchTerm, setSearchTerm] = useState('')
  const [roleFilter, setRoleFilter] = useState('all')
  const [copiedSql, setCopiedSql] = useState(false)
  const [updatingUserId, setUpdatingUserId] = useState(null)
  const [isModalOpen, setIsModalOpen] = useState(false)

  // Formulario para vincular o añadir cuenta
  const [formData, setFormData] = useState({
    full_name: '',
    username: '',
    email: '',
    role: 'customer'
  })

  const loadUsers = async () => {
    setLoading(true)
    try {
      const data = await adminService.getUsers()
      setUsers(data)
    } catch (err) {
      console.error('Error cargando usuarios:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadUsers()
  }, [])

  const handleRoleChange = async (userId, newRole) => {
    setUpdatingUserId(userId)
    try {
      await adminService.setUserRole(userId, newRole)
      // Actualizar estado local inmediatamente
      setUsers(users.map(u => u.id === userId ? { ...u, role: newRole } : u))
    } catch (err) {
      alert('Error cambiando rol: ' + err.message)
    } finally {
      setUpdatingUserId(null)
    }
  }

  const handleToggleStatus = async (user) => {
    setUpdatingUserId(user.id)
    try {
      await adminService.toggleUserStatus(user.id, user.is_active)
      setUsers(users.map(u => u.id === user.id ? { ...u, is_active: !user.is_active } : u))
    } catch (err) {
      alert('Error cambiando estado: ' + err.message)
    } finally {
      setUpdatingUserId(null)
    }
  }

  const copyPromotionSql = () => {
    const sql = `-- NINTEC — Base de Datos Supabase
-- Sincronizar y Promover Cuentas KevinRX y Jhosmar a Administrador
INSERT INTO public.profiles (id, full_name, email, username, role, is_active, created_at, updated_at)
SELECT 
    u.id,
    COALESCE(u.raw_user_meta_data->>'full_name', split_part(u.email, '@', 1)),
    COALESCE(u.email, ''),
    COALESCE(u.raw_user_meta_data->>'username', split_part(u.email, '@', 1)),
    CASE 
        WHEN u.email ILIKE '%kevinrx%' OR u.raw_user_meta_data->>'username' ILIKE '%kevinrx%' THEN 'super_admin'
        WHEN u.email ILIKE '%jhosmar%' OR u.raw_user_meta_data->>'username' ILIKE '%jhosmar%' THEN 'admin'
        ELSE 'customer'
    END,
    TRUE,
    u.created_at,
    NOW()
FROM auth.users u
ON CONFLICT (id) DO UPDATE 
SET 
    email = EXCLUDED.email,
    role = CASE WHEN EXCLUDED.role IN ('super_admin', 'admin') THEN EXCLUDED.role ELSE public.profiles.role END,
    updated_at = NOW();

UPDATE public.profiles SET role = 'super_admin', updated_at = NOW() WHERE username ILIKE '%kevinrx%' OR email ILIKE '%kevinrx%';
UPDATE public.profiles SET role = 'admin', updated_at = NOW() WHERE username ILIKE '%jhosmar%' OR email ILIKE '%jhosmar%';

ALTER TABLE public.profiles ENABLE ROW LEVEL SECURITY;
DROP POLICY IF EXISTS "profiles_select_public" ON public.profiles;
CREATE POLICY "profiles_select_public" ON public.profiles FOR SELECT USING (TRUE);

DROP POLICY IF EXISTS "cart_items_anon_all" ON public.cart_items;
CREATE POLICY "cart_items_anon_all" ON public.cart_items FOR ALL USING (TRUE) WITH CHECK (TRUE);`

    navigator.clipboard.writeText(sql)
    setCopiedSql(true)
    setTimeout(() => setCopiedSql(false), 3000)
  }

  const handleSaveUser = async (e) => {
    e.preventDefault()
    if (!formData.email && !formData.username) {
      alert('Ingresa al menos un correo o nombre de usuario')
      return
    }

    try {
      await adminService.syncOrRegisterUser(formData)
      setIsModalOpen(false)
      setFormData({ full_name: '', username: '', email: '', role: 'customer' })
      await loadUsers()
    } catch (err) {
      alert('Error guardando usuario: ' + err.message)
    }
  }

  const filteredUsers = users.filter(u => {
    const matchesSearch =
      (u.full_name && u.full_name.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (u.username && u.username.toLowerCase().includes(searchTerm.toLowerCase())) ||
      (u.email && u.email.toLowerCase().includes(searchTerm.toLowerCase()))
    const matchesRole = roleFilter === 'all' || u.role === roleFilter
    return matchesSearch && matchesRole
  })

  const superAdminCount = users.filter(u => u.role === 'super_admin').length
  const adminCount = users.filter(u => u.role === 'admin').length
  const customerCount = users.filter(u => u.role !== 'admin' && u.role !== 'super_admin').length

  const getRoleBadge = (role) => {
    switch (role) {
      case 'super_admin':
        return (
          <span className="badge badge-info" style={{ background: 'rgba(139, 92, 246, 0.15)', color: '#C084FC', borderColor: 'rgba(139, 92, 246, 0.3)' }}>
            <ShieldCheck size={12} /> Super Admin
          </span>
        )
      case 'admin':
        return (
          <span className="badge badge-primary">
            <Shield size={12} /> Administrador
          </span>
        )
      default:
        return (
          <span className="badge badge-success">
            Cliente App
          </span>
        )
    }
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '24px' }} className="view-enter">
      {/* Promotion & SQL Quick Fix Banner */}
      <div className="card" style={{
        background: 'linear-gradient(135deg, rgba(24, 70, 215, 0.15) 0%, rgba(16, 185, 129, 0.1) 100%)',
        border: '1px solid rgba(24, 70, 215, 0.3)',
        display: 'flex',
        flexDirection: 'column',
        gap: '12px'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '14px' }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#60A5FA', marginBottom: '4px' }}>
              <Database size={18} />
              <h3 className="brand-font" style={{ fontSize: '1.1rem', color: '#FFFFFF', margin: 0 }}>
                Sincronización de Cuentas (KevinRX & Jhosmar)
              </h3>
            </div>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.82rem', margin: 0 }}>
              Cuentas registradas en la app móvil. Puedes cambiar sus roles directamente desde este panel o ejecutando el script en Supabase SQL Editor.
            </p>
          </div>

          <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
            <button
              onClick={() => setIsModalOpen(true)}
              className="btn btn-primary btn-sm"
            >
              <UserPlus size={14} />
              <span>Vincular / Añadir Cuenta</span>
            </button>

            <button
              onClick={copyPromotionSql}
              className="btn btn-secondary btn-sm"
              style={{ borderColor: 'rgba(24, 70, 215, 0.4)' }}
            >
              {copiedSql ? <Check size={14} color="#34D399" /> : <Copy size={14} />}
              <span>{copiedSql ? '¡SQL Copiado para Supabase!' : 'Copiar SQL para Supabase'}</span>
            </button>
          </div>
        </div>
      </div>

      {/* Mini Stat Summary */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: '16px' }}>
        <div className="card" style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(24, 70, 215, 0.15)', color: '#1846D7', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Users size={22} />
          </div>
          <div>
            <div style={{ fontSize: '1.3rem', fontWeight: '800', color: '#FFFFFF' }}>{users.length}</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Cuentas Registradas</div>
          </div>
        </div>

        <div className="card" style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(139, 92, 246, 0.15)', color: '#C084FC', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ShieldCheck size={22} />
          </div>
          <div>
            <div style={{ fontSize: '1.3rem', fontWeight: '800', color: '#FFFFFF' }}>{superAdminCount}</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Super Administradores</div>
          </div>
        </div>

        <div className="card" style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(59, 130, 246, 0.15)', color: '#60A5FA', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Shield size={22} />
          </div>
          <div>
            <div style={{ fontSize: '1.3rem', fontWeight: '800', color: '#FFFFFF' }}>{adminCount}</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Administradores Activos</div>
          </div>
        </div>

        <div className="card" style={{ padding: '16px 20px', display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{ width: '42px', height: '42px', borderRadius: '10px', background: 'rgba(16, 185, 129, 0.15)', color: '#10B981', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <UserCheck size={22} />
          </div>
          <div>
            <div style={{ fontSize: '1.3rem', fontWeight: '800', color: '#FFFFFF' }}>{customerCount}</div>
            <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>Clientes en la App</div>
          </div>
        </div>
      </div>

      {/* Toolbar */}
      <div className="toolbar">
        <div style={{ display: 'flex', gap: '14px', flexWrap: 'wrap', flex: 1 }}>
          <div className="search-box">
            <Search size={18} />
            <input
              type="text"
              className="input"
              placeholder="Buscar por usuario (@kevinrx, @jhosmar) o email..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
            />
          </div>

          <div style={{ minWidth: '180px' }}>
            <select
              className="input"
              value={roleFilter}
              onChange={(e) => setRoleFilter(e.target.value)}
            >
              <option value="all">Todos los Roles</option>
              <option value="customer">Clientes App</option>
              <option value="admin">Administradores</option>
              <option value="super_admin">Super Admins</option>
            </select>
          </div>
        </div>

        <button onClick={loadUsers} className="btn btn-secondary btn-sm">
          <RefreshCw size={14} className={loading ? 'spin' : ''} />
          <span>Actualizar Lista</span>
        </button>
      </div>

      {/* Users Table */}
      <div className="table-responsive">
        <table className="admin-table">
          <thead>
            <tr>
              <th>Usuario / Cliente</th>
              <th>Nombre Completo</th>
              <th>Correo Electrónico</th>
              <th>Rol Actual</th>
              <th>Estado de Cuenta</th>
              <th>Fecha Registro</th>
              <th style={{ textAlign: 'right' }}>Asignar Rol en App</th>
            </tr>
          </thead>
          <tbody>
            {filteredUsers.length > 0 ? (
              filteredUsers.map((u) => {
                const initial = u.full_name ? u.full_name[0].toUpperCase() : u.username ? u.username[0].toUpperCase() : 'U'
                const isKevin = u.username?.toLowerCase().includes('kevin') || u.email?.toLowerCase().includes('kevin')
                const isJhosmar = u.username?.toLowerCase().includes('jhosmar') || u.email?.toLowerCase().includes('jhosmar')

                return (
                  <tr key={u.id} style={{ background: isKevin || isJhosmar ? 'rgba(24, 70, 215, 0.05)' : undefined }}>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <div style={{
                          width: '38px',
                          height: '38px',
                          borderRadius: '50%',
                          background: isKevin ? 'linear-gradient(135deg, #1846D7 0%, #3B82F6 100%)' :
                                      isJhosmar ? 'linear-gradient(135deg, #8B5CF6 0%, #6366F1 100%)' :
                                      '#1E293B',
                          color: '#FFFFFF',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontWeight: '700',
                          fontSize: '0.9rem',
                          boxShadow: isKevin || isJhosmar ? '0 2px 10px rgba(24, 70, 215, 0.4)' : undefined
                        }}>
                          {initial}
                        </div>
                        <div>
                          <div style={{ fontWeight: '600', color: '#FFFFFF', display: 'flex', alignItems: 'center', gap: '6px' }}>
                            @{u.username || 'sin_usuario'}
                            {(isKevin || isJhosmar) && (
                              <Sparkles size={13} color={isKevin ? '#60A5FA' : '#C084FC'} />
                            )}
                          </div>
                          {(isKevin || isJhosmar) && (
                            <span style={{ fontSize: '0.68rem', color: isKevin ? '#60A5FA' : '#C084FC', fontWeight: '600' }}>
                              Cuenta Prioritaria
                            </span>
                          )}
                        </div>
                      </div>
                    </td>
                    <td style={{ fontWeight: '500', color: 'var(--text-main)' }}>
                      {u.full_name || 'Sin nombre'}
                    </td>
                    <td style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                      {u.email || 'Sin email'}
                    </td>
                    <td>
                      {getRoleBadge(u.role)}
                    </td>
                    <td>
                      <button
                        onClick={() => handleToggleStatus(u)}
                        disabled={updatingUserId === u.id}
                        className={`badge ${u.is_active !== false ? 'badge-success' : 'badge-danger'}`}
                        style={{ cursor: 'pointer', border: 'none' }}
                        title="Clic para bloquear o activar"
                      >
                        {u.is_active !== false ? 'Activo' : 'Bloqueado'}
                      </button>
                    </td>
                    <td style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                      {u.created_at ? new Date(u.created_at).toLocaleDateString('es-BO') : 'Reciente'}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <select
                        value={u.role || 'customer'}
                        disabled={updatingUserId === u.id}
                        onChange={(e) => handleRoleChange(u.id, e.target.value)}
                        className="input"
                        style={{
                          width: 'auto',
                          display: 'inline-block',
                          padding: '4px 10px',
                          fontSize: '0.8rem',
                          background: u.role === 'super_admin' ? 'rgba(139, 92, 246, 0.2)' :
                                      u.role === 'admin' ? 'rgba(24, 70, 215, 0.2)' : 'rgba(255, 255, 255, 0.05)',
                          borderColor: u.role === 'super_admin' ? 'rgba(139, 92, 246, 0.4)' :
                                       u.role === 'admin' ? 'rgba(24, 70, 215, 0.4)' : 'rgba(255, 255, 255, 0.1)',
                          color: '#FFFFFF'
                        }}
                      >
                        <option value="customer">Cliente App</option>
                        <option value="admin">Administrador</option>
                        <option value="super_admin">Super Admin</option>
                      </select>
                    </td>
                  </tr>
                )
              })
            ) : (
              <tr>
                <td colSpan="7" style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                  No se encontraron cuentas registradas.
                </td>
              </tr>
            )}
          </tbody>
        </table>
      </div>

      {/* Modal para Vincular / Añadir Cuenta Registrada */}
      {isModalOpen && (
        <Modal
          title="Vincular o Sincronizar Cuenta de Usuario"
          onClose={() => setIsModalOpen(false)}
        >
          <form onSubmit={handleSaveUser} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', margin: 0 }}>
              Si creaste una cuenta en la aplicación móvil o en Supabase (ej. @jhosmar o @kevinrx), regístrala aquí para gestionarla y otorgarle permisos de inmediato.
            </p>

            <div className="form-group">
              <label className="label">Nombre Completo</label>
              <input
                type="text"
                className="input"
                placeholder="Ej. Jhosmar Alarcón"
                value={formData.full_name}
                onChange={(e) => setFormData({ ...formData, full_name: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="label">Nombre de Usuario (Username)</label>
              <input
                type="text"
                className="input"
                placeholder="Ej. jhosmar"
                value={formData.username}
                onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="label">Correo Electrónico (Registrado en App)</label>
              <input
                type="email"
                className="input"
                placeholder="Ej. jhosmar@gmail.com o jhosmar@nintec.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              />
            </div>

            <div className="form-group">
              <label className="label">Rol a Asignar</label>
              <select
                className="input"
                value={formData.role}
                onChange={(e) => setFormData({ ...formData, role: e.target.value })}
              >
                <option value="customer">Cliente App</option>
                <option value="admin">Administrador</option>
                <option value="super_admin">Super Administrador</option>
              </select>
            </div>

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={() => setIsModalOpen(false)}
                className="btn btn-secondary"
              >
                Cancelar
              </button>
              <button
                type="submit"
                className="btn btn-primary"
              >
                Guardar y Sincronizar
              </button>
            </div>
          </form>
        </Modal>
      )}
    </div>
  )
}
