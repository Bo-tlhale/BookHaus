export function Skeleton({ className = '' }) { return <div className={`animate-pulse rounded-xl bg-lavender-100/70 ${className}`} aria-hidden="true" /> }

export function CardGridSkeleton({ count = 6 }) {
  return <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-3">{Array.from({ length: count }, (_, i) => <div key={i} className="panel flex gap-4 p-4"><Skeleton className="h-36 w-24 shrink-0" /><div className="flex-1 space-y-3"><Skeleton className="h-5 w-4/5" /><Skeleton className="h-4 w-2/3" /><Skeleton className="h-8 w-1/2" /></div></div>)}</div>
}

export function PageLoader({ label = 'Loading…' }) {
  return <div className="flex min-h-screen flex-col items-center justify-center bg-cream-50" role="status" aria-live="polite"><div className="mb-4 flex gap-1.5" aria-hidden="true"><span className="h-2.5 w-2.5 animate-bounce rounded-full bg-lavender-500" /><span className="h-2.5 w-2.5 animate-bounce rounded-full bg-skybook-400 [animation-delay:120ms]" /><span className="h-2.5 w-2.5 animate-bounce rounded-full bg-leaf-500 [animation-delay:240ms]" /></div><p className="text-sm text-stone-600">{label}</p></div>
}
