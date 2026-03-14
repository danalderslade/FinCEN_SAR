import { useCallback, useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { deleteActivity, fetchActivity } from '../api'
import { StatusBadge } from '../components/StatusBadge'
import type { ActivityResponse, PartyResponse } from '../types'
import { PARTY_TYPE_LABELS } from '../types'
import { formatCurrency, formatDate } from '../util'

const TABS = ['Overview', 'Parties', 'Suspicious Activity', 'IP / Cyber', 'Assets', 'Narratives'] as const
type Tab = (typeof TABS)[number]

export function ActivityDetail() {
  const { activityId } = useParams<{ activityId: string }>()
  const [activity, setActivity] = useState<ActivityResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tab, setTab] = useState<Tab>('Overview')

  const load = useCallback(() => {
    if (!activityId) return
    setLoading(true)
    fetchActivity(Number(activityId))
      .then(setActivity)
      .catch((e: unknown) => setError(e instanceof Error ? e.message : 'Failed'))
      .finally(() => setLoading(false))
  }, [activityId])

  useEffect(() => {
    load()
  }, [load])

  if (loading) return <p className="state-banner">Loading activity…</p>
  if (error) return <p className="state-banner error-banner">{error}</p>
  if (!activity) return <p className="state-banner error-banner">Activity not found</p>

  return (
    <>
      <Link to={`/batches/${activity.batchId}`} className="back-link">
        ← Back to Batch #{activity.batchId}
      </Link>

      <div className="page-header">
        <p className="eyebrow">Activity #{activity.id}</p>
        <h1 style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
          SAR Activity <StatusBadge status={activity.filingStatus} />
        </h1>
      </div>

      <div className="card" style={{ marginBottom: '1rem' }}>
        <div className="detail-grid">
          <div className="detail-item">
            <span className="detail-label">Seq #</span>
            <span className="detail-value">{activity.seqNum}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Filing Date</span>
            <span className="detail-value">{activity.filingDate}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">BSA ID</span>
            <span className="detail-value">{activity.bsaIdentifier ?? '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Created</span>
            <span className="detail-value">{formatDate(activity.createdAt)}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Prior Doc #</span>
            <span className="detail-value">{activity.efilingPriorDocumentNumber ?? '—'}</span>
          </div>
          <div className="detail-item">
            <span className="detail-label">Note to FinCEN</span>
            <span className="detail-value">
              {activity.filingInstitutionNoteToFincen ?? '—'}
            </span>
          </div>
        </div>
      </div>

      {/* Action bar */}
      <div className="workflow-actions" style={{ marginBottom: '1rem' }}>
        <Link
          to={`/activities/${activity.id}/wizard/1`}
          className="btn btn-primary btn-sm"
        >
          Open Wizard
        </Link>
        <DeleteButton activityId={activity.id} batchId={activity.batchId} />
      </div>

      {/* Tabs */}
      <div className="card">
        <div className="tab-bar">
          {TABS.map((t) => (
            <button key={t} className={t === tab ? 'active' : ''} onClick={() => setTab(t)}>
              {t}
            </button>
          ))}
        </div>

        {tab === 'Overview' && <OverviewTab activity={activity} />}
        {tab === 'Parties' && <PartiesTab parties={activity.parties} />}
        {tab === 'Suspicious Activity' && <SuspiciousTab activity={activity} />}
        {tab === 'IP / Cyber' && <IpCyberTab activity={activity} />}
        {tab === 'Assets' && <AssetsTab activity={activity} />}
        {tab === 'Narratives' && <NarrativesTab activity={activity} />}
      </div>
    </>
  )
}

function DeleteButton({ activityId, batchId }: { activityId: number; batchId: number }) {
  const [confirming, setConfirming] = useState(false)
  const navigate = Link // we need useNavigate
  void navigate // suppress lint

  async function handleDelete() {
    await deleteActivity(activityId)
    window.location.href = `/batches/${batchId}`
  }

  if (confirming) {
    return (
      <>
        <button className="btn btn-danger btn-sm" onClick={handleDelete}>
          Confirm Delete
        </button>
        <button className="btn btn-secondary btn-sm" onClick={() => setConfirming(false)}>
          Cancel
        </button>
      </>
    )
  }
  return (
    <button className="btn btn-danger btn-sm" onClick={() => setConfirming(true)}>
      Delete Activity
    </button>
  )
}

function OverviewTab({ activity }: { activity: ActivityResponse }) {
  const assoc = activity.activityAssociation
  const doc = activity.activitySupportDocument

  return (
    <>
      <h3>Filing Type</h3>
      {assoc ? (
        <div className="detail-grid" style={{ marginTop: '0.5rem' }}>
          <Detail label="Initial Report" value={boolStr(assoc.initialReportIndicator)} />
          <Detail label="Corrects/Amends" value={boolStr(assoc.correctsAmendsPriorReport)} />
          <Detail label="Continuing Activity" value={boolStr(assoc.continuingActivityReport)} />
          <Detail label="Joint Report" value={boolStr(assoc.jointReportIndicator)} />
        </div>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>Not set</p>
      )}

      <h3 style={{ marginTop: '1.25rem' }}>Support Document</h3>
      {doc ? (
        <p style={{ marginTop: '0.25rem' }}>{doc.originalAttachmentFileName}</p>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>None</p>
      )}

      <h3 style={{ marginTop: '1.25rem' }}>Summary</h3>
      <div className="detail-grid" style={{ marginTop: '0.5rem' }}>
        <Detail label="Parties" value={String(activity.parties.length)} />
        <Detail label="IP Addresses" value={String(activity.ipAddresses.length)} />
        <Detail label="Cyber Events" value={String(activity.cyberEvents.length)} />
        <Detail label="Assets" value={String(activity.assets.length)} />
        <Detail label="Narratives" value={String(activity.narratives.length)} />
      </div>
    </>
  )
}

function PartiesTab({ parties }: { parties: PartyResponse[] }) {
  if (parties.length === 0) {
    return (
      <div className="empty-state">
        <h3>No parties</h3>
        <p>Use the wizard to add parties to this activity.</p>
      </div>
    )
  }

  return (
    <>
      {parties.map((p) => (
        <div key={p.id} className="card" style={{ marginBottom: '0.75rem' }}>
          <div className="card-header">
            <h3>
              {PARTY_TYPE_LABELS[p.activityPartyTypeCode] ?? `Type ${p.activityPartyTypeCode}`}
            </h3>
            <span style={{ fontSize: '0.8rem', color: 'var(--muted)' }}>ID: {p.id}</span>
          </div>
          <div className="detail-grid">
            {p.names.map((n) => (
              <Detail
                key={n.id}
                label={`Name (${n.partyNameTypeCode})`}
                value={
                  (n.rawPartyFullName ??
                  [n.rawIndividualFirstName, n.rawIndividualMiddleName, n.rawEntityIndividualLastName]
                    .filter(Boolean)
                    .join(' ')) || '—'
                }
              />
            ))}
            {p.addresses.map((a) => (
              <Detail
                key={a.id}
                label="Address"
                value={
                  [a.rawStreetAddress1, a.rawCity, a.rawStateCode, a.rawZipCode, a.rawCountryCode]
                    .filter(Boolean)
                    .join(', ') || '—'
                }
              />
            ))}
            {p.phones.map((ph) => (
              <Detail key={ph.id} label="Phone" value={ph.phoneNumberText ?? '—'} />
            ))}
            {p.identifications.map((id) => (
              <Detail key={id.id} label="ID" value={id.partyIdentificationNumber ?? '—'} />
            ))}
            {p.individualBirthDate && (
              <Detail label="Birth Date" value={p.individualBirthDate} />
            )}
            {p.lossToFinancialAmount != null && (
              <Detail label="Loss Amount" value={formatCurrency(p.lossToFinancialAmount)} />
            )}
          </div>
        </div>
      ))}
    </>
  )
}

function SuspiciousTab({ activity }: { activity: ActivityResponse }) {
  const sa = activity.suspiciousActivity
  if (!sa) {
    return (
      <div className="empty-state">
        <h3>No suspicious activity data</h3>
        <p>Use the wizard to configure suspicious activity details.</p>
      </div>
    )
  }

  return (
    <>
      <div className="detail-grid">
        <Detail label="Total Amount" value={formatCurrency(sa.totalSuspiciousAmount)} />
        <Detail label="Amount Unknown" value={boolStr(sa.amountUnknown)} />
        <Detail label="No Amount" value={boolStr(sa.noAmountInvolved)} />
        <Detail label="From Date" value={sa.suspiciousActivityFromDate ?? '—'} />
        <Detail label="To Date" value={sa.suspiciousActivityToDate ?? '—'} />
        <Detail
          label="Cumulative Total"
          value={formatCurrency(sa.cumulativeTotalViolationAmount)}
        />
      </div>
      {sa.classifications.length > 0 && (
        <>
          <h3 style={{ marginTop: '1rem' }}>Classifications</h3>
          <div className="table-frame" style={{ marginTop: '0.5rem' }}>
            <table>
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Subtype</th>
                  <th>Other Text</th>
                </tr>
              </thead>
              <tbody>
                {sa.classifications.map((c) => (
                  <tr key={c.id}>
                    <td>{c.suspiciousActivityTypeId}</td>
                    <td>{c.suspiciousActivitySubtypeId}</td>
                    <td>{c.otherSuspiciousActivityTypeText ?? '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </>
      )}
    </>
  )
}

function IpCyberTab({ activity }: { activity: ActivityResponse }) {
  return (
    <>
      <h3>IP Addresses ({activity.ipAddresses.length})</h3>
      {activity.ipAddresses.length > 0 ? (
        <div className="table-frame" style={{ marginTop: '0.5rem' }}>
          <table>
            <thead>
              <tr>
                <th>Address</th>
                <th>Date</th>
                <th>Time</th>
              </tr>
            </thead>
            <tbody>
              {activity.ipAddresses.map((ip) => (
                <tr key={ip.id}>
                  <td>{ip.ipAddressText}</td>
                  <td>{ip.ipAddressDate ?? '—'}</td>
                  <td>{ip.ipAddressTimestamp ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>None</p>
      )}

      <h3 style={{ marginTop: '1.25rem' }}>Cyber Events ({activity.cyberEvents.length})</h3>
      {activity.cyberEvents.length > 0 ? (
        <div className="table-frame" style={{ marginTop: '0.5rem' }}>
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Value</th>
                <th>Date</th>
              </tr>
            </thead>
            <tbody>
              {activity.cyberEvents.map((ce) => (
                <tr key={ce.id}>
                  <td>{ce.cyberEventIndicatorsTypeCode}</td>
                  <td>{ce.eventValueText}</td>
                  <td>{ce.cyberEventDate ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>None</p>
      )}
    </>
  )
}

function AssetsTab({ activity }: { activity: ActivityResponse }) {
  return (
    <>
      <h3>Assets ({activity.assets.length})</h3>
      {activity.assets.length > 0 ? (
        <div className="table-frame" style={{ marginTop: '0.5rem' }}>
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Subtype</th>
                <th>Other</th>
              </tr>
            </thead>
            <tbody>
              {activity.assets.map((a) => (
                <tr key={a.id}>
                  <td>{a.assetTypeId}</td>
                  <td>{a.assetSubtypeId}</td>
                  <td>{a.otherAssetSubtypeText ?? '—'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>None</p>
      )}

      <h3 style={{ marginTop: '1.25rem' }}>Asset Attributes ({activity.assetAttributes.length})</h3>
      {activity.assetAttributes.length > 0 ? (
        <div className="table-frame" style={{ marginTop: '0.5rem' }}>
          <table>
            <thead>
              <tr>
                <th>Type</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody>
              {activity.assetAttributes.map((aa) => (
                <tr key={aa.id}>
                  <td>{aa.assetAttributeTypeId}</td>
                  <td>{aa.assetAttributeDescriptionText}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p style={{ color: 'var(--muted)', marginTop: '0.25rem' }}>None</p>
      )}
    </>
  )
}

function NarrativesTab({ activity }: { activity: ActivityResponse }) {
  if (activity.narratives.length === 0) {
    return (
      <div className="empty-state">
        <h3>No narratives</h3>
        <p>Use the wizard to add narrative text for this activity.</p>
      </div>
    )
  }

  return (
    <>
      {activity.narratives.map((n) => (
        <div key={n.id} className="card" style={{ marginBottom: '0.75rem' }}>
          <div className="card-header">
            <h3>Narrative #{n.narrativeSequenceNumber}</h3>
          </div>
          <p style={{ whiteSpace: 'pre-wrap' }}>{n.narrativeText}</p>
        </div>
      ))}
    </>
  )
}

function Detail({ label, value }: { label: string; value: string }) {
  return (
    <div className="detail-item">
      <span className="detail-label">{label}</span>
      <span className="detail-value">{value}</span>
    </div>
  )
}

function boolStr(val: boolean | null | undefined): string {
  if (val == null) return '—'
  return val ? 'Yes' : 'No'
}
