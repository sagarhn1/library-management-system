import { useState, useEffect } from 'react'
import { accessApi } from '../api/accessApi'
import { EmptyState } from '../components/common/EmptyState'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { navigate } from '../app/router'
import { IconBookOpen } from '../components/common/Icons'

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-IN', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  })
}

function daysLeft(isoExpires) {
  if (!isoExpires) return null
  const diff = new Date(isoExpires) - new Date()
  if (diff < 0) return 0
  return Math.ceil(diff / 86400000)
}

export function MyLibraryPage() {
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    accessApi.getMyLibrary()
      .then(setItems)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [])

  if (loading) {
    return (
      <div className="page-content fade-in">
        <div className="loading-overlay show">
          <Spinner size="lg" />
          <span>Loading your library…</span>
        </div>
      </div>
    )
  }

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div className="page-header-text">
          <h1>My Library</h1>
          <p>Books you currently have access to read</p>
        </div>
        <button
          className="btn btn-outline"
          onClick={() => navigate('/catalog')}
          type="button"
        >
          Browse Catalog
        </button>
      </div>

      {error && (
        <div className="mb-4">
          <Alert type="error">{error}</Alert>
        </div>
      )}

      {!loading && items.length === 0 && !error ? (
        <EmptyState
          title="Your library is empty"
          message="Browse the catalog to get access to free and premium books."
          action={
            <button className="btn btn-primary" onClick={() => navigate('/catalog')}>
              Browse Catalog
            </button>
          }
        />
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          {items.map((item) => {
            const days = daysLeft(item.accessExpiresAt)
            const expiringSoon = days !== null && days <= 7 && days > 0
            const expired = days === 0

            return (
              <div key={item.id} className="library-card">
                <div
                  style={{
                    width: 44,
                    height: 60,
                    background: 'var(--primary-light)',
                    borderRadius: 6,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: 'var(--primary)',
                    flexShrink: 0,
                  }}
                >
                  <IconBookOpen size={22} />
                </div>
                <div className="library-card-info">
                  <div className="library-card-title">{item.bookTitle || `Book #${item.bookId}`}</div>
                  <div className="library-card-meta">
                    <span>Access granted: {formatDate(item.accessGrantedAt)}</span>
                    {item.accessExpiresAt && (
                      <span
                        style={{
                          marginLeft: 12,
                          color: expiringSoon
                            ? 'var(--warning)'
                            : expired
                            ? 'var(--danger)'
                            : 'inherit',
                          fontWeight: expiringSoon || expired ? 600 : 400,
                        }}
                      >
                        {expired
                          ? 'Expired'
                          : `Expires: ${formatDate(item.accessExpiresAt)} (${days}d left)`}
                      </span>
                    )}
                  </div>
                </div>
                <div className="flex gap-2" style={{ alignItems: 'center' }}>
                  {expired ? (
                    <span className="badge badge-expired">Expired</span>
                  ) : expiringSoon ? (
                    <span className="badge badge-expired">Expiring Soon</span>
                  ) : (
                    <span className="badge badge-active">Active</span>
                  )}
                  {!expired && (
                    <button
                      type="button"
                      className="btn btn-primary btn-sm"
                      onClick={() => navigate(`/reader/${item.bookId}`)}
                    >
                      <IconBookOpen size={14} style={{ marginRight: 5 }} />
                      Read
                    </button>
                  )}
                </div>
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
