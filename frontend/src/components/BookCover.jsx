import { BookOpen } from 'lucide-react'

export default function BookCover({ src, title, className = '', compact = false }) {
  if (src) return <img src={src} alt={`Cover of ${title}`} className={`bg-cream-100 object-cover ${className}`} loading="lazy" />
  return (
    <div className={`flex flex-col items-center justify-center bg-gradient-to-br from-lavender-200 via-skybook-100 to-cream-100 p-3 text-center text-lavender-700 ${className}`} role="img" aria-label={`No cover available for ${title}`}>
      <BookOpen className={compact ? 'h-5 w-5' : 'mb-2 h-8 w-8'} aria-hidden="true" />
      {!compact && <span className="line-clamp-3 font-display text-xs font-semibold">{title}</span>}
    </div>
  )
}
