import { http } from './http'

export const genresApi = {
  listGenres() {
    return http.get('/api/genres')
  },
  listActiveGenres() {
    return http.get('/api/genres/active')
  },
  listTopLevelGenres() {
    return http.get('/api/genres/top-level')
  },
  listSubgenres(parentGenreId) {
    return http.get(`/api/genres/${parentGenreId}/subgenres`)
  },
  getGenre(id) {
    return http.get(`/api/genres/${id}`)
  },
  createGenre(payload) {
    return http.post('/api/genres', payload)
  },
  updateGenre(id, payload) {
    return http.put(`/api/genres/${id}`, payload)
  },
  deleteGenre(id) {
    return http.delete(`/api/genres/${id}`)
  },
  activateGenre(id) {
    return http.patch(`/api/genres/${id}/activate`)
  },
  deactivateGenre(id) {
    return http.patch(`/api/genres/${id}/deactivate`)
  },
}
