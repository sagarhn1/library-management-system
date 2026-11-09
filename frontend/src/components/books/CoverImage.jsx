import { useState, useEffect, useCallback } from 'react'
import { booksApi } from '../../api/booksApi'
import { IconBookOpen } from '../common/Icons'

/**
 * Renders an authenticated book cover image.
 * Fetches the cover blob using JWT and creates an object URL.
 */
export function CoverImage({ bookId, title, className = '' }) {
  const [src, setSrc] = useState(null)
  const [error, setError] = useState(false)

  const fetchCover = useCallback(async () => {
    if (!bookId) return
    try {
      const blob = await booksApi.fetchCoverBlob(bookId)
      if (blob instanceof Blob) {
        setSrc(URL.createObjectURL(blob))
      }
    } catch {
      setError(true)
    }
  }, [bookId])

  useEffect(() => {
    fetchCover()
    return () => {
      if (src) URL.revokeObjectURL(src)
    }
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [fetchCover])

  if (error || !src) {
    return (
      <div className={`book-cover-placeholder ${className}`}>
        <IconBookOpen size={30} style={{ opacity: 0.5, marginBottom: 6 }} />
        <span>{title ? title.slice(0, 20) : 'No cover'}</span>
      </div>
    )
  }

  return (
    <img
      src={src}
      alt={`Cover of ${title}`}
      className={className}
      style={{ width: '100%', height: '100%', objectFit: 'cover', display: 'block' }}
    />
  )
}
