import { http } from './http'

export const paymentsApi = {
  createPayment(bookId) {
    return http.post('/api/payments/create', { bookId })
  },
  verifyPayment(payload) {
    return http.post('/api/payments/verify', payload)
  },
  markPaymentFailed(razorpayOrderId) {
    return http.post('/api/payments/failed', { razorpayOrderId })
  },
}
