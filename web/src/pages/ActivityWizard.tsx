import { useCallback, useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import {
  addAsset,
  addAssetAttribute,
  addCyberEvent,
  addIpAddress,
  addNarrative,
  addParty,
  fetchActivity,
  patchActivityHeader,
  patchFilingType,
  patchSuspiciousActivity,
} from '../api'
import { StatusBadge } from '../components/StatusBadge'
import type {
  ActivityResponse,
  AssetAttributeRequest,
  AssetRequest,
  CyberEventRequest,
  IpAddressRequest,
  NarrativeRequest,
  PartyNameRequest,
  PartyRequest,
  PatchActivityHeaderRequest,
  PatchFilingTypeRequest,
  PatchSuspiciousActivityRequest,
} from '../types'

const STEPS = [
  { key: '1', label: 'Header' },
  { key: '2', label: 'Filing Type' },
  { key: '3', label: 'Parties' },
  { key: '4', label: 'Suspicious Activity' },
  { key: '5', label: 'IP Addresses' },
  { key: '6', label: 'Cyber Events' },
  { key: '7', label: 'Assets' },
  { key: '8', label: 'Narratives' },
] as const

export function ActivityWizard() {
  const { activityId, step } = useParams<{ activityId: string; step: string }>()
  const navigate = useNavigate()
  const [activity, setActivity] = useState<ActivityResponse | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const [success, setSuccess] = useState<string | null>(null)

  const currentStep = step ?? '1'

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

  function goStep(s: string) {
    setSuccess(null)
    setError(null)
    navigate(`/activities/${activityId}/wizard/${s}`)
  }

  async function saveWith<T>(fn: () => Promise<T>, msg: string) {
    setSaving(true)
    setError(null)
    setSuccess(null)
    try {
      await fn()
      setSuccess(msg)
      load()
    } catch (e) {
      setError(e instanceof Error ? e.message : 'Save failed')
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <p className="state-banner">Loading…</p>
  if (!activity) return <p className="state-banner error-banner">Activity not found</p>

  const id = activity.id

  return (
    <>
      <Link to={`/activities/${id}`} className="back-link">
        ← Back to Activity #{id}
      </Link>

      <div className="page-header">
        <p className="eyebrow">
          Activity #{id} · Wizard <StatusBadge status={activity.filingStatus} />
        </p>
        <h1>SAR Wizard</h1>
      </div>

      {/* Step nav */}
      <div className="tab-bar" style={{ marginBottom: '1.25rem' }}>
        {STEPS.map((s) => (
          <button
            key={s.key}
            className={currentStep === s.key ? 'active' : ''}
            onClick={() => goStep(s.key)}
          >
            {s.key}. {s.label}
          </button>
        ))}
      </div>

      {error && <p className="state-banner error-banner">{error}</p>}
      {success && <p className="state-banner success-banner">{success}</p>}

      <div className="card">
        {currentStep === '1' && (
          <HeaderStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => patchActivityHeader(id, data), 'Header updated')
            }
          />
        )}
        {currentStep === '2' && (
          <FilingTypeStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => patchFilingType(id, data), 'Filing type updated')
            }
          />
        )}
        {currentStep === '3' && (
          <PartiesStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => addParty(id, data), 'Party added')
            }
          />
        )}
        {currentStep === '4' && (
          <SuspiciousStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => patchSuspiciousActivity(id, data), 'Suspicious activity updated')
            }
          />
        )}
        {currentStep === '5' && (
          <IpStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => addIpAddress(id, data), 'IP address added')
            }
          />
        )}
        {currentStep === '6' && (
          <CyberStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => addCyberEvent(id, data), 'Cyber event added')
            }
          />
        )}
        {currentStep === '7' && (
          <AssetsStep
            activity={activity}
            saving={saving}
            onSaveAsset={(data) =>
              saveWith(() => addAsset(id, data), 'Asset added')
            }
            onSaveAttr={(data) =>
              saveWith(() => addAssetAttribute(id, data), 'Asset attribute added')
            }
          />
        )}
        {currentStep === '8' && (
          <NarrativeStep
            activity={activity}
            saving={saving}
            onSave={(data) =>
              saveWith(() => addNarrative(id, data), 'Narrative added')
            }
          />
        )}
      </div>

      {/* Step navigation */}
      <div className="form-actions" style={{ marginTop: '1rem' }}>
        {Number(currentStep) > 1 && (
          <button
            className="btn btn-secondary"
            onClick={() => goStep(String(Number(currentStep) - 1))}
          >
            ← Previous
          </button>
        )}
        {Number(currentStep) < STEPS.length && (
          <button
            className="btn btn-primary"
            onClick={() => goStep(String(Number(currentStep) + 1))}
          >
            Next →
          </button>
        )}
        {Number(currentStep) === STEPS.length && (
          <Link to={`/activities/${id}`} className="btn btn-primary">
            Finish
          </Link>
        )}
      </div>
    </>
  )
}

