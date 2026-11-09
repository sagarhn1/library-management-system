import { http } from './http'

export const accessApi = {
  requestFreeBookAccess(bookId) {
    return http.post(`/api/book-access/books/${bookId}/free-access`, {})
  },
  getBookAccess(bookId) {
    return http.get(`/api/book-access/books/${bookId}`)
  },
  getMyBooks() {
    return http.get('/api/book-access/my-books')
  },
  getMyLibrary() {
    return http.get('/api/book-access/my-library')
  },
}
