import { BookOpen, Sparkles } from 'lucide-react'
import { Outlet } from 'react-router-dom'
import roomHero from '../assets/room/bookhaus-room-hero.jpg'

export default function AuthLayout() {
  return <main className="auth-page"><div className="auth-shell"><section className="auth-art" style={{ backgroundImage: `linear-gradient(0deg, rgba(47,25,56,.72), rgba(47,25,56,.04) 65%), url(${roomHero})` }}><div className="brand-lockup !text-white"><span className="brand-mark !bg-white/90"><BookOpen /></span><span><strong>BookHaus</strong><small className="!text-lavender-100">Good books &middot; brighter days</small></span></div><div><Sparkles /><h2>A softer place for every story you carry.</h2><p>Collect books, keep the lines that linger, and watch your reading life unfold—one quiet page at a time.</p></div></section><section className="auth-form"><div className="w-full max-w-md"><div className="mb-8 flex items-center gap-3 text-lavender-700 md:hidden"><BookOpen className="h-7 w-7" /><span className="font-display text-2xl font-semibold">BookHaus</span></div><Outlet /></div></section></div></main>
}
