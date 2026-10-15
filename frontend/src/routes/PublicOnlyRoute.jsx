import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'
import { PageLoader } from '../components/LoadingSkeleton'

export default function PublicOnlyRoute() {
  const { isAuthenticated, loading } = useAuth()
  if (loading) return <PageLoader label="Restoring your session…" />
  return isAuthenticated ? <Navigate to="/" replace /> : <Outlet />
}
