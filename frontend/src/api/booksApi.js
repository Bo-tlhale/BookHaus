import { apiRequest } from './client'

export const booksApi = {
  search: (query) => apiRequest(`/api/books/search?q=${encodeURIComponent(query)}`),
  details: (bookId) => apiRequest(`/api/books/${bookId}`),
}
