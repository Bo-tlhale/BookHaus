import { apiRequest } from './client'

export const goalsApi = {
  list: () => apiRequest('/api/goals'),
  get: (year) => apiRequest(`/api/goals/${year}`),
  create: (goal) => apiRequest('/api/goals', { method: 'POST', body: goal }),
  update: (year, targetBooks) => apiRequest(`/api/goals/${year}`, { method: 'PATCH', body: { targetBooks } }),
  remove: (year) => apiRequest(`/api/goals/${year}`, { method: 'DELETE' }),
}
