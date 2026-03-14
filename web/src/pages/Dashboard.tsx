import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { fetchBatches } from '../api'
import { StatusBadge } from '../components/StatusBadge'
import type { BatchSummary } from '../types'
import { formatDate } from '../util'

export function Dashboard() {
  const [batches, setBatches] = useState<BatchSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true
    fetchBatches()
      .then((data) => {
        if (active) setBatches(data)
      })
      .catch((err: unknown) => {
        if (active) setError(err instanceof Error ? err.message : 'Failed to load')
      })
      .finally(() => {
        if (active) setLoading(false)
      })
    return () => {
      active = false
    }
  }, [])

  const totalActivities = batches.reduce((s, b) => s + b.activityCount, 0)
  const totalParties = batches.reduce((s, b) => s + b.partyCount, 0)
  const draftCount = batches.filter((b) => b.filingStatus === 'DRAFT').length
  const reviewCount = batches.filter((b) => b.filingStatus === 'REVIEW').length
  const submittedCount = batches.filter(
    (b) => b.filingStatus === 'SUBMITTED' || b.filingStatus === 'ACKNOWLEDGED',
  ).length

  return (
    <>
      <div className="page-header">
        <p className="eyebrow">FinCEN SAR Platform</p>
        <h1>Dashboard</h1>
        <p>Filing inventory and workflow overview</p>
      </div>

      <div className="metric-grid" style={{ marginBottom: '1.5rem' }}>
        <div className="metric-card accent-card">
          <span className="metric-label">Total Batches</span>
          <strong>{batches.length}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Activities</span>
          <strong>{totalActivities}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Parties</span>
          <strong>{totalParties}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Drafts</span>
          <strong>{draftCount}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">In Review</span>
          <strong>{reviewCount}</strong>
        </div>
        <div className="metric-card">
          <span className="metric-label">Filed</span>
          <strong>{submittedCount}</strong>
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>Recent Batches</h2>
          <Link to="/batches" className="btn btn-secondary btn-sm">
            View All
          </Link>
        </div>

        {loading && <p className="state-banner">Loading batches…</p>}
        {error && <p className="state-banner error-banner">{error}</p>}

        {!loading && !error && (
          <div className="table-frame">
            <table>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Status</th>
                  <th>Activities</th>
                  <th>Parties</th>
                  <th>Form</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {batches.length === 0 ? (
                  <tr>
                    <td colSpan={6}>
                      <div className="empty-state">
                        <h3>No batches yet</h3>
                        <p>
                          <Link to="/batches" className="btn btn-primary btn-sm">
                            Create your first batch
                          </Link>
                        </p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  batches.slice(0, 10).map((b) => (
                    <tr key={b.id} className="link-row" onClick={() => {}}>
                      <td>
                        <Link to={`/batches/${b.id}`}>#{b.id}</Link>
                      </td>
                      <td>
                        <StatusBadge status={b.filingStatus} />
                      </td>
                      <td>{b.activityCount}</td>
                      <td>{b.partyCount}</td>
                      <td>{b.formTypeCode}</td>
                      <td>{formatDate(b.createdAt)}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}
