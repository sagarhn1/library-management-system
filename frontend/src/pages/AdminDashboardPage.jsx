import { useState, useEffect } from 'react'
import { booksApi } from '../api/booksApi'
import { authorsApi } from '../api/authorsApi'
import { genresApi } from '../api/genresApi'
import { navigate } from '../app/router'
import {
  IconBookOpen,
  IconEdit,
  IconTag,
  IconBuilding,
  IconUsers,
} from '../components/common/Icons'

function StatCard({ icon: Icon, label, value, sub }) {
  return (
    <div className="stat-card">
      <div style={{ color: 'var(--primary)', marginBottom: 12 }}>
        <Icon size={28} />
      </div>
      <div className="stat-label">{label}</div>
      <div className="stat-value">{value ?? '—'}</div>
      {sub && <div className="stat-sub">{sub}</div>}
    </div>
  )
}

export function AdminDashboardPage() {
  const [stats, setStats] = useState({ books: null, authors: null, genres: null })

  useEffect(() => {
    booksApi.listBooks({ size: 1 }).then((res) =>
      setStats((s) => ({ ...s, books: res.totalElements }))
    ).catch(() => {})
    authorsApi.listAuthors().then((list) =>
      setStats((s) => ({ ...s, authors: list.length }))
    ).catch(() => {})
    genresApi.listGenres().then((list) =>
      setStats((s) => ({ ...s, genres: list.length }))
    ).catch(() => {})
  }, [])

  const sections = [
    {
      icon: IconBookOpen,
      label: 'Books',
      description: 'Manage the book catalog',
      path: '/admin/books',
    },
    {
      icon: IconEdit,
      label: 'Authors',
      description: 'Manage author records',
      path: '/admin/authors',
    },
    {
      icon: IconTag,
      label: 'Genres',
      description: 'Manage genres and categories',
      path: '/admin/genres',
    },
    {
      icon: IconBuilding,
      label: 'Publishers',
      description: 'Manage publishing entities',
      path: '/admin/publishers',
    },
    {
      icon: IconUsers,
      label: 'Users',
      description: 'Manage member & staff accounts',
      path: '/admin/users',
    },
  ]

  return (
    <div className="page-content fade-in">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Admin Dashboard</h1>
          <p>System configuration & catalog administration</p>
        </div>
      </div>

      <div className="stats-grid">
        <StatCard icon={IconBookOpen} label="Total Books" value={stats.books} />
        <StatCard icon={IconEdit} label="Authors" value={stats.authors} />
        <StatCard icon={IconTag} label="Genres" value={stats.genres} />
      </div>

      <h2 style={{ marginBottom: 16, fontSize: 16, fontWeight: 600 }}>Management Sections</h2>
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fill, minmax(220px, 1fr))',
          gap: 16,
        }}
      >
        {sections.map((s) => {
          const SectionIcon = s.icon
          return (
            <button
              key={s.path}
              type="button"
              className="card admin-nav-card"
              style={{
                cursor: 'pointer',
                textAlign: 'left',
                border: '1px solid var(--border)',
                background: 'var(--surface)',
                borderRadius: 'var(--radius)',
                transition: 'all 0.2s ease',
              }}
              onClick={() => navigate(s.path)}
            >
              <div className="card-body">
                <div style={{ color: 'var(--primary)', marginBottom: 12 }}>
                  <SectionIcon size={28} />
                </div>
                <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 4 }}>{s.label}</h3>
                <p style={{ fontSize: 13, color: 'var(--text-muted)' }}>{s.description}</p>
              </div>
            </button>
          )
        })}
      </div>
    </div>
  )
}
