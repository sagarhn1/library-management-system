import { useState, useEffect, useCallback } from 'react'
import { authorsApi } from '../api/authorsApi'
import { useAuth } from '../auth/useAuth'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { EmptyState } from '../components/common/EmptyState'
import { navigate } from '../app/router'

const EMPTY_FORM = { name: '', biography: '', dateOfBirth: '', nationality: '', email: '' }

export function ManageAuthorsPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')

  const [authors, setAuthors] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [modal, setModal] = useState(null)
  const [editAuthor, setEditAuthor] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [modalError, setModalError] = useState('')
  const [saving, setSaving] = useState(false)

  const fetchAuthors = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setAuthors(await authorsApi.listAuthors())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchAuthors() }, [fetchAuthors])

  function updateField(e) {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }))
  }

  function openCreate() {
    setForm(EMPTY_FORM)
    setEditAuthor(null)
    setModalError('')
    setModal('form')
  }

  function openEdit(author) {
    setEditAuthor(author)
    setForm({
      name: author.name || '',
      biography: author.biography || '',
      dateOfBirth: author.dateOfBirth || '',
      nationality: author.nationality || '',
      email: author.email || '',
    })
    setModalError('')
    setModal('form')
  }

  async function handleSave() {
    setSaving(true)
    setModalError('')
    try {
      if (editAuthor) {
        await authorsApi.updateAuthor(editAuthor.id, form)
        setSuccess('Author updated.')
      } else {
        await authorsApi.createAuthor(form)
        setSuccess('Author created.')
      }
      setModal(null)
      fetchAuthors()
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(author) {
    if (!confirm(`Delete author "${author.name}"?`)) return
    try {
      await authorsApi.deleteAuthor(author.id)
      setSuccess('Author deleted.')
      fetchAuthors()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggleActive(author) {
    try {
      if (author.active) {
        await authorsApi.deactivateAuthor(author.id)
      } else {
        await authorsApi.activateAuthor(author.id)
      }
      setSuccess(`Author ${author.active ? 'deactivated' : 'activated'}.`)
      fetchAuthors()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Manage Authors</h1>
          <p>{authors.length} authors total</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-outline btn-sm" onClick={() => navigate('/admin')} type="button">← Admin</button>
          <button className="btn btn-primary" onClick={openCreate} type="button">+ Add Author</button>
        </div>
      </div>

      {success && <div className="mb-4"><Alert type="success">{success}</Alert></div>}
      {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}

      {loading ? (
        <div className="loading-overlay"><Spinner size="lg" /></div>
      ) : authors.length === 0 ? (
        <EmptyState icon="✍️" title="No authors yet" message="Create your first author." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Nationality</th>
                <th>Email</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {authors.map((a) => (
                <tr key={a.id}>
                  <td>
                    <div style={{ fontWeight: 500, color: 'var(--text-heading)' }}>{a.name}</div>
                    {a.biography && <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{a.biography.slice(0, 60)}…</div>}
                  </td>
                  <td>{a.nationality || '—'}</td>
                  <td>{a.email || '—'}</td>
                  <td>
                    <span className={`badge ${a.active !== false ? 'badge-active' : 'badge-inactive'}`}>
                      {a.active !== false ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div className="flex gap-2">
                      <button className="btn btn-outline btn-sm" onClick={() => openEdit(a)} type="button">Edit</button>
                      <button
                        className={`btn btn-sm ${a.active !== false ? 'btn-danger-outline' : 'btn-outline'}`}
                        onClick={() => handleToggleActive(a)}
                        type="button"
                      >
                        {a.active !== false ? 'Deactivate' : 'Activate'}
                      </button>
                      {isAdmin && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(a)} type="button">Delete</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={modal === 'form'} onClose={() => setModal(null)} title={editAuthor ? 'Edit Author' : 'Add Author'}>
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="form-grid mt-3">
            <div className="field">
              <label className="field-label" htmlFor="af-name">Name <span className="required">*</span></label>
              <input id="af-name" className="input" name="name" value={form.name} onChange={updateField} required />
            </div>
            <div className="form-grid-2">
              <div className="field">
                <label className="field-label" htmlFor="af-nat">Nationality</label>
                <input id="af-nat" className="input" name="nationality" value={form.nationality} onChange={updateField} />
              </div>
              <div className="field">
                <label className="field-label" htmlFor="af-dob">Date of Birth</label>
                <input id="af-dob" className="input" name="dateOfBirth" type="date" value={form.dateOfBirth} onChange={updateField} />
              </div>
            </div>
            <div className="field">
              <label className="field-label" htmlFor="af-email">Email</label>
              <input id="af-email" className="input" name="email" type="email" value={form.email} onChange={updateField} />
            </div>
            <div className="field">
              <label className="field-label" htmlFor="af-bio">Biography</label>
              <textarea id="af-bio" className="textarea" name="biography" value={form.biography} onChange={updateField} />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} type="button">
            {saving ? <Spinner /> : editAuthor ? 'Save Changes' : 'Create Author'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