// ── Step 1: Header ────────────────────────────────────────────────────────────

function HeaderStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: PatchActivityHeaderRequest) => void
}) {
  const [filingDate, setFilingDate] = useState(activity.filingDate ?? '')
  const [priorDoc, setPriorDoc] = useState(activity.efilingPriorDocumentNumber ?? '')
  const [note, setNote] = useState(activity.filingInstitutionNoteToFincen ?? '')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      filingDate: filingDate || undefined,
      efilingPriorDocumentNumber: priorDoc || undefined,
      filingInstitutionNoteToFincen: note || undefined,
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Activity Header</h3>
      <div className="form-grid" style={{ marginTop: '0.75rem' }}>
        <div className="form-field">
          <label>Filing Date</label>
          <input type="date" value={filingDate} onChange={(e) => setFilingDate(e.target.value)} />
        </div>
        <div className="form-field">
          <label>Prior Document Number</label>
          <input
            type="text"
            maxLength={14}
            value={priorDoc}
            onChange={(e) => setPriorDoc(e.target.value)}
            placeholder="14 characters"
          />
        </div>
        <div className="form-field" style={{ gridColumn: '1 / -1' }}>
          <label>Note to FinCEN</label>
          <input
            type="text"
            maxLength={50}
            value={note}
            onChange={(e) => setNote(e.target.value)}
          />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save Header'}
        </button>
      </div>
    </form>
  )
}

// ── Step 2: Filing Type ───────────────────────────────────────────────────────

function FilingTypeStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: PatchFilingTypeRequest) => void
}) {
  const assoc = activity.activityAssociation
  const [initial, setInitial] = useState(assoc?.initialReportIndicator ?? false)
  const [corrects, setCorrects] = useState(assoc?.correctsAmendsPriorReport ?? false)
  const [continuing, setContinuing] = useState(assoc?.continuingActivityReport ?? false)
  const [joint, setJoint] = useState(assoc?.jointReportIndicator ?? false)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      initialReportIndicator: initial,
      correctsAmendsPriorReport: corrects,
      continuingActivityReport: continuing,
      jointReportIndicator: joint,
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Filing Type</h3>
      <div style={{ display: 'grid', gap: '0.5rem', marginTop: '0.75rem' }}>
        <label className="form-check">
          <input type="checkbox" checked={initial} onChange={(e) => setInitial(e.target.checked)} />
          Initial Report
        </label>
        <label className="form-check">
          <input
            type="checkbox"
            checked={corrects}
            onChange={(e) => setCorrects(e.target.checked)}
          />
          Corrects / Amends Prior Report
        </label>
        <label className="form-check">
          <input
            type="checkbox"
            checked={continuing}
            onChange={(e) => setContinuing(e.target.checked)}
          />
          Continuing Activity Report
        </label>
        <label className="form-check">
          <input type="checkbox" checked={joint} onChange={(e) => setJoint(e.target.checked)} />
          Joint Report
        </label>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save Filing Type'}
        </button>
      </div>
    </form>
  )
}

// ── Step 3: Add Party ─────────────────────────────────────────────────────────

function PartiesStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: PartyRequest) => void
}) {
  const [typeCode, setTypeCode] = useState(33)
  const [lastName, setLastName] = useState('')
  const [firstName, setFirstName] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    const names: PartyNameRequest[] = []
    if (lastName || firstName) {
      names.push({
        seqNum: 1,
        partyNameTypeCode: 'L',
        rawEntityIndividualLastName: lastName || undefined,
        rawIndividualFirstName: firstName || undefined,
      })
    }
    onSave({
      seqNum: activity.parties.length + 1,
      activityPartyTypeCode: typeCode,
      names,
      addresses: [],
      phones: [],
      identifications: [],
      orgClassifications: [],
      electronicAddresses: [],
      partyAssociations: [],
    })
    setLastName('')
    setFirstName('')
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add Party</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current parties: {activity.parties.length}
      </p>
      <div className="form-grid">
        <div className="form-field">
          <label>Party Type</label>
          <select value={typeCode} onChange={(e) => setTypeCode(Number(e.target.value))}>
            <option value={30}>Filing Institution</option>
            <option value={8}>Branch</option>
            <option value={23}>Contact for Assistance</option>
            <option value={33}>Subject</option>
            <option value={34}>Seller</option>
            <option value={35}>Payor</option>
            <option value={37}>Financial Institution</option>
            <option value={46}>Person on Behalf of Subject</option>
          </select>
        </div>
        <div className="form-field">
          <label>Last Name / Entity Name</label>
          <input
            type="text"
            value={lastName}
            onChange={(e) => setLastName(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label>First Name</label>
          <input
            type="text"
            value={firstName}
            onChange={(e) => setFirstName(e.target.value)}
          />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Adding…' : 'Add Party'}
        </button>
      </div>
    </form>
  )
}

// ── Step 4: Suspicious Activity ───────────────────────────────────────────────

function SuspiciousStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: PatchSuspiciousActivityRequest) => void
}) {
  const sa = activity.suspiciousActivity
  const [totalAmount, setTotalAmount] = useState(sa?.totalSuspiciousAmount?.toString() ?? '')
  const [fromDate, setFromDate] = useState(sa?.suspiciousActivityFromDate ?? '')
  const [toDate, setToDate] = useState(sa?.suspiciousActivityToDate ?? '')
  const [amountUnknown, setAmountUnknown] = useState(sa?.amountUnknown ?? false)

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      totalSuspiciousAmount: totalAmount ? Number(totalAmount) : undefined,
      suspiciousActivityFromDate: fromDate || undefined,
      suspiciousActivityToDate: toDate || undefined,
      amountUnknown,
    })
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Suspicious Activity</h3>
      <div className="form-grid" style={{ marginTop: '0.75rem' }}>
        <div className="form-field">
          <label>Total Suspicious Amount</label>
          <input
            type="number"
            step="0.01"
            value={totalAmount}
            onChange={(e) => setTotalAmount(e.target.value)}
          />
        </div>
        <div className="form-field">
          <label>From Date</label>
          <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} />
        </div>
        <div className="form-field">
          <label>To Date</label>
          <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
        </div>
        <label className="form-check">
          <input
            type="checkbox"
            checked={amountUnknown}
            onChange={(e) => setAmountUnknown(e.target.checked)}
          />
          Amount Unknown
        </label>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Saving…' : 'Save Suspicious Activity'}
        </button>
      </div>
    </form>
  )
}

// ── Step 5: IP Addresses ──────────────────────────────────────────────────────

function IpStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: IpAddressRequest) => void
}) {
  const [ipText, setIpText] = useState('')
  const [ipDate, setIpDate] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      seqNum: activity.ipAddresses.length + 1,
      ipAddressText: ipText,
      ipAddressDate: ipDate || undefined,
    })
    setIpText('')
    setIpDate('')
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add IP Address</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current: {activity.ipAddresses.length} IP address(es)
      </p>
      <div className="form-grid">
        <div className="form-field">
          <label>IP Address</label>
          <input
            type="text"
            value={ipText}
            onChange={(e) => setIpText(e.target.value)}
            required
            maxLength={45}
            placeholder="192.168.1.1"
          />
        </div>
        <div className="form-field">
          <label>Date (optional)</label>
          <input type="date" value={ipDate} onChange={(e) => setIpDate(e.target.value)} />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Adding…' : 'Add IP Address'}
        </button>
      </div>
    </form>
  )
}

// ── Step 6: Cyber Events ──────────────────────────────────────────────────────

function CyberStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: CyberEventRequest) => void
}) {
  const [typeCode, setTypeCode] = useState(1)
  const [value, setValue] = useState('')
  const [date, setDate] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      seqNum: activity.cyberEvents.length + 1,
      cyberEventIndicatorsTypeCode: typeCode,
      eventValueText: value,
      cyberEventDate: date || undefined,
    })
    setValue('')
    setDate('')
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add Cyber Event</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current: {activity.cyberEvents.length} event(s)
      </p>
      <div className="form-grid">
        <div className="form-field">
          <label>Type Code</label>
          <input
            type="number"
            value={typeCode}
            onChange={(e) => setTypeCode(Number(e.target.value))}
            required
          />
        </div>
        <div className="form-field">
          <label>Value</label>
          <input
            type="text"
            value={value}
            onChange={(e) => setValue(e.target.value)}
            required
            maxLength={4000}
          />
        </div>
        <div className="form-field">
          <label>Date (optional)</label>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Adding…' : 'Add Cyber Event'}
        </button>
      </div>
    </form>
  )
}

