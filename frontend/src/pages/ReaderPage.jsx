import { useState, useEffect } from 'react'
import { booksApi } from '../api/booksApi'
import { Spinner } from '../components/common/Spinner'
import { Alert } from '../components/common/Alert'
import { navigate, matchRoute, useRouter } from '../app/router'
import { IconBookOpen, IconArrowLeft } from '../components/common/Icons'

export function ReaderPage() {
  const { path } = useRouter()
  const match = matchRoute('/reader/:bookId', path)
  const bookId = match?.bookId

  const [pdfUrl, setPdfUrl] = useState(null)
  const [bookTitle, setBookTitle] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!bookId) return

    let objectUrl = null

    async function loadPdf() {
      setLoading(true)
      setError('')
      try {
        // Get title for display
        try {
          const book = await booksApi.getBook(bookId)
          setBookTitle(book.title || '')
        } catch { /* ignore */ }

        const blob = await booksApi.fetchReadPdfBlob(bookId)
        if (!(blob instanceof Blob)) {
          setError('Could not load the book. Make sure you have access.')
          return
        }
        objectUrl = URL.createObjectURL(blob)
        setPdfUrl(objectUrl)
      } catch (err) {
        setError(err.message || 'Failed to load book. You may not have access or access may have expired.')
      } finally {
        setLoading(false)
      }
    }

    loadPdf()

    return () => {
      if (objectUrl) URL.revokeObjectURL(objectUrl)
    }
  }, [bookId])

  return (
    <div className="reader-container reader-bg fade-in">
      <div className="reader-toolbar">
        <div className="reader-toolbar-left">
          <button
            type="button"
            className="btn btn-ghost btn-sm"
            style={{ color: '#ccc', display: 'inline-flex', alignItems: 'center' }}
            onClick={() => navigate('/my-library')}
          >
            <IconArrowLeft size={16} style={{ marginRight: 6 }} />
            <span>Back</span>
          </button>
          {bookTitle && (
            <span className="reader-title" title={bookTitle}>
              <IconBookOpen size={16} style={{ marginRight: 6, verticalAlign: 'text-bottom' }} />
              {bookTitle}
            </span>
          )}
        </div>
        <div style={{ fontSize: 12, color: '#aaa', letterSpacing: '0.02em' }}>
          Protected Reader · Access Controlled
        </div>
      </div>

      {loading && (
        <div className={loading ? "loading-overlay show" : "loading-overlay"} style={{ background: '#262626', color: '#eaeaea' }}>
          <Spinner size="lg" />
          <span>Opening reader…</span>
        </div>
      )}

      {error && !loading && (
        <div style={{ padding: 32 }}>
          <Alert type="error">{error}</Alert>
          <button className="btn btn-outline mt-4" onClick={() => navigate('/my-library')}>
            <IconArrowLeft size={16} style={{ marginRight: 6 }} />
            Back to My Library
          </button>
        </div>
      )}

      {pdfUrl && !loading && (
        <iframe
          className="reader-frame"
          src={pdfUrl}
          title={bookTitle || 'E-Book Reader'}
          onContextMenu={(e) => e.preventDefault()}
        />
      )}
    </div>
  )
}
