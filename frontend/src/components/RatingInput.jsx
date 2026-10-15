import { Star, X } from 'lucide-react'

export default function RatingInput({ value, onChange, disabled = false }) {
  const numeric = Number(value || 0)
  return (
    <div className="flex flex-wrap items-center gap-1" aria-label="Personal rating">
      {[1, 2, 3, 4, 5].map((rating) => (
        <button key={rating} type="button" disabled={disabled} onClick={() => onChange(rating)} className="rounded-md p-1 text-amber-500 hover:bg-amber-50" aria-label={`Rate ${rating} out of 5`}>
          <Star className="h-6 w-6" fill={numeric >= rating ? 'currentColor' : 'none'} />
        </button>
      ))}
      {value !== null && value !== undefined && <button type="button" disabled={disabled} onClick={() => onChange(null)} className="btn-ghost ml-1 !px-2 !py-1 text-stone-500" aria-label="Clear rating"><X className="h-4 w-4" /> Clear</button>}
    </div>
  )
}
