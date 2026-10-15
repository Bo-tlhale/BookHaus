import { useState } from 'react'
import { ArrowRight } from 'lucide-react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import ErrorMessage from '../components/ErrorMessage'
import { useAuth } from '../context/useAuth'

export default function LoginPage() {
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  async function handleSubmit(event) {
    event.preventDefault(); setError(null); setSubmitting(true)
    try { await login(form); navigate(location.state?.from?.pathname || '/', { replace: true }) }
    catch (err) { setError(err) }
    finally { setSubmitting(false) }
  }
  return <><p className="text-sm font-bold uppercase tracking-[0.18em] text-lavender-600">Welcome back</p><h1 className="mt-2 font-display text-4xl font-semibold text-wood-900">Return to your books</h1><p className="mt-3 text-sm leading-6 text-stone-600">Sign in to pick up where you left off.</p><form className="mt-8 space-y-5" onSubmit={handleSubmit}><ErrorMessage error={error} /><div><label className="label" htmlFor="email">Email address</label><input className="field" id="email" type="email" autoComplete="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div><div><label className="label" htmlFor="password">Password</label><input className="field" id="password" type="password" autoComplete="current-password" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /></div><button className="btn-primary w-full" disabled={submitting}>{submitting ? 'Opening BookHaus…' : <>Sign in <ArrowRight className="h-4 w-4" /></>}</button></form><p className="mt-7 text-center text-sm text-stone-600">New to BookHaus? <Link className="font-semibold text-lavender-700 hover:underline" to="/register">Create an account</Link></p></>
}
