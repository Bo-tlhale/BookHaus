export default function ProgressBar({ value, label = 'Reading progress', showLabel = true }) {
  if (value === null || value === undefined) return null
  const bounded = Math.max(0, Math.min(Number(value), 100))
  return (
    <div>
      {showLabel && <div className="mb-1 flex justify-between text-xs text-stone-500"><span>{label}</span><span>{bounded.toFixed(bounded % 1 ? 1 : 0)}%</span></div>}
      <div className="h-2 overflow-hidden rounded-full bg-lavender-100" role="progressbar" aria-label={label} aria-valuemin="0" aria-valuemax="100" aria-valuenow={bounded}>
        <div className="h-full rounded-full bg-gradient-to-r from-lavender-500 to-skybook-400 transition-all" style={{ width: `${bounded}%` }} />
      </div>
    </div>
  )
}
