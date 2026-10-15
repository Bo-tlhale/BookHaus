import { apiRequest } from './client'

export const shelvesApi = {
  list: () => apiRequest('/api/shelves'),
  get: (id) => apiRequest(`/api/shelves/${id}`),
  create: (name) => apiRequest('/api/shelves', { method: 'POST', body: { name } }),
  rename: (id, name) => apiRequest(`/api/shelves/${id}`, { method: 'PATCH', body: { name } }),
  remove: (id) => apiRequest(`/api/shelves/${id}`, { method: 'DELETE' }),
  addBook: (id, libraryBookId) => apiRequest(`/api/shelves/${id}/books`, { method: 'POST', body: { libraryBookId } }),
  removeBook: (id, libraryBookId) => apiRequest(`/api/shelves/${id}/books/${libraryBookId}`, { method: 'DELETE' }),
  reorder: (shelfIds) => apiRequest('/api/shelves/reorder', { method: 'PATCH', body: { shelfIds } }),
  reorderBooks: (id, libraryBookIds) => apiRequest(`/api/shelves/${id}/books/reorder`, { method: 'PATCH', body: { libraryBookIds } }),
}
