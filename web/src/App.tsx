import { useEffect, useState } from 'react'
import { fetchBatches } from './api'
import type { BatchSummary } from './types'
import './App.css'

function App() {
  const [batches, setBatches] = useState<BatchSummary[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    let active = true

    async function loadBatches() {
      try {
        const data = await fetchBatches()
        if (!active) {
          return
        }
        setBatches(data)
        setError(null)
      } catch (loadError) {
        if (!active) {
          return
        }
        setError(loadError instanceof Error ? loadError.message : 'Unable to load batches')
      } finally {
        if (active) {
          setIsLoading(false)
        }
      }
    }

    void loadBatches()

    return () => {
      active = false
    }
  }, [])

  const totalActivities = batches.reduce((sum, batch) => sum + batch.activityCount, 0)
  const totalParties = batches.reduce((sum, batch) => sum + batch.partyCount, 0)

  return (
    <main className="app-shell">
      <section className="hero-panel">
        <p className="eyebrow">FinCEN SAR Platform</p>
        <div className="hero-copy">
          <h1>React 19 frontend on top of your Spring Boot SAR API.</h1>
          <p className="hero-text">
            This workspace is the first browser-facing layer for batch intake, SAR drafting,
            review workflow, and eventual microservice decomposition.
          </p>
        </div>
        <div className="hero-grid">
          <article className="metric-card accent-card">
            <span className="metric-label">Backend target</span>
            <strong>Spring Boot 4.0.3</strong>
            <p>Single deployable service now, with room for selective extraction later.</p>
          </article>
          <article className="metric-card">
            <span className="metric-label">Frontend target</span>
            <strong>React 19.2</strong>
            <p>Vite + TypeScript shell ready for routing, form flows, and API composition.</p>
          </article>
          <article className="metric-card">
            <span className="metric-label">API contract</span>
            <strong>/api/v1</strong>
            <p>Current endpoints already align to SAR step workflows and granular save actions.</p>
          </article>
        </div>
      </section>

      <section className="workspace-panel">
        <div className="workspace-header">
          <div>
            <p className="section-label">Workspace Snapshot</p>
            <h2>Current filing inventory</h2>
          </div>
          <div className="summary-strip">
            <div>
              <span>Batches</span>
              <strong>{batches.length}</strong>
            </div>
            <div>
              <span>Activities</span>
              <strong>{totalActivities}</strong>
            </div>
            <div>
              <span>Parties</span>
              <strong>{totalParties}</strong>
            </div>
          </div>
        </div>

        {isLoading ? <p className="state-banner">Loading batches from the Spring API...</p> : null}
        {error ? <p className="state-banner error-banner">{error}</p> : null}

        {!isLoading && !error ? (
          <div className="table-frame">
            <table>
              <thead>
                <tr>
                  <th>Batch</th>
                  <th>Activities</th>
                  <th>Parties</th>
                  <th>Form Type</th>
                  <th>Created</th>
                </tr>
              </thead>
              <tbody>
                {batches.length > 0 ? (
                  batches.map((batch) => (
                    <tr key={batch.id}>
                      <td>#{batch.id}</td>
                      <td>{batch.activityCount}</td>
                      <td>{batch.partyCount}</td>
                      <td>{batch.formTypeCode}</td>
                      <td>{formatDate(batch.createdAt)}</td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={5}>
                      No batches yet. Start by creating a batch through the API or the next UI flow.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        ) : null}
      </section>

      <section className="roadmap-panel">
        <div>
          <p className="section-label">Immediate build-out</p>
          <h2>Recommended next application slices</h2>
        </div>
        <div className="roadmap-grid">
          <article>
            <h3>Batch Intake</h3>
            <p>Create, list, and open filing batches with query-backed dashboard cards.</p>
          </article>
          <article>
            <h3>Activity Drafting</h3>
            <p>Map each wizard step directly to the existing PATCH endpoints already in the backend.</p>
          </article>
          <article>
            <h3>Reference Data</h3>
            <p>Move SAR code tables and lookup values into a dedicated backend module or service.</p>
          </article>
          <article>
            <h3>Workflow and Review</h3>
            <p>Add user auth, status transitions, audit history, and role-specific review screens.</p>
          </article>
        </div>
      </section>
    </main>
  )
}

function formatDate(value: string): string {
  const parsed = new Date(value)
  if (Number.isNaN(parsed.getTime())) {
    return value
  }
  return new Intl.DateTimeFormat('en-US', {
    year: 'numeric',
    month: 'short',
    day: '2-digit',
  }).format(parsed)
}

export default App
