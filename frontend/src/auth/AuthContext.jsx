import { useCallback, useMemo, useState } from 'react'
import { authApi } from '../api/authApi'
import { clearStoredTokens, getStoredTokens, storeTokens } from './tokenStorage'
import { decodeJwt, isJwtExpired } from './jwt'
import { AuthContext } from './context'

function buildSession(accessToken, refreshToken) {
  const claims = decodeJwt(accessToken)

  if (!accessToken || !claims || isJwtExpired(accessToken)) {
    return {
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
    }
  }

  return {
    accessToken,
    refreshToken,
    user: {
      id: claims.userId,
      email: claims.sub,
      role: claims.role,
    },
    isAuthenticated: true,
  }
}

export function AuthProvider({ children }) {
  const storedTokens = getStoredTokens()
  const [session, setSession] = useState(() =>
    buildSession(storedTokens.accessToken, storedTokens.refreshToken),
  )

  const login = useCallback(async (payload) => {
    const tokens = await authApi.login(payload)
    storeTokens(tokens)
    setSession(buildSession(tokens.accessToken, tokens.refreshToken))
    return tokens
  }, [])

  const register = useCallback(async (payload) => {
    const tokens = await authApi.register(payload)
    storeTokens(tokens)
    setSession(buildSession(tokens.accessToken, tokens.refreshToken))
    return tokens
  }, [])

  const logout = useCallback(async () => {
    const refreshToken = session.refreshToken
    clearStoredTokens()
    setSession(buildSession(null, null))

    if (refreshToken) {
      try {
        await authApi.logout(refreshToken)
      } catch {
        // Local logout should complete even if the token is already invalid.
      }
    }
  }, [session.refreshToken])

  const value = useMemo(
    () => ({
      ...session,
      login,
      register,
      logout,
      hasRole(...roles) {
        return Boolean(session.user?.role && roles.includes(session.user.role))
      },
    }),
    [login, logout, register, session],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
