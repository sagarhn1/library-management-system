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

export const booksApi = {
  listBooks(params) {
    return http.get(`/api/books${toQueryString(params)}`)
  },
  searchBooks(filters, params) {
    return http.post(`/api/books/search${toQueryString(params)}`, filters)
  },
  getBook(bookId) {
    return http.get(`/api/books/${bookId}`)
  },
  getBookByIsbn(isbn) {
    return http.get(`/api/books/isbn/${isbn}`)
  },
  getActiveBooks() {
    return http.get('/api/books/active')
  },
  createBook(payload) {
    return http.post('/api/books', payload)
  },
  updateBook(bookId, payload) {
    return http.put(`/api/books/${bookId}`, payload)
  },
  deleteBook(bookId) {
    return http.delete(`/api/books/${bookId}`)
  },
  activateBook(bookId) {
    return http.patch(`/api/books/${bookId}/activate`)
  },
  deactivateBook(bookId) {
    return http.patch(`/api/books/${bookId}/deactivate`)
  },
  uploadEbook(bookId, file) {
    const form = new FormData()
    form.append('file', file)
    return http.post(`/api/books/${bookId}/ebook`, form)
  },
  uploadCover(bookId, file) {
    const form = new FormData()
    form.append('file', file)
    return http.post(`/api/books/${bookId}/cover`, form)
  },
  fetchCoverBlob(bookId) {
    return http.get(`/api/books/${bookId}/cover`)
  },
  fetchReadPdfBlob(bookId) {
    return http.get(`/api/books/${bookId}/read`)
  },
}
