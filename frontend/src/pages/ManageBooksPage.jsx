import { useState, useEffect, useCallback } from 'react'
import { booksApi } from '../api/booksApi'
import { authorsApi } from '../api/authorsApi'
import { genresApi } from '../api/genresApi'
import { publishersApi } from '../api/publishersApi'
import { useAuth } from '../auth/useAuth'
import { Modal } from '../components/common/Modal'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { Pagination } from '../components/common/Pagination'
import { EmptyState } from '../components/common/EmptyState'
import { navigate } from '../app/router'

const PAGE_SIZE = 10

const EMPTY_FORM = {
  isbn: '',
  title: '',
  authorId: '',
  genreId: '',
  publisherId: '',
  publishDate: '',
  language: '',
  description: '',
  accessType: 'FREE',
  price: '',
  accessDurationDays: 30,
}

function BookForm({ form, onChange, authors, genres, publishers }) {
  return (
    <div className="form-grid">
      <div className="form-grid-2">
        <div className="field">
          <label className="field-label" htmlFor="bf-isbn">ISBN <span className="required">*</span></label>
          <input id="bf-isbn" className="input" name="isbn" value={form.isbn} onChange={onChange} required />
        </div>
        <div className="field">
          <label className="field-label" htmlFor="bf-title">Title <span className="required">*</span></label>
          <input id="bf-title" className="input" name="title" value={form.title} onChange={onChange} required />
        </div>
      </div>

      <div className="form-grid-2">
        <div className="field">
          <label className="field-label" htmlFor="bf-author">Author <span className="required">*</span></label>
          <select id="bf-author" className="select" name="authorId" value={form.authorId} onChange={onChange} required>
            <option value="">Select author</option>
            {authors.map((a) => <option key={a.id} value={a.id}>{a.name}</option>)}
          </select>
        </div>
        <div className="field">
          <label className="field-label" htmlFor="bf-genre">Genre <span className="required">*</span></label>
          <select id="bf-genre" className="select" name="genreId" value={form.genreId} onChange={onChange} required>
            <option value="">Select genre</option>
            {genres.map((g) => <option key={g.id} value={g.id}>{g.name}</option>)}
          </select>
        </div>
      </div>

      <div className="form-grid-2">
        <div className="field">
          <label className="field-label" htmlFor="bf-publisher">Publisher <span className="required">*</span></label>
          <select id="bf-publisher" className="select" name="publisherId" value={form.publisherId} onChange={onChange} required>
            <option value="">Select publisher</option>
            {publishers.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
        <div className="field">
          <label className="field-label" htmlFor="bf-lang">Language <span className="required">*</span></label>
          <input id="bf-lang" className="input" name="language" value={form.language} onChange={onChange} required />
        </div>
      </div>

      <div className="form-grid-2">
        <div className="field">
          <label className="field-label" htmlFor="bf-access">Access Type <span className="required">*</span></label>
          <select id="bf-access" className="select" name="accessType" value={form.accessType} onChange={onChange}>
            <option value="FREE">Free</option>
            <option value="PAID">Paid</option>
          </select>
        </div>
        <div className="field">
          <label className="field-label" htmlFor="bf-price">
            Price {form.accessType === 'PAID' ? <span className="required">*</span> : null}
          </label>
          <input
            id="bf-price"
            className="input"
            name="price"
            type="number"
            min="0"
            step="0.01"
            placeholder={form.accessType === 'FREE' ? '0.00 (free)' : 'e.g. 299.00'}
            value={form.price}
            onChange={onChange}
            disabled={form.accessType === 'FREE'}
          />
        </div>
      </div>

      <div className="form-grid-2">
        <div className="field">
          <label className="field-label" htmlFor="bf-days">Access Duration (days) <span className="required">*</span></label>
          <input
            id="bf-days"
            className="input"
            name="accessDurationDays"
            type="number"
            min="1"
            value={form.accessDurationDays}
            onChange={onChange}
          />
        </div>
        <div className="field">
          <label className="field-label" htmlFor="bf-date">Publish Date</label>
          <input id="bf-date" className="input" name="publishDate" type="date" value={form.publishDate} onChange={onChange} />
        </div>
      </div>

      <div className="field">
        <label className="field-label" htmlFor="bf-desc">Description</label>
        <textarea
          id="bf-desc"
          className="textarea"
          name="description"
          maxLength={1000}
          value={form.description}
          onChange={onChange}
          placeholder="Short description of the book"
        />
      </div>
    </div>
  )
}

export function ManageBooksPage() {
  const { hasRole } = useAuth()
  const isAdmin = hasRole('ADMIN')

  const [books, setBooks] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')

  // Metadata
  const [authors, setAuthors] = useState([])
  const [genres, setGenres] = useState([])
  const [publishers, setPublishers] = useState([])

  // Modal state
  const [modal, setModal] = useState(null) // 'create' | 'edit' | 'ebook' | 'cover'
  const [editBook, setEditBook] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [modalError, setModalError] = useState('')
  const [saving, setSaving] = useState(false)

  // File upload state
  const [ebookFile, setEbookFile] = useState(null)
  const [coverFile, setCoverFile] = useState(null)
  const [uploadingId, setUploadingId] = useState(null)

  useEffect(() => {
    authorsApi.listAuthors().then(setAuthors).catch(() => {})
    genresApi.listGenres().then(setGenres).catch(() => {})
    publishersApi.listPublishers().then(setPublishers).catch(() => {})
  }, [])

  const fetchBooks = useCallback(async (p) => {
    setLoading(true)
    setError('')
    try {
      const res = await booksApi.listBooks({ page: p, size: PAGE_SIZE, sort: 'createdAt,desc' })
      setBooks(res.content ?? [])
      setTotalPages(res.totalPages ?? 0)
      setTotalElements(res.totalElements ?? 0)
    } catch (err) {
      setError(err.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { fetchBooks(page) }, [fetchBooks, page])

  function updateField(e) {
    const { name, value } = e.target
    setForm((f) => ({
      ...f,
      [name]: value,
      ...(name === 'accessType' && value === 'FREE' ? { price: '' } : {}),
    }))
  }

  function openCreate() {
    setForm(EMPTY_FORM)
    setEditBook(null)
    setModalError('')
    setModal('create')
  }

  function openEdit(book) {
    setEditBook(book)
    setForm({
      isbn: book.isbn || '',
      title: book.title || '',
      authorId: book.authorId || '',
      genreId: book.genreId || '',
      publisherId: book.publisherId || '',
      publishDate: book.publishDate || '',
      language: book.language || '',
      description: book.description || '',
      accessType: book.accessType || 'FREE',
      price: book.price != null ? book.price : '',
      accessDurationDays: book.accessDurationDays || 30,
    })
    setModalError('')
    setModal('edit')
  }

  function buildPayload(f) {
    return {
      isbn: f.isbn,
      title: f.title,
      authorId: Number(f.authorId),
      genreId: Number(f.genreId),
      publisherId: Number(f.publisherId),
      publishDate: f.publishDate || undefined,
      language: f.language,
      description: f.description || undefined,
      accessType: f.accessType,
      price: f.accessType === 'FREE' ? 0 : (f.price ? Number(f.price) : undefined),
      accessDurationDays: Number(f.accessDurationDays),
    }
  }

  async function handleSave() {
    setSaving(true)
    setModalError('')
    try {
      if (modal === 'create') {
        await booksApi.createBook(buildPayload(form))
        setSuccess('Book created successfully.')
      } else {
        await booksApi.updateBook(editBook.id, buildPayload(form))
        setSuccess('Book updated successfully.')
      }
      setModal(null)
      fetchBooks(page)
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(book) {
    if (!confirm(`Delete "${book.title}"? This cannot be undone.`)) return
    try {
      await booksApi.deleteBook(book.id)
      setSuccess('Book deleted.')
      fetchBooks(page)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleToggleActive(book) {
    try {
      if (book.active) {
        await booksApi.deactivateBook(book.id)
        setSuccess('Book deactivated.')
      } else {
        await booksApi.activateBook(book.id)
        setSuccess('Book activated.')
      }
      fetchBooks(page)
    } catch (err) {
      setError(err.message)
    }
  }

  async function handleEbookUpload() {
    if (!ebookFile || !uploadingId) return
    setSaving(true)
    setModalError('')
    try {
      await booksApi.uploadEbook(uploadingId, ebookFile)
      setSuccess('E-book uploaded.')
      setModal(null)
      setEbookFile(null)
      fetchBooks(page)
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  async function handleCoverUpload() {
    if (!coverFile || !uploadingId) return
    setSaving(true)
    setModalError('')
    try {
      await booksApi.uploadCover(uploadingId, coverFile)
      setSuccess('Cover uploaded.')
      setModal(null)
      setCoverFile(null)
      fetchBooks(page)
    } catch (err) {
      setModalError(err.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <div className="page-content">
      <div className="page-header">
        <div className="page-header-text">
          <h1>Manage Books</h1>
          <p>{totalElements} books total</p>
        </div>
        <div className="page-actions">
          <button className="btn btn-outline btn-sm" onClick={() => navigate('/admin')} type="button">
            ← Admin
          </button>
          <button className="btn btn-primary" onClick={openCreate} type="button">
            + Add Book
          </button>
        </div>
      </div>

      {success && (
        <div className="mb-4">
          <Alert type="success">{success}</Alert>
        </div>
      )}
      {error && (
        <div className="mb-4">
          <Alert type="error">{error}</Alert>
        </div>
      )}

      {loading ? (
        <div className="loading-overlay">
          <Spinner size="lg" />
        </div>
      ) : books.length === 0 ? (
        <EmptyState icon="📚" title="No books yet" message="Create your first book." />
      ) : (
        <>
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Title</th>
                  <th>Author</th>
                  <th>Genre</th>
                  <th>Access</th>
                  <th>Price</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {books.map((book) => (
                  <tr key={book.id}>
                    <td>
                      <div style={{ fontWeight: 500, color: 'var(--text-heading)' }}>{book.title}</div>
                      <div style={{ fontSize: 11, color: 'var(--text-muted)' }}>{book.isbn}</div>
                    </td>
                    <td>{book.authorName}</td>
                    <td>{book.genreName}</td>
                    <td>
                      <span className={`badge ${book.accessType === 'FREE' ? 'badge-free' : 'badge-paid'}`}>
                        {book.accessType}
                      </span>
                    </td>
                    <td>
                      {book.accessType === 'FREE' ? '—' : `₹${Number(book.price || 0).toFixed(2)}`}
                    </td>
                    <td>
                      <span className={`badge ${book.active ? 'badge-active' : 'badge-inactive'}`}>
                        {book.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    <td>
                      <div className="flex gap-2">
                        <button
                          className="btn btn-outline btn-sm"
                          onClick={() => openEdit(book)}
                          type="button"
                        >
                          Edit
                        </button>
                        {isAdmin && (
                          <>
                            <button
                              className="btn btn-outline btn-sm"
                              onClick={() => {
                                setUploadingId(book.id)
                                setEbookFile(null)
                                setModalError('')
                                setModal('ebook')
                              }}
                              type="button"
                            >
                              PDF
                            </button>
                            <button
                              className="btn btn-outline btn-sm"
                              onClick={() => {
                                setUploadingId(book.id)
                                setCoverFile(null)
                                setModalError('')
                                setModal('cover')
                              }}
                              type="button"
                            >
                              Cover
                            </button>
                          </>
                        )}
                        <button
                          className={`btn btn-sm ${book.active ? 'btn-danger-outline' : 'btn-outline'}`}
                          onClick={() => handleToggleActive(book)}
                          type="button"
                        >
                          {book.active ? 'Deactivate' : 'Activate'}
                        </button>
                        {isAdmin && (
                          <button
                            className="btn btn-danger btn-sm"
                            onClick={() => handleDelete(book)}
                            type="button"
                          >
                            Delete
                          </button>
                        )}
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

      {/* Create/Edit Modal */}
      <Modal
        isOpen={modal === 'create' || modal === 'edit'}
        onClose={() => setModal(null)}
        title={modal === 'create' ? 'Add Book' : 'Edit Book'}
        size="lg"
      >
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="mt-3">
            <BookForm form={form} onChange={updateField} authors={authors} genres={genres} publishers={publishers} />
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleSave} disabled={saving} type="button">
            {saving ? <Spinner /> : modal === 'create' ? 'Create Book' : 'Save Changes'}
          </button>
        </div>
      </Modal>

      {/* Upload E-book Modal */}
      <Modal
        isOpen={modal === 'ebook'}
        onClose={() => setModal(null)}
        title="Upload E-book (PDF)"
      >
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="field mt-3">
            <label className="field-label" htmlFor="ebook-file">Select PDF file <span className="required">*</span></label>
            <input
              id="ebook-file"
              className="input"
              type="file"
              accept="application/pdf"
              onChange={(e) => setEbookFile(e.target.files[0])}
            />
          </div>
          <p className="text-sm text-muted mt-2">Uploading a PDF will activate the book.</p>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleEbookUpload} disabled={!ebookFile || saving} type="button">
            {saving ? <Spinner /> : 'Upload PDF'}
          </button>
        </div>
      </Modal>

      {/* Upload Cover Modal */}
      <Modal
        isOpen={modal === 'cover'}
        onClose={() => setModal(null)}
        title="Upload Cover Image"
      >
        <div className="modal-body">
          {modalError && <Alert type="error">{modalError}</Alert>}
          <div className="field mt-3">
            <label className="field-label" htmlFor="cover-file">Select image file <span className="required">*</span></label>
            <input
              id="cover-file"
              className="input"
              type="file"
              accept="image/*"
              onChange={(e) => setCoverFile(e.target.files[0])}
            />
          </div>
        </div>
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={() => setModal(null)} type="button">Cancel</button>
          <button className="btn btn-primary" onClick={handleCoverUpload} disabled={!coverFile || saving} type="button">
            {saving ? <Spinner /> : 'Upload Cover'}
          </button>
        </div>
      </Modal>
    </div>
  )
}
