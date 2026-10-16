import { useEffect, useId, useRef } from 'react'
import { AlertTriangle, X } from 'lucide-react'

export default function ConfirmDialog({ open, title, description, confirmLabel = 'Confirm', onConfirm, onCancel, busy = false }) {
  const titleId = useId()
  const descriptionId = useId()
  const dialogRef = useRef(null)

  useEffect(() => {
    if (!open) return undefined
    const previousFocus = document.activeElement
    const dialog = dialogRef.current
    dialog?.querySelector('button')?.focus()

    function handleKeyDown(event) {
      if (event.key === 'Escape' && !busy) {
        event.preventDefault()
        onCancel()
        return
      }
      if (event.key !== 'Tab' || !dialog) return
      const focusable = [...dialog.querySelectorAll('button:not(:disabled)')]
      if (!focusable.length) return
      const first = focusable[0]
      const last = focusable.at(-1)
      if (event.shiftKey && document.activeElement === first) {
        event.preventDefault()
        last.focus()
      } else if (!event.shiftKey && document.activeElement === last) {
        event.preventDefault()
        first.focus()
      }
    }

    document.addEventListener('keydown', handleKeyDown)
    return () => {
      document.removeEventListener('keydown', handleKeyDown)
      previousFocus?.focus()
    }
  }, [open, busy, onCancel])

  if (!open) return null
  return <div className="fixed inset-0 z-50 flex items-center justify-center bg-wood-900/35 p-4" role="presentation" onMouseDown={(event) => { if (event.target === event.currentTarget && !busy) onCancel() }}><div ref={dialogRef} className="panel w-full max-w-md bg-white p-6" role="alertdialog" aria-modal="true" aria-labelledby={titleId} aria-describedby={descriptionId}><div className="flex items-start justify-between gap-4"><div className="rounded-full bg-red-50 p-3 text-red-600"><AlertTriangle className="h-5 w-5" aria-hidden="true" /></div><button type="button" onClick={onCancel} className="rounded-lg p-1 text-stone-500 hover:bg-stone-100" aria-label="Close" disabled={busy}><X className="h-5 w-5" aria-hidden="true" /></button></div><h2 id={titleId} className="mt-4 font-display text-2xl font-semibold text-wood-900">{title}</h2><p id={descriptionId} className="mt-2 text-sm leading-6 text-stone-600">{description}</p><div className="mt-6 flex justify-end gap-3"><button type="button" className="btn-secondary" onClick={onCancel} disabled={busy}>Cancel</button><button type="button" className="btn-danger" onClick={onConfirm} disabled={busy}>{busy ? 'Working…' : confirmLabel}</button></div></div></div>
}
