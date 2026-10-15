import { apiRequest } from './client'

export const statsApi = {
  overview: () => apiRequest('/api/stats'),
  yearly: (year) => apiRequest(`/api/stats/year/${year}`),
}
