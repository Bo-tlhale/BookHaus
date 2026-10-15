import { ApiError, apiRequest } from './client'

export const journalApi = {
  async get(libraryBookId) {
    try { return await apiRequest(`/api/library/books/${libraryBookId}/journal`) }
    catch (error) { if (error instanceof ApiError && error.status === 404) return null; throw error }
  },
  save: (id, journal) => apiRequest(`/api/library/books/${id}/journal`, { method: 'PUT', body: journal }),
  quotes: (id) => apiRequest(`/api/library/books/${id}/quotes`),
  addQuote: (id, quote) => apiRequest(`/api/library/books/${id}/quotes`, { method: 'POST', body: quote }),
  updateQuote: (id, quoteId, quote) => apiRequest(`/api/library/books/${id}/quotes/${quoteId}`, { method: 'PATCH', body: quote }),
  deleteQuote: (id, quoteId) => apiRequest(`/api/library/books/${id}/quotes/${quoteId}`, { method: 'DELETE' }),
}
