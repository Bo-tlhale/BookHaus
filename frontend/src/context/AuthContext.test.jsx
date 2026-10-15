import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it, vi } from 'vitest'
import { authApi } from '../api/authApi'
import { AuthProvider } from './AuthContext'
import { useAuth } from './useAuth'

vi.mock('../api/authApi', () => ({ authApi: { currentUser: vi.fn(), login: vi.fn(), register: vi.fn() } }))

function AuthProbe() {
  const { loading, user, isAuthenticated, login } = useAuth()
  return <><p>{loading ? 'loading' : `${isAuthenticated}:${user?.name || 'none'}`}</p><button onClick={() => login({ email: 'ada@example.com', password: 'password' })}>Log in</button></>
}

describe('AuthProvider', () => {
  it('restores a session by asking the backend for the current user', async () => {
    sessionStorage.setItem('bookhaus_token', 'saved-token')
    authApi.currentUser.mockResolvedValue({ id: 1, name: 'Ada Reader', email: 'ada@example.com' })
    render(<AuthProvider><AuthProbe /></AuthProvider>)
    expect(screen.getByText('loading')).toBeInTheDocument()
    await waitFor(() => expect(screen.getByText('true:Ada Reader')).toBeInTheDocument())
    expect(authApi.currentUser).toHaveBeenCalledOnce()
  })

  it('starts signed out when no session token exists', () => {
    render(<AuthProvider><AuthProbe /></AuthProvider>)
    expect(screen.getByText('false:none')).toBeInTheDocument()
  })

  it('persists the token returned by login', async () => {
    authApi.login.mockResolvedValue({ token: 'fresh-token', user: { id: 1, name: 'Ada Reader', email: 'ada@example.com' } })
    authApi.currentUser.mockResolvedValue({ id: 1, name: 'Ada Reader', email: 'ada@example.com' })
    render(<AuthProvider><AuthProbe /></AuthProvider>)
    await userEvent.click(screen.getByRole('button', { name: 'Log in' }))
    await waitFor(() => expect(screen.getByText('true:Ada Reader')).toBeInTheDocument())
    expect(sessionStorage.getItem('bookhaus_token')).toBe('fresh-token')
  })
})
