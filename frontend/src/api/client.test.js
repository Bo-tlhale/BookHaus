import { describe, expect, it, vi } from 'vitest'
import { ApiError, apiRequest } from './client'

describe('apiRequest', () => {
  it('turns backend problem details into a consistent ApiError', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(new Response(JSON.stringify({ detail: 'Current page cannot exceed page count' }), { status: 400, headers: { 'Content-Type': 'application/json' } })))
    let caught
    try { await apiRequest('/api/example') } catch (error) { caught = error }
    expect(caught).toBeInstanceOf(ApiError)
    expect(caught).toEqual(expect.objectContaining({ name: 'ApiError', status: 400, message: 'Current page cannot exceed page count' }))
  })

  it('turns network failures into a useful BookHaus message', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(apiRequest('/api/example')).rejects.toEqual(expect.objectContaining({
      name: 'ApiError',
      status: 0,
      message: 'BookHaus could not reach the server. Check your connection and try again.',
    }))
  })
})
