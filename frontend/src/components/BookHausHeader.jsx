import { useState } from 'react'
import { BookMarked, BookOpen, ChevronDown, Feather, Home, Library, LogOut, Menu, Search, Sparkles, Target, X } from 'lucide-react'
import { NavLink, useNavigate } from 'react-router-dom'
import avatar from '../assets/branding/reader-avatar.jpg'
import { useAuth } from '../context/useAuth'

const navItems = [
  { to: '/', label: 'Home', icon: Home, end: true },
  { to: '/search', label: 'Discover', icon: Search },
  { to: '/library', label: 'My Library', icon: Library },
  { to: '/shelves', label: 'Shelves', icon: BookMarked },
  { to: '/journal', label: 'Journal', icon: Feather },
  { to: '/goals', label: 'Goals', icon: Target },
  { to: '/customize', label: 'Customize', icon: Sparkles },
]

function Brand() {
  return <NavLink to="/" className="brand-lockup"><span className="brand-mark"><BookOpen aria-hidden="true" /></span><span><strong>BookHaus</strong><small>Good books &middot; brighter days</small></span></NavLink>
}

function NavItems({ onNavigate }) {
  return navItems.map(({ to, label, icon: Icon, end }) => <NavLink key={to} to={to} end={end} onClick={onNavigate} className={({ isActive }) => `top-nav__link ${isActive ? 'top-nav__link--active' : ''}`}><Icon aria-hidden="true" /> <span>{label}</span></NavLink>)
}

export default function BookHausHeader() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [query, setQuery] = useState('')
  const [mobileOpen, setMobileOpen] = useState(false)
  const [profileOpen, setProfileOpen] = useState(false)

  function submitSearch(event) {
    event.preventDefault()
    if (query.trim()) navigate(`/search?q=${encodeURIComponent(query.trim())}`)
  }

  return <header className="site-header"><div className="site-header__inner"><Brand /><nav className="top-nav" aria-label="Main navigation"><NavItems /></nav><div className="site-header__tools"><form className="global-search" onSubmit={submitSearch}><label className="sr-only" htmlFor="global-search">Search books</label><Search aria-hidden="true" /><input id="global-search" value={query} onChange={(event) => setQuery(event.target.value)} placeholder="Search books, authors, genres..." /></form><div className="relative"><button type="button" className="profile-button" onClick={() => setProfileOpen((open) => !open)} aria-expanded={profileOpen} aria-label="Open profile menu"><img src={avatar} alt="" /><span className="hidden xl:block">{user?.name?.split(' ')[0]}</span><ChevronDown aria-hidden="true" /></button>{profileOpen && <div className="profile-menu"><p>{user?.name}</p><span>{user?.email}</span><button type="button" onClick={logout}><LogOut aria-hidden="true" /> Log out</button></div>}</div><button type="button" className="mobile-menu-button" onClick={() => setMobileOpen(true)} aria-label="Open navigation"><Menu /></button></div></div>{mobileOpen && <div className="mobile-nav-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) setMobileOpen(false) }}><aside className="mobile-nav"><div className="flex items-center justify-between"><Brand /><button type="button" className="icon-button" onClick={() => setMobileOpen(false)} aria-label="Close navigation"><X /></button></div><nav className="mt-8 grid gap-2"><NavItems onNavigate={() => setMobileOpen(false)} /></nav><button type="button" className="btn-secondary mt-auto w-full" onClick={logout}><LogOut className="h-4 w-4" /> Log out</button></aside></div>}</header>
}
