import { useEffect } from 'react'
import { useAuth } from './useAuth'
import { navigate } from '../app/router'

export function RequireAuth({ children }) {
  const { isAuthenticated } = useAuth()

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/login')
    }
  }, [isAuthenticated])

  if (!isAuthenticated) {
    return null
  }

  return children
}
