import { AlertCircle, X } from 'lucide-react'

export default function ErrorMessage({ error, onDismiss }) {
  if (!error) return null
  const message = typeof error === 'string' ? error : error.message
  return <div className="mb-5 flex items-start gap-3 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800" role="alert"><AlertCircle className="mt-0.5 h-4 w-4 shrink-0" /><span className="flex-1">{message}</span>{onDismiss && <button type="button" onClick={onDismiss} aria-label="Dismiss error" className="rounded p-0.5 hover:bg-red-100"><X className="h-4 w-4" /></button>}</div>
}
