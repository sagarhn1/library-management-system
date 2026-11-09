import { useState, useEffect, useCallback } from 'react'
import { genresApi } from '../api/genresApi'
import { useAuth } from '../auth/useAuth'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { EmptyState } from '../components/common/EmptyState'
import { navigate } from '../app/router'

const EMPTY_FORM = { code: '', name: '', description: '', active: true, parentGenreId: '' }

export function ManageGenresPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')

  const [genres, setGenres] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [modal, setModal] = useState(null)
  const [editGenre, setEditGenre] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [modalError, setModalError] = useState('')
  const [saving, setSaving] = useState(false)

  const fetchGenres = useCallback(async () => {
    setLoading(true)
    try {
      setGenres(await genresApi.listGenres())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchGenres() }, [fetchGenres])

  function updateField(e) {
    const { name, value, type, checked } = e.target
    setForm((f) => ({ ...f, [name]: type === 'checkbox' ? checked : value }))
  }

  function openCreate() {
    setForm(EMPTY_FORM)
    setEditGenre(null)
    setModalError('')
    setModal('form')
  }

  function openEdit(genre) {
    setEditGenre(genre)
    setForm({
      code: genre.code || '',
      name: genre.name || '',
      description: genre.description || '',
      active: genre.active !== false,
      parentGenreId: genre.parentGenreId || '',
    })
    setModalError('')
    setModal('form')
  }

  async function handleSave() {
    setSaving(true)
    setModalError('')
    const payload = {
      ...form,
      parentGenreId: form.parentGenreId ? Number(form.parentGenreId) : null,
    }
    try {
      if (editGenre) {
        await genresApi.updateGenre(editGenre.id, payload)
        setSuccess('Genre updated.')
      } else {
        await genresApi.createGenre(payload)
        setSuccess('Genre created.')
      }
      setModal(null)
      fetchGenres()
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(genre) {
    if (!confirm(`Delete genre "${genre.name}"?`)) return
    try {
      await genresApi.deleteGenre(genre.id)
      setSuccess('Genre deleted.')
      fetchGenres()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggle(genre) {
    try {
      if (genre.active !== false) {
        await genresApi.deactivateGenre(genre.id)
      } else {
        await genresApi.activateGenre(genre.id)
      }
      setSuccess(`Genre ${genre.active !== false ? 'deactivated' : 'activated'}.`)
      fetchGenres()
    } catch (err) {
      setError(err.message)
    }
  }

  // Top-level genres for parent selector
  const topLevel = genres.filter((g) => !g.parentGenreId)

  return (
    <div className="page-content">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Manage Genres</h1>
          <p>{genres.length} genres total</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-outline btn-sm" onClick={() => navigate('/admin')} type="button">← Admin</button>
          <button className="btn btn-primary" onClick={openCreate} type="button">+ Add Genre</button>
        </div>
      </div>

      {success && <div className="mb-4"><Alert type="success">{success}</Alert></div>}
      {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}

      {loading ? (
        <div className="loading-overlay"><Spinner size="lg" /></div>
      ) : genres.length === 0 ? (
        <EmptyState icon="🏷️" title="No genres yet" message="Create your first genre." />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Parent</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {genres.map((g) => (
                <tr key={g.id}>
                  <td><span style={{ fontFamily: 'var(--font-mono)', fontSize: 12, background: 'var(--gray-100)', padding: '2px 6px', borderRadius: 4 }}>{g.code}</span></td>
                  <td style={{ fontWeight: 500, color: 'var(--text-heading)' }}>
                    {g.parentGenreId ? <span style={{ color: 'var(--text-muted)', marginRight: 6 }}>↳</span> : null}
                    {g.name}
                  </td>
                  <td>
                    {g.parentGenreId
                      ? genres.find((x) => x.id === g.parentGenreId)?.name || `#${g.parentGenreId}`
                      : '—'}
                  </td>
                  <td>
                    <span className={`badge ${g.active !== false ? 'badge-active' : 'badge-inactive'}`}>
                      {g.active !== false ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div className="flex gap-2">
                      <button className="btn btn-outline btn-sm" onClick={() => openEdit(g)} type="button">Edit</button>
                      <button
                        className={`btn btn-sm ${g.active !== false ? 'btn-danger-outline' : 'btn-outline'}`}
                        onClick={() => handleToggle(g)}
                        type="button"
                      >
                        {g.active !== false ? 'Deactivate' : 'Activate'}
                      </button>
                      {isAdmin && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(g)} type="button">Delete</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={modal === 'form'} onClose={() => setModal(null)} title={editGenre ? 'Edit Genre' : 'Add Genre'}>
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="form-grid mt-3">
            <div className="form-grid-2">
              <div className="field">
                <label className="field-label" htmlFor="gf-code">Code <span className="required">*</span></label>
                <input id="gf-code" className="input" name="code" value={form.code} onChange={updateField} required />
              </div>
              <div className="field">
                <label className="field-label" htmlFor="gf-name">Name <span className="required">*</span></label>
                <input id="gf-name" className="input" name="name" value={form.name} onChange={updateField} required />
              </div>
            </div>
            <div className="field">
              <label className="field-label" htmlFor="gf-parent">Parent Genre</label>
              <select id="gf-parent" className="select" name="parentGenreId" value={form.parentGenreId} onChange={updateField}>
                <option value="">None (top-level)</option>
                {topLevel.filter((g) => !editGenre || g.id !== editGenre.id).map((g) => (
                  <option key={g.id} value={g.id}>{g.name}</option>
                ))}
              </select>
            </div>
            <div className="field">
              <label className="field-label" htmlFor="gf-desc">Description</label>
              <textarea id="gf-desc" className="textarea" name="description" value={form.description} onChange={updateField} />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} type="button">
            {saving ? <Spinner /> : editGenre ? 'Save Changes' : 'Create Genre'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
