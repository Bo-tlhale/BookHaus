import { render, screen } from '@testing-library/react'
import { MemoryRouter, Route, Routes } from 'react-router-dom'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import ProtectedRoute from './ProtectedRoute'

const authState = vi.hoisted(() => ({ value: { isAuthenticated: false, loading: false } }))
vi.mock('../context/useAuth', () => ({ useAuth: () => authState.value }))

function renderRoute(path = '/library') {
  return render(<MemoryRouter initialEntries={[path]}><Routes><Route path="/login" element={<p>Login page</p>} /><Route element={<ProtectedRoute />}><Route path="/library" element={<p>Private library</p>} /></Route></Routes></MemoryRouter>)
}

describe('ProtectedRoute', () => {
  beforeEach(() => { authState.value = { isAuthenticated: false, loading: false } })

  it('redirects a signed-out reader to login', () => {
    renderRoute()
    expect(screen.getByText('Login page')).toBeInTheDocument()
  })

  it('renders private content for an authenticated reader', () => {
    authState.value = { isAuthenticated: true, loading: false }
    renderRoute()
    expect(screen.getByText('Private library')).toBeInTheDocument()
  })
})
