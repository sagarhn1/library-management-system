import { useState, useEffect } from 'react'
import { booksApi } from '../api/booksApi'
import { accessApi } from '../api/accessApi'
import { paymentsApi } from '../api/paymentsApi'
import { CoverImage } from '../components/books/CoverImage'
import { Alert } from '../components/common/Alert'
import { Spinner } from '../components/common/Spinner'
import { navigate, matchRoute, useRouter } from '../app/router'
import {
  IconBookOpen,
  IconSparkles,
  IconCreditCard,
  IconRotateCw,
  IconArrowLeft,
} from '../components/common/Icons'

function loadRazorpay() {
  return new Promise((resolve) => {
    if (window.Razorpay) { resolve(true); return }
    const script = document.createElement('script')
    script.src = 'https://checkout.razorpay.com/v1/checkout.js'
    script.onload = () => resolve(true)
    script.onerror = () => resolve(false)
    document.body.appendChild(script)
  })
}

const ACTION_LABELS = {
  READ: { label: 'Read Now', icon: IconBookOpen, cls: 'btn-primary' },
  GET_FREE: { label: 'Get Free Access', icon: IconSparkles, cls: 'btn-success' },
  BUY: { label: 'Purchase Access', icon: IconCreditCard, cls: 'btn-primary', style: { background: '#6366f1', borderColor: '#6366f1' } },
  EXPIRED: { label: 'Renew Access', icon: IconRotateCw, cls: 'btn-outline' },
}

