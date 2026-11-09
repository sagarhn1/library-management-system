import { useEffect, useMemo, useState } from 'react'

function getPath() {
  return window.location.pathname || '/'
}

export function navigate(path) {
  if (window.location.pathname === path) {
    return
  }

  window.history.pushState({}, '', path)
  window.dispatchEvent(new PopStateEvent('popstate'))
}

export function useRouter() {
  const [path, setPath] = useState(getPath)

  useEffect(() => {
    const handleNavigation = () => setPath(getPath())
    window.addEventListener('popstate', handleNavigation)
    return () => window.removeEventListener('popstate', handleNavigation)
  }, [])

  return useMemo(() => ({ path, navigate }), [path])
}

export function matchRoute(pattern, path) {
  const patternParts = pattern.split('/').filter(Boolean)
  const pathParts = path.split('/').filter(Boolean)

  if (patternParts.length !== pathParts.length) {
    return null
  }

  const params = {}

  for (let index = 0; index < patternParts.length; index += 1) {
    const patternPart = patternParts[index]
    const pathPart = pathParts[index]

    if (patternPart.startsWith(':')) {
      params[patternPart.slice(1)] = pathPart
    } else if (patternPart !== pathPart) {
      return null
    }
  }

  return params
}
