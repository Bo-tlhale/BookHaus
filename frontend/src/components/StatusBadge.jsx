import { STATUS_LABELS, STATUS_STYLES } from '../utils/status'

export default function StatusBadge({ status }) {
  if (!status) return null
  return <span className={`inline-flex rounded-full border px-2.5 py-1 text-xs font-semibold ${STATUS_STYLES[status] || STATUS_STYLES.TO_READ}`}>{STATUS_LABELS[status] || status}</span>
}
