import { http } from './http'

export const authorsApi = {
  listAuthors() {
    return http.get('/api/authors')
  },
  listActiveAuthors() {
    return http.get('/api/authors/active')
  },
  getAuthor(id) {
    return http.get(`/api/authors/${id}`)
  },
  createAuthor(payload) {
    return http.post('/api/authors', payload)
  },
  updateAuthor(id, payload) {
    return http.put(`/api/authors/${id}`, payload)
  },
  deleteAuthor(id) {
    return http.delete(`/api/authors/${id}`)
  },
  activateAuthor(id) {
    return http.patch(`/api/authors/${id}/activate`)
  },
  deactivateAuthor(id) {
    return http.patch(`/api/authors/${id}/deactivate`)
  },
}
