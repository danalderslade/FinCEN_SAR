import { STATUS_COLORS } from '../types'
import type { FilingStatus } from '../types'
import './StatusBadge.css'

export function StatusBadge({ status }: { status: string }) {
  const color = STATUS_COLORS[status as FilingStatus] ?? '#6b7356'
  return (
    <span className="status-badge" style={{ '--badge-color': color } as React.CSSProperties}>
      {status}
    </span>
  )
}
