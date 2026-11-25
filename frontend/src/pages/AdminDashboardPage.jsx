import { useState, useEffect, useCallback, useMemo } from 'react'
import { booksApi } from '../api/booksApi'
import { authorsApi } from '../api/authorsApi'
import { genresApi } from '../api/genresApi'
import { publishersApi } from '../api/publishersApi'
import { usersApi } from '../api/usersApi'
import { useAuth } from '../auth/useAuth'
import { navigate } from '../app/router'
import { Spinner } from '../components/common/Spinner'
import { Alert } from '../components/common/Alert'
import {
  IconBookOpen,
  IconEdit,
  IconTag,
  IconBuilding,
  IconUsers,
  IconBarChart,
  IconTrendingUp,
  IconLayers,
  IconRotateCw,
  IconArrowRight,
  IconCreditCard,
} from '../components/common/Icons'

function KpiCard({ icon: Icon, label, value, sub, progress, accent = 'var(--primary)' }) {
  return (
    <div className="kpi-card">
      <div className="kpi-card-top">
        <span className="kpi-label">{label}</span>
        <span className="kpi-icon-wrap" style={{ color: accent, background: `${accent}15` }}>
          <Icon size={18} />
        </span>
      </div>
      <div className="kpi-value">{value ?? '—'}</div>
      {typeof progress === 'number' && (
        <div className="kpi-progress-track">
          <div
            className="kpi-progress-fill"
            style={{ width: `${Math.min(100, Math.max(0, progress))}%`, background: accent }}
          />
        </div>
      )}
      {sub && <div className="kpi-sub">{sub}</div>}
    </div>
  )
}

function DonutChart({ segments, centerValue, centerLabel }) {
  const total = segments.reduce((sum, s) => sum + s.value, 0)
  const radius = 42
  const circumference = 2 * Math.PI * radius
  let cumulative = 0

  return (
    <div className="donut-wrap">
      <svg viewBox="0 0 110 110" className="donut-svg">
        <circle cx="55" cy="55" r={radius} fill="none" stroke="var(--gray-100)" strokeWidth="13" />
        {total > 0 &&
          segments.map((seg) => {
            const pct = seg.value / total
            const dash = pct * circumference
            const offset = -cumulative * circumference
            cumulative += pct
            return (
              <circle
                key={seg.label}
                cx="55"
                cy="55"
                r={radius}
                fill="none"
                stroke={seg.color}
                strokeWidth="13"
                strokeDasharray={`${dash} ${circumference - dash}`}
                strokeDashoffset={offset}
                transform="rotate(-90 55 55)"
                style={{ transition: 'stroke-dasharray 0.45s ease' }}
              />
            )
          })}
        <text x="55" y="53" textAnchor="middle" className="donut-center-val">
          {centerValue}
        </text>
        <text x="55" y="67" textAnchor="middle" className="donut-center-lbl">
          {centerLabel}
        </text>
      </svg>
      <div className="donut-legend">
        {segments.map((seg) => (
          <div key={seg.label} className="donut-legend-item">
            <span className="legend-dot" style={{ background: seg.color }} />
            <span className="legend-label">{seg.label}</span>
            <span className="legend-val">
              {seg.value}{' '}
              <span className="legend-pct">
                {total > 0 ? `${Math.round((seg.value / total) * 100)}%` : '0%'}
              </span>
            </span>
          </div>
        ))}
      </div>
    </div>
  )
}

function ColumnChart({ buckets }) {
  const max = Math.max(1, ...buckets.map((b) => b.count))
  return (
    <div className="col-chart">
      {buckets.map((b) => {
        const heightPct = Math.round((b.count / max) * 100)
        return (
          <div key={b.label} className="col-chart-item">
            <span className="col-chart-val">{b.count}</span>
            <div className="col-chart-track">
              <div
                className="col-chart-bar"
                style={{
                  height: `${Math.max(b.count > 0 ? 12 : 4, heightPct)}%`,
                  background: b.color || 'var(--primary)',
                }}
              />
            </div>
            <span className="col-chart-lbl">{b.label}</span>
          </div>
        )
      })}
    </div>
  )
}

