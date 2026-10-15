import { BookOpen } from 'lucide-react'

export default function GoalRing({ value = 0, completed, target, label = 'reading goal' }) {
  const bounded = Math.min(100, Math.max(0, Number(value) || 0))
  return <div className="goal-ring" style={{ '--goal-progress': `${bounded * 3.6}deg` }} role="progressbar" aria-label={label} aria-valuemin="0" aria-valuemax="100" aria-valuenow={bounded}><div className="goal-ring__inner"><BookOpen aria-hidden="true" /><strong>{completed}</strong><span>of {target} books</span><b>{bounded.toFixed(0)}%</b></div></div>
}
