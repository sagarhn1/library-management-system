import { useState } from 'react'
import { CoverImage } from './CoverImage'
import { navigate } from '../../app/router'
import { Spinner } from '../common/Spinner'
import { IconBookOpen, IconSparkles, IconCreditCard, IconRotateCw } from '../common/Icons'

const ACTION_CONFIG = {
  READ: {
    label: 'Read',
    icon: IconBookOpen,
    cls: 'btn-primary',
  },
  GET_FREE: {
    label: 'Get Free',
    icon: IconSparkles,
    cls: 'btn-success',
  },
  BUY: {
    label: 'Purchase',
    icon: IconCreditCard,
    cls: 'btn-primary',
    style: { background: '#6366f1', borderColor: '#6366f1' },
  },
  EXPIRED: {
    label: 'Renew',
    icon: IconRotateCw,
    cls: 'btn-outline',
  },
}

export function BookCard({ book, onAction }) {
  const [acting, setActing] = useState(false)

  const actionCfg = book.userBookAction ? ACTION_CONFIG[book.userBookAction] : null

  async function handleAction(e) {
    e.stopPropagation()
    if (!onAction || !book.userBookAction) return
    setActing(true)
    try {
      await onAction(book)
    } finally {
      setActing(false)
    }
  }

  const ActionIcon = actionCfg?.icon

  return (
    <article
      className="book-card"
      onClick={() => navigate(`/books/${book.id}`)}
      role="button"
      tabIndex={0}
      aria-label={`${book.title} by ${book.authorName}`}
      onKeyDown={(e) => e.key === 'Enter' && navigate(`/books/${book.id}`)}
    >
      <div className="book-cover">
        <CoverImage bookId={book.id} title={book.title} />
      </div>

      <div className="book-card-body">
        <div className="book-title" title={book.title}>{book.title}</div>
        <div className="book-meta">{book.authorName}</div>
        <div className="book-meta" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <span>{book.genreName}</span>
          <span className={`badge ${book.accessType === 'FREE' ? 'badge-free' : 'badge-paid'}`}>
            {book.accessType === 'FREE' ? 'Free' : `₹${Number(book.price || 0).toFixed(0)}`}
          </span>
        </div>
      </div>

      {actionCfg && (
        <div className="book-footer">
          <span className="text-sm text-muted">
            {book.accessDurationDays}d access
          </span>
          <button
            type="button"
            className={`btn btn-sm ${actionCfg.cls}`}
            style={actionCfg.style}
            onClick={handleAction}
            disabled={acting}
          >
            {acting ? (
              <Spinner />
            ) : (
              <>
                {ActionIcon && <ActionIcon size={14} style={{ marginRight: 5 }} />}
                <span>{actionCfg.label}</span>
              </>
            )}
          </button>
        </div>
      )}
    </article>
  )
}
