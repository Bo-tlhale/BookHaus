import { useCallback, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/authApi'
import { getStoredToken, setUnauthorizedHandler, storeToken } from '../api/client'
import { AuthContext } from './auth-context'

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() => getStoredToken())
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(Boolean(getStoredToken()))

  const logout = useCallback(() => {
    storeToken(null)
    setToken(null)
    setUser(null)
    setLoading(false)
  }, [])

  useEffect(() => {
    setUnauthorizedHandler(logout)
    return () => setUnauthorizedHandler(null)
  }, [logout])

  useEffect(() => {
    let active = true
    if (!token) { setLoading(false); return undefined }
    setLoading(true)
    authApi.currentUser()
      .then((currentUser) => { if (active) setUser(currentUser) })
      .catch(() => { if (active) logout() })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [token, logout])

  const acceptAuth = useCallback((response) => {
    storeToken(response.token)
    setToken(response.token)
    setUser(response.user)
    setLoading(false)
    return response.user
  }, [])

  const login = useCallback(async (credentials) => acceptAuth(await authApi.login(credentials)), [acceptAuth])
  const register = useCallback(async (details) => acceptAuth(await authApi.register(details)), [acceptAuth])

  const value = useMemo(() => ({ token, user, loading, isAuthenticated: Boolean(token && user), login, register, logout }), [token, user, loading, login, register, logout])
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
