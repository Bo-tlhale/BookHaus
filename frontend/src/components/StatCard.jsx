export default function StatCard({ label, value, detail, icon: Icon, tone = 'lavender' }) {
  const tones = { lavender: 'bg-lavender-100 text-lavender-700', blue: 'bg-skybook-100 text-skybook-600', green: 'bg-leaf-100 text-leaf-700', wood: 'bg-wood-100 text-wood-700' }
  return <div className="panel p-5"><div className={`mb-4 inline-flex rounded-xl p-2.5 ${tones[tone]}`}>{Icon && <Icon className="h-5 w-5" aria-hidden="true" />}</div><div><p className="text-sm font-medium text-stone-500">{label}</p><p className="mt-1 font-display text-3xl font-semibold text-plum">{value ?? '—'}</p>{detail && <p className="mt-2 text-xs text-stone-500">{detail}</p>}</div></div>
}
