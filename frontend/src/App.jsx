import { lazy, Suspense } from 'react'
import { Route, Routes } from 'react-router-dom'
import { PageLoader } from './components/LoadingSkeleton'
import AppLayout from './layouts/AppLayout'
import AuthLayout from './layouts/AuthLayout'
import ProtectedRoute from './routes/ProtectedRoute'
import PublicOnlyRoute from './routes/PublicOnlyRoute'

const CustomizePage = lazy(() => import('./pages/CustomizePage'))
const GoalsPage = lazy(() => import('./pages/GoalsPage'))
const HomePage = lazy(() => import('./pages/HomePage'))
const JournalPage = lazy(() => import('./pages/JournalPage'))
const LibraryDetailPage = lazy(() => import('./pages/LibraryDetailPage'))
const LibraryPage = lazy(() => import('./pages/LibraryPage'))
const LoginPage = lazy(() => import('./pages/LoginPage'))
const NotFoundPage = lazy(() => import('./pages/NotFoundPage'))
const RegisterPage = lazy(() => import('./pages/RegisterPage'))
const SearchPage = lazy(() => import('./pages/SearchPage'))
const ShelfDetailPage = lazy(() => import('./pages/ShelfDetailPage'))
const ShelvesPage = lazy(() => import('./pages/ShelvesPage'))
const StatsPage = lazy(() => import('./pages/StatsPage'))

export default function App() {
  return <Suspense fallback={<PageLoader label="Opening BookHaus…" />}><Routes><Route element={<PublicOnlyRoute />}><Route element={<AuthLayout />}><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /></Route></Route><Route element={<ProtectedRoute />}><Route element={<AppLayout />}><Route index element={<HomePage />} /><Route path="/search" element={<SearchPage />} /><Route path="/library" element={<LibraryPage />} /><Route path="/library/:libraryBookId" element={<LibraryDetailPage />} /><Route path="/journal" element={<JournalPage />} /><Route path="/shelves" element={<ShelvesPage />} /><Route path="/shelves/:shelfId" element={<ShelfDetailPage />} /><Route path="/goals" element={<GoalsPage />} /><Route path="/stats" element={<StatsPage />} /><Route path="/customize" element={<CustomizePage />} /><Route path="*" element={<NotFoundPage />} /></Route></Route></Routes></Suspense>
}
