import { http } from './http'

export const publishersApi = {
  listPublishers() {
    return http.get('/api/publishers')
  },
  listActivePublishers() {
    return http.get('/api/publishers/active')
  },
  getPublisher(id) {
    return http.get(`/api/publishers/${id}`)
  },
  createPublisher(payload) {
    return http.post('/api/publishers', payload)
  },
  updatePublisher(id, payload) {
    return http.put(`/api/publishers/${id}`, payload)
  },
  deletePublisher(id) {
    return http.delete(`/api/publishers/${id}`)
  },
  activatePublisher(id) {
    return http.patch(`/api/publishers/${id}/activate`)
  },
  deactivatePublisher(id) {
    return http.patch(`/api/publishers/${id}/deactivate`)
  },
}
