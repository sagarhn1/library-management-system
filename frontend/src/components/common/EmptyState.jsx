import { IconBookOpen } from './Icons'

export function EmptyState({ icon, title, message, action }) {
  return (
    <div className="empty-state">
      <div className="empty-state-icon">
        {icon ? (
          typeof icon === 'string' ? <span>{icon}</span> : icon
        ) : (
          <IconBookOpen size={36} />
        )}
      </div>
      {title && <h3>{title}</h3>}
      {message && <p>{message}</p>}
      {action}
    </div>
  )
}
