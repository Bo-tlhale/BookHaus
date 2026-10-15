const TOKEN_KEY = 'bookhaus_token'
const API_BASE_URL = (import.meta.env.VITE_API_BASE_URL || '').replace(/\/$/, '')

let unauthorizedHandler = null

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message)
    this.name = 'ApiError'
    this.status = status
    this.details = details
  }
}

export function getStoredToken() {
  return sessionStorage.getItem(TOKEN_KEY)
}

export function storeToken(token) {
  if (token) sessionStorage.setItem(TOKEN_KEY, token)
  else sessionStorage.removeItem(TOKEN_KEY)
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

function errorMessage(body, status) {
  if (typeof body === 'string' && body.trim()) return body
  if (body?.detail) return body.detail
  if (body?.message) return body.message
  if (body?.errors && typeof body.errors === 'object') return Object.values(body.errors).join(' ')
  if (status === 401) return 'Your session has expired. Please sign in again.'
  if (status === 404) return 'The requested item could not be found.'
  if (status === 409) return 'That item already exists.'
  return 'Something went wrong while talking to BookHaus.'
}

export async function apiRequest(path, options = {}) {
  const { body, headers = {}, auth = true, ...rest } = options
  const token = getStoredToken()
  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...rest,
    headers: {
      Accept: 'application/json',
      ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
      ...(auth && token ? { Authorization: `Bearer ${token}` } : {}),
      ...headers,
    },
    ...(body !== undefined ? { body: JSON.stringify(body) } : {}),
  })

  if (response.status === 204) return null
  const contentType = response.headers.get('content-type') || ''
  const payload = contentType.includes('application/json') ? await response.json() : await response.text()

  if (!response.ok) {
    if (response.status === 401 && auth && unauthorizedHandler) unauthorizedHandler()
    throw new ApiError(errorMessage(payload, response.status), response.status, payload)
  }
  return payload
}
