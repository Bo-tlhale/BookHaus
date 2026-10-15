import { useState } from 'react'
import { BarChart3, BookHeart, BookMarked, BookOpen, Home, Library, LogOut, Menu, Search, Target, X } from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'
import { useAuth } from '../context/useAuth'

const navItems = [
  { to: '/', label: 'Home', icon: Home, end: true },
  { to: '/search', label: 'Find Books', icon: Search },
  { to: '/library', label: 'My Library', icon: Library },
  { to: '/shelves', label: 'Shelves', icon: BookMarked },
  { to: '/goals', label: 'Goals', icon: Target },
  { to: '/stats', label: 'Insights', icon: BarChart3 },
]

function Navigation({ onNavigate }) {
  const { user, logout } = useAuth()
  return <div className="flex h-full flex-col"><div className="flex items-center gap-3 px-2 py-3"><span className="rounded-2xl bg-lavender-600 p-2.5 text-white shadow-sm"><BookHeart className="h-6 w-6" /></span><div><p className="font-display text-xl font-semibold text-wood-900">BookHaus</p><p className="text-[11px] text-stone-500">Your reading home</p></div></div><nav className="mt-7 flex-1 space-y-1.5" aria-label="Main navigation">{navItems.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => `flex items-center gap-3 rounded-xl px-3.5 py-3 text-sm font-semibold transition ${isActive ? 'bg-lavender-100 text-lavender-700' : 'text-stone-600 hover:bg-cream-100 hover:text-wood-900'}`}><Icon className="h-5 w-5" />{label}</NavLink>)}</nav><div className="mt-5 border-t border-cream-200 pt-5"><div className="mb-3 flex items-center gap-3 px-2"><div className="flex h-9 w-9 items-center justify-center rounded-full bg-skybook-100 font-display font-semibold text-skybook-600">{user?.name?.charAt(0)?.toUpperCase()}</div><div className="min-w-0"><p className="truncate text-sm font-semibold text-wood-900">{user?.name}</p><p className="truncate text-xs text-stone-500">{user?.email}</p></div></div><button type="button" onClick={logout} className="btn-ghost w-full justify-start text-stone-600"><LogOut className="h-4 w-4" /> Log out</button></div></div>
}

export default function AppLayout() {
  const [mobileOpen, setMobileOpen] = useState(false)
  return <div className="min-h-screen bg-[radial-gradient(circle_at_top_right,_rgba(220,210,236,0.42),_transparent_32%),linear-gradient(to_bottom,_#fffdf8,_#faf6ed)]"><aside className="fixed inset-y-0 left-0 z-30 hidden w-64 border-r border-cream-200 bg-white/80 p-5 backdrop-blur lg:block"><Navigation /></aside><header className="sticky top-0 z-20 flex items-center justify-between border-b border-cream-200 bg-cream-50/90 px-4 py-3 backdrop-blur lg:hidden"><div className="flex items-center gap-2 font-display text-lg font-semibold text-wood-900"><BookOpen className="h-5 w-5 text-lavender-600" /> BookHaus</div><button type="button" onClick={() => setMobileOpen(true)} className="rounded-xl p-2 text-wood-700 hover:bg-cream-100" aria-label="Open navigation"><Menu className="h-6 w-6" /></button></header>{mobileOpen && <div className="fixed inset-0 z-40 bg-wood-900/30 lg:hidden" onMouseDown={(e) => { if (e.target === e.currentTarget) setMobileOpen(false) }}><aside className="h-full w-[min(19rem,85vw)] bg-white p-5 shadow-2xl"><div className="mb-2 flex justify-end"><button type="button" onClick={() => setMobileOpen(false)} className="rounded-lg p-1.5 text-stone-500 hover:bg-stone-100" aria-label="Close navigation"><X className="h-5 w-5" /></button></div><Navigation onNavigate={() => setMobileOpen(false)} /></aside></div>}<main className="px-4 py-7 sm:px-7 lg:ml-64 lg:px-10 lg:py-10"><div className="page-enter mx-auto max-w-7xl"><Outlet /></div></main></div>
}
