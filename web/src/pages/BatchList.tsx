import { useEffect, useState, useCallback } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { createBatch, deleteBatch, fetchBatches } from '../api'
import { StatusBadge } from '../components/StatusBadge'
import type { BatchRequest, BatchSummary, FilingStatus } from '../types'
import { FILING_STATUSES } from '../types'
import { formatDate } from '../util'

export function BatchList() {
  const navigate = useNavigate()
  const [batches, setBatches] = useState<BatchSummary[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [showForm, setShowForm] = useState(false)
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)
  const [statusFilter, setStatusFilter] = useState<FilingStatus | ''>('')
  const pageSize = 20

  const load = useCallback(() => {
    setLoading(true)
    fetchBatches({
      page,
      size: pageSize,
      status: statusFilter || undefined,
      sort: 'createdAt',
      direction: 'desc',
    })
      .then((resp) => {
        setBatches(resp.content)
        setTotalPages(resp.totalPages)
        setTotalElements(resp.totalElements)
      })
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed'))
      .finally(() => setLoading(false))
  }, [page, statusFilter])

  useEffect(() => {
    load()
  }, [load])

  async function handleCreate(data: BatchRequest) {
    const batch = await createBatch(data)
    setShowForm(false)
    navigate(`/batches/${batch.id}`)
  }

  async function handleDelete(id: number) {
    await deleteBatch(id)
    load()
  }

  function handleStatusChange(e: React.ChangeEvent<HTMLSelectElement>) {
    setStatusFilter(e.target.value as FilingStatus | '')
    setPage(0)
  }

  return (
    <>
      <div className="page-header">
        <p className="eyebrow">Filing Management</p>
        <h1>Batches</h1>
      </div>

      <div className="card">
        <div className="card-header">
          <h2>All Batches {totalElements > 0 && <small>({totalElements})</small>}</h2>
          <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
            <select value={statusFilter} onChange={handleStatusChange} className="form-select">
              <option value="">All Statuses</option>
              {FILING_STATUSES.map((s) => (
                <option key={s} value={s}>
                  {s}
                </option>
              ))}
            </select>
            <button className="btn btn-primary btn-sm" onClick={() => setShowForm(!showForm)}>
              {showForm ? 'Cancel' : '+ New Batch'}
            </button>
          </div>
        </div>

        {showForm && (
          <div style={{ marginBottom: '1rem' }}>
            <CreateBatchForm onSubmit={handleCreate} />
          </div>
        )}

        {loading && <p className="state-banner">Loading…</p>}
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
                  <th></th>
                </tr>
              </thead>
              <tbody>
                {batches.length === 0 ? (
                  <tr>
                    <td colSpan={7}>
                      <div className="empty-state">
                        <h3>No batches</h3>
                        <p>Create your first filing batch to get started.</p>
                      </div>
                    </td>
                  </tr>
                ) : (
                  batches.map((b) => (
                    <tr key={b.id}>
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
                      <td>
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => handleDelete(b.id)}
                        >
                          Delete
                        </button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        )}

        {!loading && totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '0.75rem 0' }}>
            <span className="text-muted">
              Page {page + 1} of {totalPages}
            </span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button
                className="btn btn-sm"
                disabled={page === 0}
                onClick={() => setPage((p) => p - 1)}
              >
                ← Previous
              </button>
              <button
                className="btn btn-sm"
                disabled={page >= totalPages - 1}
                onClick={() => setPage((p) => p + 1)}
              >
                Next →
              </button>
            </div>
          </div>
        )}
      </div>
    </>
  )
}

function CreateBatchForm({ onSubmit }: { onSubmit: (data: BatchRequest) => void }) {
  const [activityCount, setActivityCount] = useState(1)
  const [partyCount, setPartyCount] = useState(0)
  const [totalAmount, setTotalAmount] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSubmit({
      activityCount,
      partyCount,
      totalAmount: totalAmount ? Number(totalAmount) : undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit} className="card" style={{ background: 'rgba(242,226,191,0.3)' }}>
      <h3 style={{ marginBottom: '0.75rem' }}>Create New Batch</h3>
      <div className="form-grid">
        <div className="form-field">
          <label>Activity Count</label>
          <input
            type="number"
            min={1}
            value={activityCount}
            onChange={(e) => setActivityCount(Number(e.target.value))}
            required
          />
        </div>
        <div className="form-field">
          <label>Party Count</label>
          <input
            type="number"
            min={0}
            value={partyCount}
            onChange={(e) => setPartyCount(Number(e.target.value))}
            required
          />
        </div>
        <div className="form-field">
          <label>Total Amount (optional)</label>
          <input
            type="number"
            step="0.01"
            value={totalAmount}
            onChange={(e) => setTotalAmount(e.target.value)}
            placeholder="0.00"
          />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary">
          Create Batch
        </button>
      </div>
    </form>
  )
}
