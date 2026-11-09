import { http } from './http'

export const authApi = {
  register(payload) {
    return http.post('/api/v1/auth/register', payload)
  },
  login(payload) {
    return http.post('/api/v1/auth/login', payload)
  },
  refresh(refreshToken) {
    return http.post('/api/v1/auth/refresh', { refreshToken })
  },
  logout(refreshToken) {
    return http.post('/api/v1/auth/logout', { refreshToken })
  },
}
