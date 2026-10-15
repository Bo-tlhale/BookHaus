import { Route, Routes } from 'react-router-dom'
import AppLayout from './layouts/AppLayout'
import AuthLayout from './layouts/AuthLayout'
import GoalsPage from './pages/GoalsPage'
import HomePage from './pages/HomePage'
import LibraryDetailPage from './pages/LibraryDetailPage'
import LibraryPage from './pages/LibraryPage'
import LoginPage from './pages/LoginPage'
import NotFoundPage from './pages/NotFoundPage'
import RegisterPage from './pages/RegisterPage'
import SearchPage from './pages/SearchPage'
import ShelfDetailPage from './pages/ShelfDetailPage'
import ShelvesPage from './pages/ShelvesPage'
import StatsPage from './pages/StatsPage'
import ProtectedRoute from './routes/ProtectedRoute'
import PublicOnlyRoute from './routes/PublicOnlyRoute'

export default function App() {
  return <Routes><Route element={<PublicOnlyRoute />}><Route element={<AuthLayout />}><Route path="/login" element={<LoginPage />} /><Route path="/register" element={<RegisterPage />} /></Route></Route><Route element={<ProtectedRoute />}><Route element={<AppLayout />}><Route index element={<HomePage />} /><Route path="/search" element={<SearchPage />} /><Route path="/library" element={<LibraryPage />} /><Route path="/library/:libraryBookId" element={<LibraryDetailPage />} /><Route path="/shelves" element={<ShelvesPage />} /><Route path="/shelves/:shelfId" element={<ShelfDetailPage />} /><Route path="/goals" element={<GoalsPage />} /><Route path="/stats" element={<StatsPage />} /><Route path="*" element={<NotFoundPage />} /></Route></Route></Routes>
}