export function AdminDashboardPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')

  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [lastUpdated, setLastUpdated] = useState(null)
  const [data, setData] = useState({
    books: [],
    totalBooks: 0,
    authors: [],
    genres: [],
    publishers: [],
    users: [],
    totalUsers: 0,
  })

  const loadAnalytics = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const requests = [
        booksApi.listBooks({ page: 0, size: 100 }),
        authorsApi.listAuthors(),
        genresApi.listGenres(),
        publishersApi.listPublishers(),
        isAdmin ? usersApi.getUsers({ page: 0, size: 100 }) : Promise.resolve(null),
      ]
      const [booksRes, authorsList, genresList, publishersList, usersRes] = await Promise.all(requests)

      setData({
        books: booksRes?.content ?? [],
        totalBooks: booksRes?.totalElements ?? (booksRes?.content?.length || 0),
        authors: Array.isArray(authorsList) ? authorsList : [],
        genres: Array.isArray(genresList) ? genresList : [],
        publishers: Array.isArray(publishersList) ? publishersList : [],
        users: usersRes?.content ?? [],
        totalUsers: usersRes?.totalElements ?? (usersRes?.content?.length || 0),
      })
      setLastUpdated(new Date())
    } catch (err) {
      setError(err.message || 'Failed to load dashboard analytics.')
    } finally {
      setLoading(false)
    }
  }, [isAdmin])

  useEffect(() => {
    loadAnalytics()
  }, [loadAnalytics])

  const analytics = useMemo(() => {
    const books = data.books
    const activeBooks = books.filter((b) => b.active !== false).length
    const inactiveBooks = books.length - activeBooks
    const activePct = books.length ? Math.round((activeBooks / books.length) * 100) : 0

    const freeBooks = books.filter((b) => b.accessType === 'FREE').length
    const paidBooks = books.filter((b) => b.accessType === 'PAID').length

    const paidPrices = books
      .filter((b) => b.accessType === 'PAID' && Number(b.price) > 0)
      .map((b) => Number(b.price))
    const totalCatalogValue = paidPrices.reduce((a, b) => a + b, 0)
    const avgPrice = paidPrices.length ? totalCatalogValue / paidPrices.length : 0

    // Price distribution buckets
    const priceBuckets = [
      { label: 'Free', count: freeBooks, color: '#0f4c3a' },
      {
        label: '₹1–199',
        count: paidPrices.filter((p) => p > 0 && p < 200).length,
        color: '#1e6b52',
      },
      {
        label: '₹200–499',
        count: paidPrices.filter((p) => p >= 200 && p < 500).length,
        color: '#b45309',
      },
      {
        label: '₹500+',
        count: paidPrices.filter((p) => p >= 500).length,
        color: '#d97706',
      },
    ]

    // Genre & language breakdown
    const genreCounts = {}
    const langCounts = {}
    books.forEach((b) => {
      const g = b.genreName || 'Uncategorized'
      genreCounts[g] = (genreCounts[g] || 0) + 1
      if (b.language) {
        langCounts[b.language] = (langCounts[b.language] || 0) + 1
      }
    })
    const topGenres = Object.entries(genreCounts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)
    const languages = Object.entries(langCounts)
      .sort((a, b) => b[1] - a[1])
      .slice(0, 5)

    // User role & status breakdown
    const roleCounts = { MEMBER: 0, LIBRARIAN: 0, ADMIN: 0 }
    let activeUsers = 0
    data.users.forEach((u) => {
      if (roleCounts[u.role] !== undefined) roleCounts[u.role] += 1
      if (u.active !== false) activeUsers += 1
    })

    const activeAuthors = data.authors.filter((a) => a.active !== false).length
    const activePublishers = data.publishers.filter((p) => p.active !== false).length
    const totalEntities = data.authors.length + data.publishers.length
    const activeEntityPct = totalEntities
      ? Math.round(((activeAuthors + activePublishers) / totalEntities) * 100)
      : 0

    return {
      activeBooks,
      inactiveBooks,
      activePct,
      freeBooks,
      paidBooks,
      avgPrice,
      totalCatalogValue,
      priceBuckets,
      topGenres,
      languages,
      roleCounts,
      activeUsers,
      activeAuthors,
      activePublishers,
      activeEntityPct,
      recentBooks: books.slice(0, 6),
    }
  }, [data])

  const sections = [
    {
      icon: IconBookOpen,
      label: 'Books & E-Books',
      count: data.totalBooks,
      description: 'Upload PDFs, cover art, pricing, and metadata',
      path: '/admin/books',
      visible: true,
    },
    {
      icon: IconEdit,
      label: 'Authors',
      count: data.authors.length,
      description: 'Manage author profiles and active status',
      path: '/admin/authors',
      visible: true,
    },
    {
      icon: IconTag,
      label: 'Genres',
      count: data.genres.length,
      description: 'Organize catalog categories and hierarchy',
      path: '/admin/genres',
      visible: true,
    },
    {
      icon: IconBuilding,
      label: 'Publishers',
      count: data.publishers.length,
      description: 'Administer publishing houses and imprints',
      path: '/admin/publishers',
      visible: true,
    },
    {
      icon: IconUsers,
      label: 'Users & Access',
      count: data.totalUsers,
      description: 'Manage accounts, roles, and permissions',
      path: '/admin/users',
      visible: isAdmin,
    },
  ].filter((s) => s.visible)

  const maxGenreCount = analytics.topGenres.length ? analytics.topGenres[0][1] : 1

  return (
    <div className="page-content fade-in">
      <div className="hero-banner admin-hero-banner">
        <div className="hero-banner-content">
          <div className="hero-status-row">
            <span className="hero-eyebrow">Vellum® Executive Operations</span>
            <span className="status-pill-live">
              <span className="status-dot" />
              System Operational
            </span>
          </div>
          <h1>Operations & Catalog Analytics</h1>
          <p>
            Real-time inventory distribution, pricing telemetry, and institutional resource controls
            {lastUpdated ? ` · Synced ${lastUpdated.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}` : ''}
          </p>
        </div>
        <button
          type="button"
          className="btn btn-hero-light"
          onClick={loadAnalytics}
          disabled={loading}
        >
          <IconRotateCw size={15} className={loading ? 'spin-icon' : ''} />
          <span>{loading ? 'Syncing…' : 'Refresh Analytics'}</span>
        </button>
      </div>

      {error && (
        <div className="mb-4">
          <Alert type="error">{error}</Alert>
        </div>
      )}

      {loading && data.books.length === 0 ? (
        <div className="loading-overlay show" style={{ minHeight: 280 }}>
          <Spinner size="lg" />
          <span>Aggregating platform analytics…</span>
        </div>
      ) : (
        <>
          {/* Executive KPI Summary Row */}
          <div className="kpi-grid">
            <KpiCard
              icon={IconBookOpen}
              label="Total Catalog Titles"
              value={data.totalBooks.toLocaleString()}
              progress={analytics.activePct}
              sub={`${analytics.activeBooks} active (${analytics.activePct}%) · ${analytics.inactiveBooks} archived`}
              accent="#0f4c3a"
            />
            <KpiCard
              icon={IconTrendingUp}
              label="Avg. License Price"
              value={`₹${analytics.avgPrice.toFixed(0)}`}
              progress={data.books.length ? (analytics.paidBooks / data.books.length) * 100 : 0}
              sub={`Catalog value ₹${analytics.totalCatalogValue.toLocaleString('en-IN')} · ${analytics.paidBooks} paid`}
              accent="#b45309"
            />
            <KpiCard
              icon={IconLayers}
              label="Publishing Network"
              value={(data.authors.length + data.publishers.length).toLocaleString()}
              progress={analytics.activeEntityPct}
              sub={`${analytics.activeAuthors} active authors · ${analytics.activePublishers} publishers`}
              accent="#1e6b52"
            />
            {isAdmin ? (
              <KpiCard
                icon={IconUsers}
                label="Platform Accounts"
                value={data.totalUsers.toLocaleString()}
                progress={data.users.length ? (analytics.activeUsers / data.users.length) * 100 : 0}
                sub={`${analytics.roleCounts.MEMBER} members · ${analytics.roleCounts.LIBRARIAN + analytics.roleCounts.ADMIN} staff`}
                accent="#0f4c3a"
              />
            ) : (
              <KpiCard
                icon={IconTag}
                label="Taxonomy Categories"
                value={data.genres.length.toLocaleString()}
                sub="Active subject classifications"
                accent="#b45309"
              />
            )}
          </div>

          {/* Visual Analytics Suite */}
          <div className="analytics-grid">
            <div className="analytics-card">
              <div className="analytics-card-header">
                <div>
                  <h3>Monetization & Availability</h3>
                  <p>Free access vs. paid publication licenses</p>
                </div>
                <IconBarChart size={18} style={{ color: 'var(--gray-400)' }} />
              </div>
              <DonutChart
                centerValue={data.books.length}
                centerLabel="Titles"
                segments={[
                  { label: 'Free Access', value: analytics.freeBooks, color: '#0f4c3a' },
                  { label: 'Paid License', value: analytics.paidBooks, color: '#d97706' },
                ]}
              />
              <div className="analytics-footer-bar">
                <span className="text-sm text-muted">Catalog Readiness</span>
                <span className="text-sm" style={{ fontWeight: 600, color: 'var(--primary)' }}>
                  {analytics.activePct}% Published
                </span>
              </div>
            </div>

            <div className="analytics-card">
              <div className="analytics-card-header">
                <div>
                  <h3>Price Tier Distribution</h3>
                  <p>Volume breakdown across pricing brackets</p>
                </div>
                <IconCreditCard size={18} style={{ color: 'var(--gray-400)' }} />
              </div>
              <ColumnChart buckets={analytics.priceBuckets} />
            </div>

            <div className="analytics-card">
              <div className="analytics-card-header">
                <div>
                  <h3>Top Subject Genres</h3>
                  <p>Leading categories & language coverage</p>
                </div>
                <IconTag size={18} style={{ color: 'var(--gray-400)' }} />
              </div>
              {analytics.topGenres.length === 0 ? (
                <p className="text-sm text-muted">No genre data available yet.</p>
              ) : (
                <div className="hbar-list">
                  {analytics.topGenres.map(([name, count], idx) => (
                    <div key={name} className="hbar-item">
                      <div className="hbar-meta">
                        <span className="hbar-name">
                          <span className="hbar-rank">0{idx + 1}</span>
                          {name}
                        </span>
                        <span className="hbar-count">{count} titles</span>
                      </div>
                      <div className="hbar-track">
                        <div
                          className="hbar-fill"
                          style={{ width: `${Math.max(8, (count / maxGenreCount) * 100)}%` }}
                        />
                      </div>
                    </div>
                  ))}
                </div>
              )}
              {analytics.languages.length > 0 && (
                <div className="lang-pills-row">
                  {analytics.languages.map(([lang, count]) => (
                    <span key={lang} className="lang-chip">
                      {lang} · {count}
                    </span>
                  ))}
                </div>
              )}
            </div>

            {isAdmin && (
              <div className="analytics-card">
                <div className="analytics-card-header">
                  <div>
                    <h3>Account Role Distribution</h3>
                    <p>Access tiers & active member ratio</p>
                  </div>
                  <IconUsers size={18} style={{ color: 'var(--gray-400)' }} />
                </div>
                <DonutChart
                  centerValue={data.totalUsers}
                  centerLabel="Accounts"
                  segments={[
                    { label: 'Members', value: analytics.roleCounts.MEMBER, color: '#0f4c3a' },
                    { label: 'Librarians', value: analytics.roleCounts.LIBRARIAN, color: '#334155' },
                    { label: 'Admins', value: analytics.roleCounts.ADMIN, color: '#b45309' },
                  ]}
                />
                <div className="analytics-footer-bar">
                  <span className="text-sm text-muted">Active Accounts</span>
                  <span className="text-sm" style={{ fontWeight: 600, color: 'var(--primary)' }}>
                    {analytics.activeUsers} / {data.users.length} Active
                  </span>
                </div>
              </div>
            )}
          </div>

          {/* Management Modules */}
          <div className="section-heading-row">
            <div>
              <h2>Administration Modules</h2>
              <p className="text-sm text-muted">Direct operational access to all library entities</p>
            </div>
          </div>

          <div className="admin-modules-grid">
            {sections.map((s) => {
              const SectionIcon = s.icon
              return (
                <button
                  key={s.path}
                  type="button"
                  className="admin-module-card"
                  onClick={() => navigate(s.path)}
                >
                  <div className="admin-module-top">
                    <span className="admin-module-icon">
                      <SectionIcon size={20} />
                    </span>
                    <span className="admin-module-count">{s.count}</span>
                  </div>
                  <div className="admin-module-title">{s.label}</div>
                  <div className="admin-module-desc">{s.description}</div>
                  <div className="admin-module-cta">
                    <span>Open Module</span>
                    <IconArrowRight size={14} />
                  </div>
                </button>
              )
            })}
          </div>

          {/* Catalog Snapshot Table */}
          {analytics.recentBooks.length > 0 && (
            <div className="card mt-4" style={{ marginTop: 28 }}>
              <div
                className="card-body"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  borderBottom: '1px solid var(--border)',
                  padding: '16px 20px',
                  flexWrap: 'wrap',
                  gap: 12,
                }}
              >
                <div>
                  <h3 style={{ fontSize: 15, fontWeight: 600 }}>Catalog Inventory Snapshot</h3>
                  <p className="text-sm text-muted">Latest publications indexed in the repository</p>
                </div>
                <button
                  type="button"
                  className="btn btn-outline btn-sm"
                  onClick={() => navigate('/admin/books')}
                >
                  Manage All Books
                </button>
              </div>
              <div className="table-wrap" style={{ border: 'none', borderRadius: 0 }}>
                <table className="table">
                  <thead>
                    <tr>
                      <th>Publication Title</th>
                      <th>Author</th>
                      <th>Genre</th>
                      <th>Access Tier</th>
                      <th>Price</th>
                      <th>Status</th>
                      <th style={{ textAlign: 'right' }}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {analytics.recentBooks.map((b) => (
                      <tr key={b.id}>
                        <td style={{ fontWeight: 500, color: 'var(--text-heading)' }}>{b.title}</td>
                        <td>{b.authorName || '—'}</td>
                        <td>{b.genreName || '—'}</td>
                        <td>
                          <span className={`badge ${b.accessType === 'FREE' ? 'badge-free' : 'badge-paid'}`}>
                            {b.accessType}
                          </span>
                        </td>
                        <td style={{ fontVariantNumeric: 'tabular-nums' }}>
                          {b.accessType === 'FREE' ? 'Free' : `₹${Number(b.price || 0).toFixed(2)}`}
                        </td>
                        <td>
                          <span className={`badge ${b.active ? 'badge-active' : 'badge-inactive'}`}>
                            {b.active ? 'Active' : 'Inactive'}
                          </span>
                        </td>
                        <td style={{ textAlign: 'right' }}>
                          <button
                            type="button"
                            className="btn btn-ghost btn-sm"
                            onClick={() => navigate(`/books/${b.id}`)}
                          >
                            Inspect
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </>
      )}
    </div>
  )
}
