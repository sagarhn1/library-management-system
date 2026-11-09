import { useEffect } from 'react'
import { useAuth } from './useAuth'
import { navigate } from '../app/router'

export function RequireRole({ roles, children }) {
  const { isAuthenticated, user } = useAuth()
  const allowed = isAuthenticated && roles.includes(user?.role)

  useEffect(() => {
    if (isAuthenticated && !allowed) {
      navigate('/catalog')
    }
  }, [allowed, isAuthenticated])

  if (!allowed) {
    return null
  }

  return children
}
