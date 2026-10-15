import { apiRequest } from './client'

export const libraryApi = {
  list: (status) => apiRequest(`/api/library/books${status ? `?status=${encodeURIComponent(status)}` : ''}`),
  get: (libraryBookId) => apiRequest(`/api/library/books/${libraryBookId}`),
  add: (book) => apiRequest('/api/library/books', { method: 'POST', body: book }),
  updateStatus: (id, status) => apiRequest(`/api/library/books/${id}/status`, { method: 'PATCH', body: { status } }),
  updateProgress: (id, currentPage) => apiRequest(`/api/library/books/${id}/progress`, { method: 'PATCH', body: { currentPage } }),
  updateRating: (id, personalRating) => apiRequest(`/api/library/books/${id}/rating`, { method: 'PATCH', body: { personalRating } }),
  remove: (id) => apiRequest(`/api/library/books/${id}`, { method: 'DELETE' }),
}
