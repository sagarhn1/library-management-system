import { useState, useEffect, useCallback } from 'react'
import { publishersApi } from '../api/publishersApi'
import { useAuth } from '../auth/useAuth'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { EmptyState } from '../components/common/EmptyState'
import { navigate } from '../app/router'

const EMPTY_FORM = { name: '', address: '', email: '', phone: '', website: '' }

export function ManagePublishersPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')

  const [publishers, setPublishers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [modal, setModal] = useState(null)
  const [editPub, setEditPub] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [modalError, setModalError] = useState('')
  const [saving, setSaving] = useState(false)

  const fetchPublishers = useCallback(async () => {
    setLoading(true)
    try {
      setPublishers(await publishersApi.listPublishers())
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchPublishers() }, [fetchPublishers])

  function updateField(e) {
    setForm((f) => ({ ...f, [e.target.name]: e.target.value }))
  }

  function openCreate() {
    setForm(EMPTY_FORM)
    setEditPub(null)
    setModalError('')
    setModal('form')
  }

  function openEdit(pub) {
    setEditPub(pub)
    setForm({
      name: pub.name || '',
      address: pub.address || '',
      email: pub.email || '',
      phone: pub.phone || '',
      website: pub.website || '',
    })
    setModalError('')
    setModal('form')
  }

  async function handleSave() {
    setSaving(true)
    setModalError('')
    try {
      if (editPub) {
        await publishersApi.updatePublisher(editPub.id, form)
        setSuccess('Publisher updated.')
      } else {
        await publishersApi.createPublisher(form)
        setSuccess('Publisher created.')
      }
      setModal(null)
      fetchPublishers()
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(pub) {
    if (!confirm(`Delete publisher "${pub.name}"?`)) return
    try {
      await publishersApi.deletePublisher(pub.id)
      setSuccess('Publisher deleted.')
      fetchPublishers()
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggle(pub) {
    try {
      if (pub.active !== false) {
        await publishersApi.deactivatePublisher(pub.id)
      } else {
        await publishersApi.activatePublisher(pub.id)
      }
      setSuccess(`Publisher ${pub.active !== false ? 'deactivated' : 'activated'}.`)
      fetchPublishers()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Manage Publishers</h1>
          <p>{publishers.length} publishers total</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-outline btn-sm" onClick={() => navigate('/admin')} type="button">← Admin</button>
          <button className="btn btn-primary" onClick={openCreate} type="button">+ Add Publisher</button>
        </div>
      </div>

      {success && <div className="mb-4"><Alert type="success">{success}</Alert></div>}
      {error && <div className="mb-4"><Alert type="error">{error}</Alert></div>}

      {loading ? (
        <div className="loading-overlay"><Spinner size="lg" /></div>
      ) : publishers.length === 0 ? (
        <EmptyState icon="🏢" title="No publishers yet" />
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Phone</th>
                <th>Website</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {publishers.map((p) => (
                <tr key={p.id}>
                  <td style={{ fontWeight: 500, color: 'var(--text-heading)' }}>{p.name}</td>
                  <td>{p.email || '—'}</td>
                  <td>{p.phone || '—'}</td>
                  <td>
                    {p.website
                      ? <a href={p.website} target="_blank" rel="noreferrer" style={{ fontSize: 12 }}>🔗 Link</a>
                      : '—'}
                  </td>
                  <td>
                    <span className={`badge ${p.active !== false ? 'badge-active' : 'badge-inactive'}`}>
                      {p.active !== false ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <div className="flex gap-2">
                      <button className="btn btn-outline btn-sm" onClick={() => openEdit(p)} type="button">Edit</button>
                      <button
                        className={`btn btn-sm ${p.active !== false ? 'btn-danger-outline' : 'btn-outline'}`}
                        onClick={() => handleToggle(p)}
                        type="button"
                      >
                        {p.active !== false ? 'Deactivate' : 'Activate'}
                      </button>
                      {isAdmin && (
                        <button className="btn btn-danger btn-sm" onClick={() => handleDelete(p)} type="button">Delete</button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal isOpen={modal === 'form'} onClose={() => setModal(null)} title={editPub ? 'Edit Publisher' : 'Add Publisher'}>
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="form-grid mt-3">
            <div className="field">
              <label className="field-label" htmlFor="pf-name">Name <span className="required">*</span></label>
              <input id="pf-name" className="input" name="name" value={form.name} onChange={updateField} required />
            </div>
            <div className="field">
              <label className="field-label" htmlFor="pf-email">Email</label>
              <input id="pf-email" className="input" name="email" type="email" value={form.email} onChange={updateField} />
            </div>
            <div className="form-grid-2">
              <div className="field">
                <label className="field-label" htmlFor="pf-phone">Phone</label>
                <input id="pf-phone" className="input" name="phone" value={form.phone} onChange={updateField} />
              </div>
              <div className="field">
                <label className="field-label" htmlFor="pf-website">Website</label>
                <input id="pf-website" className="input" name="website" type="url" placeholder="https://" value={form.website} onChange={updateField} />
              </div>
            </div>
            <div className="field">
              <label className="field-label" htmlFor="pf-addr">Address</label>
              <textarea id="pf-addr" className="textarea" name="address" value={form.address} onChange={updateField} rows={2} />
            </div>
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} type="button">
            {saving ? <Spinner /> : editPub ? 'Save Changes' : 'Create Publisher'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
