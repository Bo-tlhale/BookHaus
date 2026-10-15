import { BookOpen } from 'lucide-react'

export default function EmptyState({ title, description, action, icon: Icon = BookOpen }) {
  return (
    <div className="panel flex flex-col items-center px-6 py-12 text-center">
      <div className="mb-4 rounded-full bg-lavender-100 p-4 text-lavender-600"><Icon className="h-7 w-7" /></div>
      <h2 className="font-display text-xl font-semibold text-wood-900">{title}</h2>
      {description && <p className="mt-2 max-w-md text-sm leading-6 text-stone-600">{description}</p>}
      {action && <div className="mt-5">{action}</div>}
    </div>
  )
}
