export function Pagination({ page, totalPages, onPageChange }) {
  if (totalPages <= 1) return null

  const pages = []
  const delta = 2
  const left = Math.max(0, page - delta)
  const right = Math.min(totalPages - 1, page + delta)

  for (let i = left; i <= right; i++) {
    pages.push(i)
  }

  return (
    <div className="pagination">
      <button
        className="page-btn"
        onClick={() => onPageChange(page - 1)}
        disabled={page === 0}
        aria-label="Previous page"
      >
        ‹
      </button>

      {left > 0 && (
        <>
          <button className="page-btn" onClick={() => onPageChange(0)}>1</button>
          {left > 1 && <span style={{ padding: '0 4px', color: 'var(--text-muted)' }}>…</span>}
        </>
      )}

      {pages.map((p) => (
        <button
          key={p}
          className={`page-btn ${p === page ? 'active' : ''}`}
          onClick={() => onPageChange(p)}
          aria-current={p === page ? 'page' : undefined}
        >
          {p + 1}
        </button>
      ))}

      {right < totalPages - 1 && (
        <>
          {right < totalPages - 2 && <span style={{ padding: '0 4px', color: 'var(--text-muted)' }}>…</span>}
          <button className="page-btn" onClick={() => onPageChange(totalPages - 1)}>
            {totalPages}
          </button>
        </>
      )}

      <button
        className="page-btn"
        onClick={() => onPageChange(page + 1)}
        disabled={page >= totalPages - 1}
        aria-label="Next page"
      >
        ›
      </button>
    </div>
  )
}