export function BookDetailPage() {
  const { path } = useRouter()
  const match = matchRoute('/books/:bookId', path)
  const bookId = match?.bookId

  const [book, setBook] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [acting, setActing] = useState(false)
  const [actionMsg, setActionMsg] = useState('')
  const [actionError, setActionError] = useState('')

  useEffect(() => {
    if (!bookId) return
    setLoading(true)
    booksApi.getBook(bookId)
      .then(setBook)
      .catch((err) => setError(err.message))
      .finally(() => setLoading(false))
  }, [bookId])

  async function handleAction() {
    if (!book?.userBookAction) return
    setActing(true)
    setActionError('')
    setActionMsg('')

    try {
      if (book.userBookAction === 'READ') {
        navigate(`/reader/${bookId}`)
        return
      }

      if (book.userBookAction === 'GET_FREE' || (book.userBookAction === 'EXPIRED' && book.accessType === 'FREE')) {
        await accessApi.requestFreeBookAccess(bookId)
        const updated = await booksApi.getBook(bookId)
        setBook(updated)
        setActionMsg('Access granted! You can now read this book.')
        return
      }

      if (book.userBookAction === 'BUY' || (book.userBookAction === 'EXPIRED' && book.accessType === 'PAID')) {
        await handleRazorpayPayment()
        return
      }
    } catch (err) {
      setActionError(err.message)
    } finally {
      setActing(false)
    }
  }

  async function handleRazorpayPayment() {
    const loaded = await loadRazorpay()
    if (!loaded) {
      setActionError('Payment gateway could not load. Check your connection.')
      setActing(false)
      return
    }

    let paymentData
    try {
      paymentData = await paymentsApi.createPayment(Number(bookId))
    } catch (err) {
      setActionError(err.message)
      setActing(false)
      return
    }

    const rzp = new window.Razorpay({
      key: paymentData.razorpayKeyId,
      amount: paymentData.amount * 100,
      currency: paymentData.currency || 'INR',
      order_id: paymentData.razorpayOrderId,
      name: 'E-Library',
      description: paymentData.bookTitle,
      handler: async function (response) {
        setActing(true)
        try {
          await paymentsApi.verifyPayment({
            razorpayPaymentId: response.razorpay_payment_id,
            razorpayOrderId: response.razorpay_order_id,
            razorpaySignature: response.razorpay_signature,
          })
          const updated = await booksApi.getBook(bookId)
          setBook(updated)
          setActionMsg('Payment successful! You can now read this book.')
        } catch (err) {
          setActionError(err.message)
        } finally {
          setActing(false)
        }
      },
      modal: {
        ondismiss: async function () {
          try {
            await paymentsApi.markPaymentFailed(paymentData.razorpayOrderId)
          } catch { /* ignore */ }
          setActing(false)
        },
      },
    })
    rzp.open()
  }

  if (loading) {
    return (
      <div className="page-content fade-in">
        <div className="loading-overlay show">
          <Spinner size="lg" />
          <span>Loading book details…</span>
        </div>
      </div>
    )
  }

  if (error || !book) {
    return (
      <div className="page-content fade-in">
        <Alert type="error">{error || 'Book not found.'}</Alert>
        <button className="btn btn-outline mt-4" onClick={() => navigate('/catalog')}>
          <IconArrowLeft size={16} style={{ marginRight: 6 }} />
          Back to Catalog
        </button>
      </div>
    )
  }

  const actionCfg = book.userBookAction ? ACTION_LABELS[book.userBookAction] : null
  const ActionIcon = actionCfg?.icon

  return (
    <div className="page-content fade-in">
      <button
        className="btn btn-ghost btn-sm mb-4"
        onClick={() => navigate('/catalog')}
        type="button"
      >
        <IconArrowLeft size={16} style={{ marginRight: 6 }} />
        Back to Catalog
      </button>

      <div className="book-detail-layout">
        {/* Cover */}
        <div>
          <div className="book-detail-cover">
            <CoverImage bookId={book.id} title={book.title} />
          </div>

          {/* Action Area */}
          <div className="book-action-area mt-4">
            {book.accessType === 'FREE' ? (
              <div className="book-price free">Free</div>
            ) : (
              <div>
                <div className="book-price">₹{Number(book.price || 0).toFixed(2)}</div>
                <div className="text-sm text-muted">{book.accessDurationDays} days access</div>
              </div>
            )}

            {actionMsg && <Alert type="success">{actionMsg}</Alert>}
            {actionError && <Alert type="error">{actionError}</Alert>}

            {actionCfg && (
              <button
                type="button"
                className={`btn ${actionCfg.cls} btn-block`}
                style={actionCfg.style}
                onClick={handleAction}
                disabled={acting}
              >
                {acting ? (
                  <Spinner />
                ) : (
                  <>
                    {ActionIcon && <ActionIcon size={16} style={{ marginRight: 6 }} />}
                    <span>{actionCfg.label}</span>
                  </>
                )}
              </button>
            )}

            {!book.active && (
              <Alert type="warning">This book is currently not available.</Alert>
            )}
          </div>
        </div>

        {/* Info */}
        <div className="book-detail-info">
          <div>
            <div className="flex gap-2 mb-2" style={{ flexWrap: 'wrap' }}>
              <span className={`badge ${book.accessType === 'FREE' ? 'badge-free' : 'badge-paid'}`}>
                {book.accessType === 'FREE' ? 'Free Access' : 'Paid'}
              </span>
              <span className={`badge ${book.active ? 'badge-active' : 'badge-inactive'}`}>
                {book.active ? 'Available' : 'Unavailable'}
              </span>
            </div>
            <h1 className="book-detail-title">{book.title}</h1>
          </div>

          <div className="book-detail-meta">
            {book.authorName && (
              <span className="book-detail-meta-item">
                <span className="meta-label">Author:</span> {book.authorName}
              </span>
            )}
            {book.genreName && (
              <span className="book-detail-meta-item">
                <span className="meta-label">Genre:</span> {book.genreName}
              </span>
            )}
            {book.publisherName && (
              <span className="book-detail-meta-item">
                <span className="meta-label">Publisher:</span> {book.publisherName}
              </span>
            )}
            {book.publishDate && (
              <span className="book-detail-meta-item">
                <span className="meta-label">Published:</span> {book.publishDate}
              </span>
            )}
            {book.language && (
              <span className="book-detail-meta-item">
                <span className="meta-label">Language:</span> {book.language}
              </span>
            )}
            {book.isbn && (
              <span className="book-detail-meta-item">
                <span className="meta-label">ISBN:</span> {book.isbn}
              </span>
            )}
          </div>

          {book.description && (
            <div>
              <h3 style={{ marginBottom: 8 }}>About this book</h3>
              <p className="book-description">{book.description}</p>
            </div>
          )}

          <div className="card" style={{ maxWidth: 400 }}>
            <div className="card-body">
              <h4 style={{ marginBottom: 12, fontWeight: 600 }}>Book Specifications</h4>
              <table style={{ width: '100%', fontSize: 13 }}>
                <tbody>
                  <tr>
                    <td style={{ color: 'var(--text-muted)', paddingRight: 16, paddingBottom: 6 }}>ISBN</td>
                    <td style={{ fontWeight: 500 }}>{book.isbn || '—'}</td>
                  </tr>
                  <tr>
                    <td style={{ color: 'var(--text-muted)', paddingRight: 16, paddingBottom: 6 }}>Language</td>
                    <td>{book.language || '—'}</td>
                  </tr>
                  <tr>
                    <td style={{ color: 'var(--text-muted)', paddingRight: 16, paddingBottom: 6 }}>Access Duration</td>
                    <td>{book.accessDurationDays} days</td>
                  </tr>
                  <tr>
                    <td style={{ color: 'var(--text-muted)', paddingRight: 16 }}>Published Date</td>
                    <td>{book.publishDate || '—'}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
