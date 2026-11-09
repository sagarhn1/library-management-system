import { useState, useEffect, useCallback } from 'react'
import { usersApi } from '../api/usersApi'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { EmptyState } from '../components/common/EmptyState'
import { Pagination } from '../components/common/Pagination'
import { navigate } from '../app/router'

const PAGE_SIZE = 10
const ROLES = ['ADMIN', 'LIBRARIAN', 'MEMBER']

function formatDate(iso) {
  if (!iso) return '—'
  return new Date(iso).toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' })
}

export function ManageUsersPage() {
  const [users, setUsers] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Filters
  const [filterRole, setFilterRole] = useState('')
  const [filterEmail, setFilterEmail] = useState('')

  // Modal
  const [modal, setModal] = useState(null)
  const [editUser, setEditUser] = useState(null)
  const [form, setForm] = useState({ firstName: '', lastName: '', email: '', role: 'MEMBER', active: true })
  const [modalError, setModalError] = useState('')
  const [saving, setSaving] = useState(false)

  const fetchUsers = useCallback(async (p) => {
    setLoading(true)
    setError('')
    try {
      const res = await usersApi.getUsers({
        page: p,
        size: PAGE_SIZE,
        role: filterRole || undefined,
        email: filterEmail || undefined,
        sortBy: 'createdAt',
        direction: 'desc',
      })
      setUsers(res.content ?? [])
      setTotalPages(res.totalPages ?? 0)
      setTotalElements(res.totalElements ?? 0)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [filterRole, filterEmail])

  useEffect(() => { fetchUsers(page) }, [fetchUsers, page])

  function updateField(e) {
    const { name, value, type, checked } = e.target
    setForm((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
  }

  function openCreate() {
    setForm({ firstName: '', lastName: '', email: '', phone: '', password: '', role: 'MEMBER' })
    setEditUser(null)
    setModalError('')
    setModal('form')
  }

  function openEdit(user) {
    setEditUser(user)
    setForm({
      firstName: user.firstName || '',
      lastName: user.lastName || '',
      email: user.email || '',
      phone: user.phone || '',
      role: user.role || 'MEMBER',
      active: user.active !== false,
    })
    setModalError('')
    setModal('form')
  }

  async function handleSave() {
    setSaving(true)
    setModalError('')
    try {
      if (editUser) {
        await usersApi.updateUser(editUser.id, form)
        setSuccess('User updated.')
      } else {
        await usersApi.createUser(form)
        setSuccess('User created.')
      }
      setModal(null)
      fetchUsers(page)
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(user) {
    if (!confirm(`Delete user "${user.email}"? This is irreversible.`)) return
    try {
      await usersApi.deleteUser(user.id)
      setSuccess('User deleted.')
      fetchUsers(page)
    } catch (err) {
      setError(err.message)
    }
  }

  function handleFilterSearch(e) {
    e.preventDefault()
    setPage(0)
    fetchUsers(0)
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Manage Users</h1>
          <p>{totalElements} users total</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-outline btn-sm" onClick={() => navigate('/admin')} type="button">← Admin</button>
          <button className="btn btn-primary" onClick={openCreate} type="button">+ Add User</button>
        </div>
      </div>

      {success && <div className="mb-4"><Alert type="success">{success}</Alert></div>}
      {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}

      {/* Filters */}
      <form className="filters-bar" onSubmit={handleFilterSearch} style={{ marginBottom: 16 }}>
        <input
          className="input"
          placeholder="Search by email…"
          value={filterEmail}
          onChange={(e) => setFilterEmail(e.target.value)}
          style={{ flex: 1, minWidth: 180 }}
        />
        <select
          className="select"
          style={{ minWidth: 130 }}
          value={filterRole}
          onChange={(e) => setFilterRole(e.target.value)}
          aria-label="Filter by role"
        >
          <option value="">All Roles</option>
          {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
        </select>
        <button type="submit" className="btn btn-primary btn-sm">Search</button>
        <button
          type="button"
          className="btn btn-outline btn-sm"
          onClick={() => { setFilterRole(''); setFilterEmail(''); setPage(0) }}
        >
          Reset
        </button>
      </form>

      {loading ? (
        <div className="loading-overlay"><Spinner size="lg" /></div>
      ) : users.length === 0 ? (
        <EmptyState icon="👥" title="No users found" />
      ) : (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>Role</th>
                  <th>Joined</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {users.map((u) => (
                  <tr key={u.id}>
                    <td style={{ fontWeight: 500, color: 'var(--text-heading)' }}>
                      {u.firstName} {u.lastName}
                    </td>
                    <td>{u.email}</td>
                    <td>{u.phone || '—'}</td>
                    <td><span className={`role-chip ${u.role}`}>{u.role}</span></td>
                    <td style={{ fontSize: 12 }}>{formatDate(u.createdAt)}</td>
                    <td>
                      <span className={`badge ${u.active !== false ? 'badge-active' : 'badge-inactive'}`}>
                        {u.active !== false ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        <button className="btn btn-outline btn-sm" onClick={() => openEdit(u)} type="button">Edit</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(u)} type="button">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
        </>
      )}

      <Modal isOpen={modal === 'form'} onClose={() => setModal(null)} title={editUser ? 'Edit User' : 'Add User'}>
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="form-grid mt-3">
            <div className="form-grid-2">
              <div className="field">
                <label className="field-label" htmlFor="uf-fn">First Name <span className="required">*</span></label>
                <input id="uf-fn" className="input" name="firstName" value={form.firstName} onChange={updateField} required />
              </div>
              <div className="field">
                <label className="field-label" htmlFor="uf-ln">Last Name <span className="required">*</span></label>
                <input id="uf-ln" className="input" name="lastName" value={form.lastName} onChange={updateField} required />
              </div>
            </div>
            <div className="field">
              <label className="field-label" htmlFor="uf-email">Email <span className="required">*</span></label>
              <input id="uf-email" className="input" name="email" type="email" value={form.email} onChange={updateField} required disabled={!!editUser} />
            </div>
            <div className="form-grid-2">
              <div className="field">
                <label className="field-label" htmlFor="uf-phone">Phone</label>
                <input id="uf-phone" className="input" name="phone" value={form.phone || ''} onChange={updateField} />
              </div>
              <div className="field">
                <label className="field-label" htmlFor="uf-role">Role <span className="required">*</span></label>
                <select id="uf-role" className="select" name="role" value={form.role} onChange={updateField}>
                  {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
            </div>
            {!editUser && (
              <div className="field">
                <label className="field-label" htmlFor="uf-pass">Password <span className="required">*</span></label>
                <input id="uf-pass" className="input" name="password" type="password" value={form.password || ''} onChange={updateField} required minLength={8} />
              </div>
            )}
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} type="button">
            {saving ? <Spinner /> : editUser ? 'Save Changes' : 'Create User'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
