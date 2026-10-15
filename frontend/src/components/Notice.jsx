import { CheckCircle2, X } from 'lucide-react'

export default function Notice({ message, onDismiss }) {
  if (!message) return null
  return <div className="mb-5 flex items-center gap-3 rounded-xl border border-leaf-300 bg-leaf-100 px-4 py-3 text-sm text-leaf-700" role="status"><CheckCircle2 className="h-4 w-4 shrink-0" /><span className="flex-1">{message}</span>{onDismiss && <button type="button" onClick={onDismiss} aria-label="Dismiss message"><X className="h-4 w-4" /></button>}</div>
}
