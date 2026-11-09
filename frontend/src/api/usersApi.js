import { http } from './http'

function toQueryString(params = {}) {
  const searchParams = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') {
      searchParams.set(key, value)
    }
  })
  const query = searchParams.toString()
  return query ? `?${query}` : ''
}

export const usersApi = {
  getUsers(params = {}) {
    return http.get(`/api/v1/users${toQueryString(params)}`)
  },
  getUser(id) {
    return http.get(`/api/v1/users/${id}`)
  },
  createUser(payload) {
    return http.post('/api/v1/users', payload)
  },
  updateUser(id, payload) {
    return http.put(`/api/v1/users/${id}`, payload)
  },
  deleteUser(id) {
    return http.delete(`/api/v1/users/${id}`)
  },
}
