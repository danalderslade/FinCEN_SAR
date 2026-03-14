import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  acknowledgeBatch,
  createActivity,
  deleteBatch,
  downloadBsaXml,
  fetchBatch,
  rejectBatch,
  returnToDraft,
  submitForReview,
  submitToFincen,
} from '../api'
import { StatusBadge } from '../components/StatusBadge'
import type { ActivityRequest, BatchResponse } from '../types'
import { formatCurrency, formatDate } from '../util'

export function BatchDetail() {
  const { batchId } = useParams<{ batchId: string }>()
  const navigate = useNavigate()
  const [batch, setBatch] = useState<BatchResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showAddActivity, setShowAddActivity] = useState(false)

  const load = useCallback(() => {
    if (!batchId) return
    setLoading(true)
    fetchBatch(Number(batchId))
      .then(setBatch)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed'))
      .finally(() => setLoading(false))
  }, [batchId])

  useEffect(() => {
    load()
  }, [load])

  async function handleWorkflow(action: (id: number) => Promise<BatchResponse>) {
    if (!batch) return
    try {
      const updated = await action(batch.id)
      setBatch(updated)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Workflow action failed')
    }
  }

  async function handleDelete() {
    if (!batch) return
    if (!window.confirm('Delete this batch and all its activities? This cannot be undone.')) return
    await deleteBatch(batch.id)
    navigate('/batches')
  }

  async function handleDownloadXml() {
    if (!batch) return
    try {
      const blob = await downloadBsaXml(batch.id)
      const url = URL.createObjectURL(blob)
      const a = document.createElement('a')
      a.href = url
      a.download = `batch-${batch.id}-bsa.xml`
      a.click()
      URL.revokeObjectURL(url)
    } catch (e) {
      setError(e instanceof Error ? e.message : 'XML download failed')
    }
  }

  async function handleAddActivity(data: ActivityRequest) {
    if (!batch) return
    await createActivity(batch.id, data)
    setShowAddActivity(false)
    load()
  }

  if (loading) return <p className="state-banner">Loading batch…</p>
  if (error) return <p className="state-banner error-banner">{error}</p>
  if (!batch) return <p className="state-banner error-banner">Batch not found</p>

  const status = batch.filingStatus

  return (
    <>
      <Link to="/batches" className="back-link">
        ← Back to Batches
      </Link>

      <div className="page-header">
        <p className="eyebrow">Batch #{batch.id}</p>
        <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          Filing Batch <StatusBadge status={status} />
        </h1>
      </div>

      {/* Detail Grid */}
      <div className="card" style={{ marginBottom: '1rem' }}>
        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">Form Type</span>
            <span className="detail-value">{batch.formTypeCode}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Total Amount</span>
            <span className="detail-value">{formatCurrency(batch.totalAmount)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Activities</span>
            <span className="detail-value">{batch.activityCount}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Parties</span>
            <span className="detail-value">{batch.partyCount}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Created</span>
            <span className="detail-value">{formatDate(batch.createdAt)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Updated</span>
            <span className="detail-value">{formatDate(batch.updatedAt)}</span>
          </div>
        </div>
      </div>

      {/* Workflow Actions */}
      <div className="card" style={{ marginBottom: '1rem' }}>
        <h3 style={{ marginBottom: '0.75rem' }}>Workflow</h3>
        <div className="workflow-actions">
          {status === 'DRAFT' && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => handleWorkflow(submitForReview)}
            >
              Submit for Review
            </button>
          )}
          {status === 'REVIEW' && (
            <>
              <button
                className="btn btn-primary btn-sm"
                onClick={() => handleWorkflow(submitToFincen)}
              >
                Submit to FinCEN
              </button>
              <button
                className="btn btn-secondary btn-sm"
                onClick={() => handleWorkflow(returnToDraft)}
              >
                Return to Draft
              </button>
            </>
          )}
          {status === 'SUBMITTED' && (
            <>
              <button
                className="btn btn-primary btn-sm"
                onClick={() => handleWorkflow(acknowledgeBatch)}
              >
                Acknowledge
              </button>
              <button
                className="btn btn-danger btn-sm"
                onClick={() => handleWorkflow(rejectBatch)}
              >
                Reject
              </button>
            </>
          )}
          <button className="btn btn-secondary btn-sm" onClick={handleDownloadXml}>
            Download BSA XML
          </button>
          {status === 'DRAFT' && (
            <button className="btn btn-danger btn-sm" onClick={handleDelete}>
              Delete Batch
            </button>
          )}
        </div>
      </div>

      {/* Activities */}
      <div className="card">
        <div className="card-header">
          <h2>Activities ({batch.activities.length})</h2>
          {status === 'DRAFT' && (
            <button
              className="btn btn-primary btn-sm"
              onClick={() => setShowAddActivity(!showAddActivity)}
            >
              {showAddActivity ? 'Cancel' : '+ Add Activity'}
            </button>
          )}
        </div>

        {showAddActivity && <QuickAddActivity onSubmit={handleAddActivity} />}

        <div className="table-frame">
          <table>
            <thead>
              <tr>
                <th>ID</th>
                <th>Seq</th>
                <th>Status</th>
                <th>Filing Date</th>
                <th>BSA ID</th>
                <th>Created</th>
              </tr>
            </thead>
            <tbody>
              {batch.activities.length === 0 ? (
                <tr>
                  <td colSpan={6}>
                    <div className="empty-state">
                      <p>No activities in this batch yet.</p>
                    </div>
                  </td>
                </tr>
              ) : (
                batch.activities.map((a) => (
                  <tr key={a.id}>
                    <td>
                      <Link to={`/activities/${a.id}`}>#{a.id}</Link>
                    </td>
                    <td>{a.seqNum}</td>
                    <td>
                      <StatusBadge status={a.filingStatus} />
                    </td>
                    <td>{a.filingDate}</td>
                    <td>{a.bsaIdentifier ?? '—'}</td>
                    <td>{formatDate(a.createdAt)}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

function QuickAddActivity({ onSubmit }: { onSubmit: (data: ActivityRequest) => void }) {
  const [filingDate, setFilingDate] = useState(new Date().toISOString().slice(0, 10))

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSubmit({
      seqNum: 1,
      filingDate,
      parties: [],
      ipAddresses: [],
      cyberEvents: [],
      assets: [],
      assetAttributes: [],
      narratives: [],
    })
  }

  return (
    <form
      onSubmit={handleSubmit}
      className="card"
      style={{ background: 'rgba(242,226,191,0.3)', marginBottom: '1rem' }}
    >
      <h3 style={{ marginBottom: '0.75rem' }}>Quick Add Activity</h3>
      <div className="form-grid">
        <div className="form-field">
          <label>Filing Date</label>
          <input
            type="date"
            value={filingDate}
            onChange={(e) => setFilingDate(e.target.value)}
            required
          />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary">
          Create Activity
        </button>
      </div>
      <p style={{ fontSize: '0.8rem', color: 'var(--muted)', marginTop: '0.5rem' }}>
        Creates a minimal activity. Use the wizard to add parties, suspicious activity, and other
        details.
      </p>
    </form>
  )
}
