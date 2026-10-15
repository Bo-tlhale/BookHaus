import { apiRequest } from './client'

export const authApi = {
  login: (credentials) => apiRequest('/api/auth/login', { method: 'POST', body: credentials, auth: false }),
  register: (details) => apiRequest('/api/auth/register', { method: 'POST', body: details, auth: false }),
  currentUser: () => apiRequest('/api/users/me'),
}
