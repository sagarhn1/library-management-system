import { navigate } from '../../app/router'

export function LinkButton({ to, children, className = '', variant = 'ghost' }) {
  return (
    <button
      type="button"
      className={`btn btn-${variant} ${className}`}
      onClick={() => navigate(to)}
    >
      {children}
    </button>
  )
}
