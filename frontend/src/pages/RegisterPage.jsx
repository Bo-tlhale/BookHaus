import { useState } from 'react'
import { ArrowRight } from 'lucide-react'
import { Link, useNavigate } from 'react-router-dom'
import ErrorMessage from '../components/ErrorMessage'
import { useAuth } from '../context/useAuth'

export default function RegisterPage() {
  const [form, setForm] = useState({ name: '', email: '', password: '' })
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const { register } = useAuth(); const navigate = useNavigate()
  async function handleSubmit(event) { event.preventDefault(); setError(null); if (form.password.length < 8) { setError('Password must be at least 8 characters.'); return } setSubmitting(true); try { await register(form); navigate('/', { replace: true }) } catch (err) { setError(err) } finally { setSubmitting(false) } }
  return <><p className="text-sm font-bold uppercase tracking-[0.18em] text-lavender-600">Begin a new chapter</p><h1 className="mt-2 font-display text-4xl font-semibold text-wood-900">Make BookHaus yours</h1><p className="mt-3 text-sm leading-6 text-stone-600">A calm corner for your library, thoughts, and reading goals.</p><form className="mt-8 space-y-4" onSubmit={handleSubmit}><ErrorMessage error={error} /><div><label className="label" htmlFor="name">Name</label><input className="field" id="name" autoComplete="name" maxLength="120" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div><div><label className="label" htmlFor="register-email">Email address</label><input className="field" id="register-email" type="email" autoComplete="email" required value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} /></div><div><label className="label" htmlFor="register-password">Password</label><input className="field" id="register-password" type="password" autoComplete="new-password" minLength="8" maxLength="72" required value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} /><p className="mt-1.5 text-xs text-stone-500">Use at least 8 characters.</p></div><button className="btn-primary mt-2 w-full" disabled={submitting}>{submitting ? 'Creating your space…' : <>Create account <ArrowRight className="h-4 w-4" /></>}</button></form><p className="mt-6 text-center text-sm text-stone-600">Already have an account? <Link className="font-semibold text-lavender-700 hover:underline" to="/login">Sign in</Link></p></>
}
