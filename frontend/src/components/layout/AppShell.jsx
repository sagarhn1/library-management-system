import { useState } from 'react'
import { useAuth } from '../../auth/useAuth'
import { navigate, useRouter } from '../../app/router'
import { Spinner } from '../common/Spinner'
import { BrandLogo, IconLogOut, IconShield } from '../common/Icons'

export function AppShell({ children }) {
  const { user, logout, hasRole } = useAuth()
  const { path } = useRouter()
  const [loggingOut, setLoggingOut] = useState(false)

  async function handleLogout() {
    setLoggingOut(true)
    try {
      await logout()
    } finally {
      navigate('/login')
    }
  }

  function isActive(href) {
    return path === href || path.startsWith(href + '/')
  }

  const initial = user?.email ? user.email.charAt(0).toUpperCase() : 'U'

  return (
    <div className="app-shell">
      <header className="topbar">
        <button
          type="button"
          className="topbar-brand"
          onClick={() => navigate('/catalog')}
        >
          <span className="topbar-brand-icon">
            <BrandLogo size={18} />
          </span>
          <span className="brand-wordmark">
            Vellum<sup className="brand-tm">®</sup>
          </span>
        </button>

        <nav className="topbar-nav">
          <button
            type="button"
            className={`nav-link ${isActive('/catalog') ? 'active' : ''}`}
            onClick={() => navigate('/catalog')}
          >
            Catalog
          </button>
          <button
            type="button"
            className={`nav-link ${isActive('/my-library') ? 'active' : ''}`}
            onClick={() => navigate('/my-library')}
          >
            My Library
          </button>
          {hasRole('ADMIN', 'LIBRARIAN') && (
            <button
              type="button"
              className={`nav-link ${isActive('/admin') ? 'active' : ''}`}
              onClick={() => navigate('/admin')}
            >
              Console
            </button>
          )}
        </nav>

        <div className="topbar-right">
          <div className="user-profile-container">
            <div
              className="user-profile-avatar"
              title={user?.role === 'ADMIN' ? 'Administrator Account' : 'Member Account'}
            >
              <span className="avatar-letter">{initial}</span>
            </div>
            {user?.role === 'ADMIN' && (
              <span className="role-chip ADMIN">
                <IconShield size={12} style={{ marginRight: 4 }} />
                Admin
              </span>
            )}
          </div>

          <button
            type="button"
            className="btn btn-outline btn-sm logout-btn"
            onClick={handleLogout}
            disabled={loggingOut}
            title="Sign out"
          >
            {loggingOut ? (
              <Spinner />
            ) : (
              <>
                <IconLogOut size={15} style={{ marginRight: 6 }} />
                <span>Sign out</span>
              </>
            )}
          </button>
        </div>
      </header>

      <main style={{ flex: 1 }}>
        {children}
      </main>
    </div>
  )
}
