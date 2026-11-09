import { useEffect, useRef } from 'react'

export function Modal({ isOpen, onClose, title, children, size = '' }) {
  const overlayRef = useRef(null)

  useEffect(() => {
    if (!isOpen) return
    const handleKey = (e) => {
      if (e.key === 'Escape') onClose()
    }
    document.addEventListener('keydown', handleKey)
    document.body.style.overflow = 'hidden'
    return () => {
      document.removeEventListener('keydown', handleKey)
      document.body.style.overflow = ''
    }
  }, [isOpen, onClose])

  if (!isOpen) return null

  function handleOverlayClick(e) {
    if (e.target === overlayRef.current) onClose()
  }

  return (
    <div
      className="modal-overlay"
      ref={overlayRef}
      onClick={handleOverlayClick}
      role="dialog"
      aria-modal="true"
      aria-label={title}
    >
      <div className={`modal ${size === 'lg' ? 'modal-lg' : ''}`}>
        <div className="modal-header">
          <h2>{title}</h2>
          <button
            className="btn btn-ghost btn-sm"
            onClick={onClose}
            aria-label="Close"
            type="button"
          >
            ✕
          </button>
        </div>
        {children}
      </div>
    </div>
  )
}
