import React, { useState, useRef } from 'react'
import { UploadCloud, Link as LinkIcon, Trash2, CheckCircle2, Loader2, Image as ImageIcon } from 'lucide-react'
import { adminService } from '../services/adminService'

export default function ImageUploader({
  bucket = 'product-images',
  folder = '',
  currentUrl = '',
  onImageUploaded,
  onImageRemoved,
  label = 'Imagen del Artículo'
}) {
  const [isUploading, setIsUploading] = useState(false)
  const [uploadError, setUploadError] = useState(null)
  const [urlMode, setUrlMode] = useState(false)
  const [manualUrl, setManualUrl] = useState('')
  const fileInputRef = useRef(null)

  const handleFileChange = async (e) => {
    const file = e.target.files?.[0]
    if (!file) return

    setIsUploading(true)
    setUploadError(null)

    try {
      const { publicUrl } = await adminService.uploadStorageFile(bucket, file, folder)
      onImageUploaded(publicUrl)
    } catch (err) {
      console.error('Error al subir imagen a Supabase Storage:', err)
      setUploadError(err.message || 'Error al subir la imagen. Verifica las políticas del bucket.')
    } finally {
      setIsUploading(false)
      if (fileInputRef.current) fileInputRef.current.value = ''
    }
  }

  const handleManualUrlSubmit = (e) => {
    e.preventDefault()
    if (manualUrl.trim()) {
      onImageUploaded(manualUrl.trim())
      setManualUrl('')
      setUrlMode(false)
    }
  }

  return (
    <div className="form-group">
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
        <label className="form-label" style={{ margin: 0 }}>{label}</label>
        <button
          type="button"
          onClick={() => setUrlMode(!urlMode)}
          style={{
            background: 'none',
            border: 'none',
            color: 'var(--accent-cyan)',
            fontSize: '0.75rem',
            cursor: 'pointer',
            display: 'flex',
            alignItems: 'center',
            gap: '4px'
          }}
        >
          <LinkIcon size={12} />
          {urlMode ? 'Subir archivo' : 'Ingresar URL directa'}
        </button>
      </div>

      {currentUrl ? (
        <div style={{
          position: 'relative',
          borderRadius: 'var(--radius-md)',
          overflow: 'hidden',
          border: '1px solid var(--border-medium)',
          aspectRatio: '16/9',
          maxHeight: '200px',
          background: '#0D1527',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          <img
            src={currentUrl}
            alt="Preview"
            style={{ width: '100%', height: '100%', objectFit: 'contain' }}
            onError={(e) => {
              e.currentTarget.src = 'https://placehold.co/600x400/18223C/FFFFFF?text=Imagen+No+Disponible'
            }}
          />
          <div style={{
            position: 'absolute',
            top: '8px',
            right: '8px',
            display: 'flex',
            gap: '6px'
          }}>
            <button
              type="button"
              onClick={onImageRemoved}
              className="btn btn-danger btn-icon btn-sm"
              title="Eliminar imagen"
            >
              <Trash2 size={14} />
            </button>
          </div>
        </div>
      ) : urlMode ? (
        <div style={{ display: 'flex', gap: '8px' }}>
          <input
            type="url"
            className="input"
            placeholder="https://ejemplo.com/imagen.jpg"
            value={manualUrl}
            onChange={(e) => setManualUrl(e.target.value)}
          />
          <button
            type="button"
            onClick={handleManualUrlSubmit}
            className="btn btn-primary btn-sm"
          >
            Usar
          </button>
        </div>
      ) : (
        <div
          onClick={() => fileInputRef.current?.click()}
          style={{
            border: '2px dashed var(--border-medium)',
            borderRadius: 'var(--radius-md)',
            padding: '28px 16px',
            textAlign: 'center',
            cursor: isUploading ? 'not-allowed' : 'pointer',
            background: 'rgba(255, 255, 255, 0.01)',
            transition: 'all var(--transition-fast)'
          }}
          onMouseEnter={(e) => (e.currentTarget.style.borderColor = 'var(--primary)')}
          onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'var(--border-medium)')}
        >
          <input
            type="file"
            ref={fileInputRef}
            onChange={handleFileChange}
            accept="image/*"
            style={{ display: 'none' }}
            disabled={isUploading}
          />
          {isUploading ? (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '10px' }}>
              <Loader2 size={32} className="animate-spin" color="var(--primary)" />
              <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                Subiendo a Supabase Storage ({bucket})...
              </span>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: '8px' }}>
              <div style={{
                width: '44px',
                height: '44px',
                borderRadius: '50%',
                background: 'rgba(24, 70, 215, 0.1)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#60A5FA'
              }}>
                <UploadCloud size={24} />
              </div>
              <div style={{ fontSize: '0.88rem', fontWeight: '500', color: 'var(--text-main)' }}>
                Haz clic para subir imagen
              </div>
              <div style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>
                PNG, JPG, WEBP hasta 5MB (Bucket: {bucket})
              </div>
            </div>
          )}
        </div>
      )}

      {uploadError && (
        <div style={{ color: '#F87171', fontSize: '0.75rem', marginTop: '6px' }}>
          {uploadError}
        </div>
      )}
    </div>
  )
}