// ── Step 7: Assets ────────────────────────────────────────────────────────────

function AssetsStep({
  activity,
  saving,
  onSaveAsset,
  onSaveAttr,
}: {
  activity: ActivityResponse
  saving: boolean
  onSaveAsset: (data: AssetRequest) => void
  onSaveAttr: (data: AssetAttributeRequest) => void
}) {
  const [assetTypeId, setAssetTypeId] = useState(1)
  const [assetSubtypeId, setAssetSubtypeId] = useState(1)
  const [attrTypeId, setAttrTypeId] = useState(1)
  const [attrDesc, setAttrDesc] = useState('')
  const [mode, setMode] = useState<'asset' | 'attr'>('asset')

  function handleAsset(e: React.FormEvent) {
    e.preventDefault()
    onSaveAsset({
      seqNum: activity.assets.length + 1,
      assetTypeId,
      assetSubtypeId,
    })
  }

  function handleAttr(e: React.FormEvent) {
    e.preventDefault()
    onSaveAttr({
      seqNum: activity.assetAttributes.length + 1,
      assetAttributeTypeId: attrTypeId,
      assetAttributeDescriptionText: attrDesc,
    })
    setAttrDesc('')
  }

  return (
    <>
      <h3>Assets &amp; Attributes</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Assets: {activity.assets.length} · Attributes: {activity.assetAttributes.length}
      </p>
      <div className="tab-bar" style={{ marginBottom: '0.75rem' }}>
        <button className={mode === 'asset' ? 'active' : ''} onClick={() => setMode('asset')}>
          Add Asset
        </button>
        <button className={mode === 'attr' ? 'active' : ''} onClick={() => setMode('attr')}>
          Add Attribute
        </button>
      </div>

      {mode === 'asset' ? (
        <form onSubmit={handleAsset}>
          <div className="form-grid">
            <div className="form-field">
              <label>Asset Type ID</label>
              <input
                type="number"
                value={assetTypeId}
                onChange={(e) => setAssetTypeId(Number(e.target.value))}
                required
              />
            </div>
            <div className="form-field">
              <label>Asset Subtype ID</label>
              <input
                type="number"
                value={assetSubtypeId}
                onChange={(e) => setAssetSubtypeId(Number(e.target.value))}
                required
              />
            </div>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Adding…' : 'Add Asset'}
            </button>
          </div>
        </form>
      ) : (
        <form onSubmit={handleAttr}>
          <div className="form-grid">
            <div className="form-field">
              <label>Attribute Type ID</label>
              <input
                type="number"
                value={attrTypeId}
                onChange={(e) => setAttrTypeId(Number(e.target.value))}
                required
              />
            </div>
            <div className="form-field">
              <label>Description</label>
              <input
                type="text"
                value={attrDesc}
                onChange={(e) => setAttrDesc(e.target.value)}
                required
                maxLength={50}
              />
            </div>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Adding…' : 'Add Attribute'}
            </button>
          </div>
        </form>
      )}
    </>
  )
}

// ── Step 8: Narratives ────────────────────────────────────────────────────────

function NarrativeStep({
  activity,
  saving,
  onSave,
}: {
  activity: ActivityResponse
  saving: boolean
  onSave: (data: NarrativeRequest) => void
}) {
  const [seqNum, setSeqNum] = useState(1)
  const [text, setText] = useState('')

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()
    onSave({
      seqNum: activity.narratives.length + 1,
      narrativeSequenceNumber: seqNum,
      narrativeText: text,
    })
    setText('')
    setSeqNum((s) => Math.min(s + 1, 5))
  }

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add Narrative</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current: {activity.narratives.length} narrative(s) · Max sequence: 5
      </p>
      <div className="form-grid">
        <div className="form-field">
          <label>Sequence Number (1-5)</label>
          <input
            type="number"
            min={1}
            max={5}
            value={seqNum}
            onChange={(e) => setSeqNum(Number(e.target.value))}
            required
          />
        </div>
      </div>
      <div className="form-field" style={{ marginTop: '0.75rem' }}>
        <label>Narrative Text</label>
        <textarea
          value={text}
          onChange={(e) => setText(e.target.value)}
          required
          maxLength={4000}
          rows={8}
          placeholder="Enter narrative text describing the suspicious activity…"
        />
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>
          {saving ? 'Adding…' : 'Add Narrative'}
        </button>
      </div>
    </form>
  )
}
