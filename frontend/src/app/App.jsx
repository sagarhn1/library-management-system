import { AuthProvider } from '../auth/AuthContext'
import { useAuth } from '../auth/useAuth'
import { RequireAuth } from '../auth/RequireAuth'
import { RequireRole } from '../auth/RequireRole'
import { AppShell } from '../components/layout/AppShell'
import { LoginPage } from '../pages/LoginPage'
import { RegisterPage } from '../pages/RegisterPage'
import { CatalogPage } from '../pages/CatalogPage'
import { BookDetailPage } from '../pages/BookDetailPage'
import { MyLibraryPage } from '../pages/MyLibraryPage'
import { ReaderPage } from '../pages/ReaderPage'
import { AdminDashboardPage } from '../pages/AdminDashboardPage'
import { ManageBooksPage } from '../pages/ManageBooksPage'
import { ManageAuthorsPage } from '../pages/ManageAuthorsPage'
import { ManageGenresPage } from '../pages/ManageGenresPage'
import { ManagePublishersPage } from '../pages/ManagePublishersPage'
import { ManageUsersPage } from '../pages/ManageUsersPage'
import { matchRoute, navigate, useRouter } from './router'
import '../App.css'

function NotFound() {
  return (
    <div className="page-content">
      <div style={{ textAlign: 'center', padding: '64px 0' }}>
        <div style={{ fontSize: 64, marginBottom: 16 }}>🔍</div>
        <h1>Page not found</h1>
        <p style={{ color: 'var(--text-muted)', marginTop: 8 }}>
          The page you are looking for does not exist.
        </p>
        <button className="btn btn-primary mt-4" onClick={() => navigate('/catalog')} type="button">
          Back to Catalog
        </button>
      </div>
    </div>
  )
}

function PublicOnly({ children }) {
  const { isAuthenticated } = useAuth()
  if (isAuthenticated) {
    navigate('/catalog')
    return null
  }
  return children
}

function RoutedApp() {
  const { path } = useRouter()

  if (path === '/' || path === '') {
    navigate('/catalog')
    return null
  }

  if (path === '/login') {
    return <PublicOnly><LoginPage /></PublicOnly>
  }

  if (path === '/register') {
    return <PublicOnly><RegisterPage /></PublicOnly>
  }

  // Match /books/:bookId
  const bookDetailMatch = matchRoute('/books/:bookId', path)
  if (bookDetailMatch) {
    return (
      <RequireAuth>
        <AppShell>
          <BookDetailPage />
        </AppShell>
      </RequireAuth>
    )
  }

  // Match /reader/:bookId — full screen, no topbar
  const readerMatch = matchRoute('/reader/:bookId', path)
  if (readerMatch) {
    return (
      <RequireAuth>
        <ReaderPage />
      </RequireAuth>
    )
  }

  const authenticatedRoutes = [
    ['/catalog', <CatalogPage key="catalog" />],
    ['/my-library', <MyLibraryPage key="my-library" />],
    [
      '/admin',
      <RequireRole key="admin" roles={['ADMIN', 'LIBRARIAN']}>
        <AdminDashboardPage />
      </RequireRole>,
    ],
    [
      '/admin/books',
      <RequireRole key="admin-books" roles={['ADMIN', 'LIBRARIAN']}>
        <ManageBooksPage />
      </RequireRole>,
    ],
    [
      '/admin/authors',
      <RequireRole key="admin-authors" roles={['ADMIN', 'LIBRARIAN']}>
        <ManageAuthorsPage />
      </RequireRole>,
    ],
    [
      '/admin/genres',
      <RequireRole key="admin-genres" roles={['ADMIN', 'LIBRARIAN']}>
        <ManageGenresPage />
      </RequireRole>,
    ],
    [
      '/admin/publishers',
      <RequireRole key="admin-publishers" roles={['ADMIN', 'LIBRARIAN']}>
        <ManagePublishersPage />
      </RequireRole>,
    ],
    [
      '/admin/users',
      <RequireRole key="admin-users" roles={['ADMIN']}>
        <ManageUsersPage />
      </RequireRole>,
    ],
  ]

  const matchedRoute = authenticatedRoutes.find(([pattern]) =>
    matchRoute(pattern, path),
  )

  return (
    <RequireAuth>
      <AppShell>
        {matchedRoute?.[1] ?? <NotFound />}
      </AppShell>
    </RequireAuth>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <RoutedApp />
    </AuthProvider>
  )
}
