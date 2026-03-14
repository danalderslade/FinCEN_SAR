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
import {
  ASSET_ATTRIBUTE_TYPES,
  ASSET_SUBTYPES,
  ASSET_TYPES,
  CYBER_EVENT_INDICATOR_CODES,
  FIELD_LIMITS,
  ORGANIZATION_SUBTYPES,
  ORGANIZATION_TYPE_CODES,
  PARTY_ID_TYPE_CODES,
  PARTY_NAME_TYPE_CODES,
  PARTY_TYPE_REQUIREMENTS,
  PRIMARY_REGULATOR_CODES,
  SAR_PARTY_TYPES,
  SUSPICIOUS_ACTIVITY_SUBTYPES,
  SUSPICIOUS_ACTIVITY_TYPES,
  US_STATE_CODES,
} from '../fincenReference'
import { StatusBadge } from '../components/StatusBadge'
import { validateActivity, validateParty, validateSuspiciousActivity } from '../validation'
import type {
  ActivityResponse,
  AssetAttributeRequest,
  AssetRequest,
  CyberEventRequest,
  IpAddressRequest,
  NarrativeRequest,
  OrgClassificationRequest,
  PartyAddressRequest,
  PartyAssociationRequest,
  PartyIdentificationRequest,
  PartyNameRequest,
  PartyRequest,
  PatchActivityHeaderRequest,
  PatchFilingTypeRequest,
  PatchSuspiciousActivityRequest,
} from '../types'

// ── Helpers ───────────────────────────────────────────────────────────────────

function Req() {
  return <span className="required-star" title="Required by FinCEN">*</span>
}

function FieldErr({ msg }: { msg?: string }) {
  return msg ? <div className="field-error">{msg}</div> : null
}

function Hint({ text }: { text: string }) {
  return <div className="field-hint">{text}</div>
}

function Counter({ value, max }: { value: string; max: number }) {
  const len = value?.length ?? 0
  return (
    <div className={`field-counter${len > max ? ' over-limit' : ''}`}>
      {len}/{max}
    </div>
  )
}

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

  useEffect(() => { load() }, [load])

  function goStep(s: string) {
    setSuccess(null)
    setError(null)
    navigate(`/activities/${activityId}/wizard/${s}`)
  }

  async function saveWith<T>(fn: () => Promise<T>, msg: string) {
    setSaving(true); setError(null); setSuccess(null)
    try { await fn(); setSuccess(msg); load() }
    catch (e) { setError(e instanceof Error ? e.message : 'Save failed') }
    finally { setSaving(false) }
  }

  if (loading) return <p className="state-banner">Loading…</p>
  if (!activity) return <p className="state-banner error-banner">Activity not found</p>

  const id = activity.id

  // Activity-level validation summary
  const actVal = validateActivity({
    filingDate: activity.filingDate,
    activityAssociation: activity.activityAssociation ? { ...activity.activityAssociation, seqNum: 1 } : undefined,
    parties: activity.parties.map((p) => ({ ...p, lossToFinancialAmount: p.lossToFinancialAmount ?? undefined, names: p.names.map((n) => ({ ...n })), addresses: p.addresses.map((a) => ({ ...a })), phones: p.phones.map((ph) => ({ ...ph })), identifications: p.identifications.map((i) => ({ ...i })), orgClassifications: p.orgClassifications.map((o) => ({ ...o })), electronicAddresses: p.electronicAddresses.map((e) => ({ ...e })), partyAssociations: p.partyAssociations.map((a) => ({ ...a })) })) as unknown as PartyRequest[],
    suspiciousActivity: activity.suspiciousActivity ? { seqNum: 1, amountUnknown: activity.suspiciousActivity.amountUnknown ?? undefined, noAmountInvolved: activity.suspiciousActivity.noAmountInvolved ?? undefined, totalSuspiciousAmount: activity.suspiciousActivity.totalSuspiciousAmount ?? undefined, suspiciousActivityFromDate: activity.suspiciousActivity.suspiciousActivityFromDate ?? '', suspiciousActivityToDate: activity.suspiciousActivity.suspiciousActivityToDate ?? undefined, cumulativeTotalViolationAmount: activity.suspiciousActivity.cumulativeTotalViolationAmount ?? undefined, classifications: activity.suspiciousActivity.classifications?.map((c, i) => ({ seqNum: i + 1, suspiciousActivityTypeId: c.suspiciousActivityTypeId, suspiciousActivitySubtypeId: c.suspiciousActivitySubtypeId })) ?? [] } : undefined,
    narratives: activity.narratives.map((n) => ({ ...n, seqNum: 1 })),
    seqNum: activity.seqNum,
    ipAddresses: [],
    cyberEvents: [],
    assets: [],
    assetAttributes: [],
  })

  return (
    <>
      <Link to={`/activities/${id}`} className="back-link">← Back to Activity #{id}</Link>

      <div className="page-header">
        <p className="eyebrow">
          Activity #{id} · Wizard <StatusBadge status={activity.filingStatus} />
        </p>
        <h1>SAR Wizard</h1>
      </div>

      {/* Validation summary bar */}
      {!actVal.valid && (
        <div className="validation-summary has-errors">
          <h4>⚠ FinCEN Validation Issues ({actVal.errors.length})</h4>
          <ul>{actVal.errors.slice(0, 5).map((e, i) => (
            <li key={i} className="val-error">{e.field}: {e.message}</li>
          ))}</ul>
          {actVal.errors.length > 5 && <p style={{ fontSize: '0.75rem', color: 'var(--muted)' }}>…and {actVal.errors.length - 5} more</p>}
        </div>
      )}

      {/* Step nav */}
      <div className="tab-bar" style={{ marginBottom: '1.25rem' }}>
        {STEPS.map((s) => (
          <button key={s.key} className={currentStep === s.key ? 'active' : ''} onClick={() => goStep(s.key)}>
            {s.key}. {s.label}
          </button>
        ))}
      </div>

      {error && <p className="state-banner error-banner">{error}</p>}
      {success && <p className="state-banner success-banner">{success}</p>}

      <div className="card">
        {currentStep === '1' && <HeaderStep activity={activity} saving={saving} onSave={(data) => saveWith(() => patchActivityHeader(id, data), 'Header updated')} />}
        {currentStep === '2' && <FilingTypeStep activity={activity} saving={saving} onSave={(data) => saveWith(() => patchFilingType(id, data), 'Filing type updated')} />}
        {currentStep === '3' && <PartiesStep activity={activity} saving={saving} onSave={(data) => saveWith(() => addParty(id, data), 'Party added')} />}
        {currentStep === '4' && <SuspiciousStep activity={activity} saving={saving} onSave={(data) => saveWith(() => patchSuspiciousActivity(id, data), 'Suspicious activity updated')} />}
        {currentStep === '5' && <IpStep activity={activity} saving={saving} onSave={(data) => saveWith(() => addIpAddress(id, data), 'IP address added')} />}
        {currentStep === '6' && <CyberStep activity={activity} saving={saving} onSave={(data) => saveWith(() => addCyberEvent(id, data), 'Cyber event added')} />}
        {currentStep === '7' && <AssetsStep activity={activity} saving={saving} onSaveAsset={(data) => saveWith(() => addAsset(id, data), 'Asset added')} onSaveAttr={(data) => saveWith(() => addAssetAttribute(id, data), 'Asset attribute added')} />}
        {currentStep === '8' && <NarrativeStep activity={activity} saving={saving} onSave={(data) => saveWith(() => addNarrative(id, data), 'Narrative added')} />}
      </div>

      <div className="form-actions" style={{ marginTop: '1rem' }}>
        {Number(currentStep) > 1 && <button className="btn btn-secondary" onClick={() => goStep(String(Number(currentStep) - 1))}>← Previous</button>}
        {Number(currentStep) < STEPS.length && <button className="btn btn-primary" onClick={() => goStep(String(Number(currentStep) + 1))}>Next →</button>}
        {Number(currentStep) === STEPS.length && <Link to={`/activities/${id}`} className="btn btn-primary">Finish</Link>}
      </div>
    </>
  )
}

// ── Step 1: Header ────────────────────────────────────────────────────────────

function HeaderStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: PatchActivityHeaderRequest) => void }) {
  const [filingDate, setFilingDate] = useState(activity.filingDate ?? '')
  const [priorDoc, setPriorDoc] = useState(activity.efilingPriorDocumentNumber ?? '')
  const [note, setNote] = useState(activity.filingInstitutionNoteToFincen ?? '')

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSave({ filingDate: filingDate || undefined, efilingPriorDocumentNumber: priorDoc || undefined, filingInstitutionNoteToFincen: note || undefined }) }}>
      <h3>Activity Header</h3>
      <div className="form-grid" style={{ marginTop: '0.75rem' }}>
        <div className={`form-field${!filingDate ? ' has-error' : ''}`}>
          <label>Filing Date <Req /></label>
          <input type="date" value={filingDate} onChange={(e) => setFilingDate(e.target.value)} required />
          <FieldErr msg={!filingDate ? 'Filing date is required' : undefined} />
        </div>
        <div className="form-field">
          <label>Prior Document Number</label>
          <input type="text" maxLength={FIELD_LIMITS.efilingPriorDocumentNumber} value={priorDoc} onChange={(e) => setPriorDoc(e.target.value)} placeholder="14-digit BSA ID" />
          <Hint text={`Exactly ${FIELD_LIMITS.efilingPriorDocumentNumber} characters. Required for Corrects/Amends or Continuing filings.`} />
          <Counter value={priorDoc} max={FIELD_LIMITS.efilingPriorDocumentNumber} />
        </div>
        <div className="form-field" style={{ gridColumn: '1 / -1' }}>
          <label>Note to FinCEN</label>
          <input type="text" maxLength={FIELD_LIMITS.filingInstitutionNoteToFincen} value={note} onChange={(e) => setNote(e.target.value)} />
          <Counter value={note} max={FIELD_LIMITS.filingInstitutionNoteToFincen} />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Header'}</button>
      </div>
    </form>
  )
}

// ── Step 2: Filing Type ───────────────────────────────────────────────────────

function FilingTypeStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: PatchFilingTypeRequest) => void }) {
  const assoc = activity.activityAssociation
  const [initial, setInitial] = useState(assoc?.initialReportIndicator ?? false)
  const [corrects, setCorrects] = useState(assoc?.correctsAmendsPriorReport ?? false)
  const [continuing, setContinuing] = useState(assoc?.continuingActivityReport ?? false)
  const [joint, setJoint] = useState(assoc?.jointReportIndicator ?? false)

  const count = [initial, corrects, continuing].filter(Boolean).length
  const filingError = count === 0 ? 'Exactly one filing type must be selected' : count > 1 ? 'Only one filing type should be selected' : undefined

  return (
    <form onSubmit={(e) => { e.preventDefault(); onSave({ initialReportIndicator: initial, correctsAmendsPriorReport: corrects, continuingActivityReport: continuing, jointReportIndicator: joint }) }}>
      <h3>Filing Type <Req /></h3>
      <Hint text="Select exactly one primary filing type. Joint Report is an additional flag." />
      {filingError && <div className="validation-summary has-errors" style={{ marginTop: '0.5rem' }}><p className="val-error">{filingError}</p></div>}
      <div style={{ display: 'grid', gap: '0.5rem', marginTop: '0.75rem' }}>
        <label className="form-check"><input type="radio" name="filingType" checked={initial} onChange={() => { setInitial(true); setCorrects(false); setContinuing(false) }} /> Initial Report</label>
        <label className="form-check"><input type="radio" name="filingType" checked={corrects} onChange={() => { setInitial(false); setCorrects(true); setContinuing(false) }} /> Corrects / Amends Prior Report</label>
        <label className="form-check"><input type="radio" name="filingType" checked={continuing} onChange={() => { setInitial(false); setCorrects(false); setContinuing(true) }} /> Continuing Activity Report</label>
        <hr style={{ border: 'none', borderTop: '1px solid var(--border)', margin: '0.25rem 0' }} />
        <label className="form-check"><input type="checkbox" checked={joint} onChange={(e) => setJoint(e.target.checked)} /> Joint Report</label>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Filing Type'}</button>
      </div>
    </form>
  )
}

// ── Step 3: Add Party (Comprehensive) ─────────────────────────────────────────

function PartiesStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: PartyRequest) => void }) {
  const [typeCode, setTypeCode] = useState(33)
  const reqs = PARTY_TYPE_REQUIREMENTS[typeCode]

  // Name
  const [nameTypeCode, setNameTypeCode] = useState('L')
  const [lastName, setLastName] = useState('')
  const [firstName, setFirstName] = useState('')
  const [middleName, setMiddleName] = useState('')
  const [suffix, setSuffix] = useState('')
  const [fullName, setFullName] = useState('')
  const [isEntity, setIsEntity] = useState(false)

  // Address
  const [street, setStreet] = useState('')
  const [city, setCity] = useState('')
  const [stateCode, setStateCode] = useState('')
  const [zip, setZip] = useState('')
  const [country, setCountry] = useState('US')

  // Identification
  const [idTypeCode, setIdTypeCode] = useState(1)
  const [idNumber, setIdNumber] = useState('')

  // Institution fields
  const [regulatorCode, setRegulatorCode] = useState<number | ''>('')
  const [orgTypeCode, setOrgTypeCode] = useState<number | ''>('')
  const [orgSubtypeCode, setOrgSubtypeCode] = useState<number | ''>('')
  const [lossAmount, setLossAmount] = useState('')

  // Subject fields
  const [allInfoUnavailable, setAllInfoUnavailable] = useState(false)
  const [birthDate, setBirthDate] = useState('')
  const [gender, setGender] = useState<'' | 'M' | 'F' | 'U'>('')
  const [admissionYes, setAdmissionYes] = useState(false)
  const [admissionNo, setAdmissionNo] = useState(false)
  const [noKnownAccount, setNoKnownAccount] = useState(false)

  // Association (for subjects)
  const [assocTin, setAssocTin] = useState('')
  const [relationship, setRelationship] = useState('customer')

  // Validation
  const [errors, setErrors] = useState<string[]>([])

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault()

    const names: PartyNameRequest[] = []
    if (isEntity && fullName) {
      names.push({ seqNum: 1, partyNameTypeCode: nameTypeCode, rawPartyFullName: fullName })
    } else if (lastName || firstName) {
      names.push({
        seqNum: 1, partyNameTypeCode: nameTypeCode,
        rawEntityIndividualLastName: lastName || undefined,
        rawIndividualFirstName: firstName || undefined,
        rawIndividualMiddleName: middleName || undefined,
        rawIndividualNameSuffixText: suffix || undefined,
      })
    }

    const addresses: PartyAddressRequest[] = []
    if (street || city || stateCode || zip) {
      addresses.push({ seqNum: 1, rawStreetAddress1: street || undefined, rawCity: city || undefined, rawStateCode: stateCode || undefined, rawZipCode: zip || undefined, rawCountryCode: country || undefined })
    }

    const identifications: PartyIdentificationRequest[] = []
    if (idNumber) {
      identifications.push({ seqNum: 1, partyIdentificationTypeCode: idTypeCode, partyIdentificationNumber: idNumber })
    }

    const orgClassifications: OrgClassificationRequest[] = []
    if (orgTypeCode !== '') {
      orgClassifications.push({ seqNum: 1, organizationTypeId: Number(orgTypeCode), organizationSubtypeId: orgSubtypeCode !== '' ? Number(orgSubtypeCode) : undefined })
    }

    const partyAssociations: PartyAssociationRequest[] = []
    if (typeCode === 33 && (assocTin || relationship)) {
      const assoc: PartyAssociationRequest = {
        seqNum: 1,
        subjectRelationshipInstitutionTin: assocTin || undefined,
        customerIndicator: relationship === 'customer' ? true : undefined,
        employeeIndicator: relationship === 'employee' ? true : undefined,
        officerIndicator: relationship === 'officer' ? true : undefined,
        directorIndicator: relationship === 'director' ? true : undefined,
        ownerShareholderIndicator: relationship === 'owner' ? true : undefined,
        noRelationshipToInstitution: relationship === 'none' ? true : undefined,
        branchParties: [],
      }
      partyAssociations.push(assoc)
    }

    const party: PartyRequest = {
      seqNum: activity.parties.length + 1,
      activityPartyTypeCode: typeCode,
      partyAsEntityOrganization: isEntity || undefined,
      primaryRegulatorTypeCode: regulatorCode !== '' ? Number(regulatorCode) : undefined,
      lossToFinancialAmount: lossAmount ? Number(lossAmount) : undefined,
      individualBirthDate: birthDate || undefined,
      maleGenderIndicator: gender === 'M' || undefined,
      femaleGenderIndicator: gender === 'F' || undefined,
      unknownGenderIndicator: gender === 'U' || undefined,
      admissionConfessionYes: admissionYes || undefined,
      admissionConfessionNo: admissionNo || undefined,
      allCriticalSubjectInfoUnavailable: allInfoUnavailable || undefined,
      noKnownAccountInvolved: noKnownAccount || undefined,
      names,
      addresses,
      phones: [],
      identifications,
      orgClassifications,
      electronicAddresses: [],
      partyAssociations,
    }

    // Validate
    const val = validateParty(party)
    if (!val.valid) {
      setErrors(val.errors.map((e) => `${e.field}: ${e.message}`))
      return
    }
    setErrors([])
    onSave(party)

    // Reset fields
    setLastName(''); setFirstName(''); setMiddleName(''); setSuffix(''); setFullName('')
    setStreet(''); setCity(''); setStateCode(''); setZip(''); setCountry('US')
    setIdNumber(''); setLossAmount(''); setBirthDate(''); setGender('')
    setAssocTin(''); setAllInfoUnavailable(false)
    setAdmissionYes(false); setAdmissionNo(false); setNoKnownAccount(false)
  }

  const typeInfo = SAR_PARTY_TYPES.find((t) => t.code === typeCode)

  return (
    <form onSubmit={handleSubmit}>
      <h3>Add Party</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current parties: {activity.parties.length}
        {activity.parties.length > 0 && (
          <span> — {activity.parties.map((p) => `${SAR_PARTY_TYPES.find(t => t.code === p.activityPartyTypeCode)?.label || `Type ${p.activityPartyTypeCode}`}`).join(', ')}</span>
        )}
      </p>

      {errors.length > 0 && (
        <div className="validation-summary has-errors">
          <h4>Validation Errors</h4>
          <ul>{errors.map((e, i) => <li key={i} className="val-error">{e}</li>)}</ul>
        </div>
      )}

      {/* Party Type Selection */}
      <div className="form-field">
        <label>Party Type <Req /></label>
        <select value={typeCode} onChange={(e) => { setTypeCode(Number(e.target.value)); setIsEntity(PARTY_TYPE_REQUIREMENTS[Number(e.target.value)]?.isEntity ?? false) }}>
          {SAR_PARTY_TYPES.map((t) => <option key={t.code} value={t.code}>{t.code} — {t.label}</option>)}
        </select>
        {typeInfo && <Hint text={typeInfo.description} />}
      </div>

      {/* === Name Section === */}
      <div className="form-section">
        <h4>Name {reqs?.requiresName && <Req />}</h4>
        <div className="form-grid">
          <div className="form-field">
            <label>Name Type <Req /></label>
            <select value={nameTypeCode} onChange={(e) => setNameTypeCode(e.target.value)}>
              {PARTY_NAME_TYPE_CODES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label><input type="checkbox" checked={isEntity} onChange={(e) => setIsEntity(e.target.checked)} style={{ marginRight: '0.35rem' }} />Entity / Organization</label>
          </div>
        </div>
        {isEntity ? (
          <div className="form-field">
            <label>Full Entity Name {reqs?.requiresName && <Req />}</label>
            <input type="text" maxLength={FIELD_LIMITS.rawPartyFullName} value={fullName} onChange={(e) => setFullName(e.target.value)} />
            <Counter value={fullName} max={FIELD_LIMITS.rawPartyFullName} />
          </div>
        ) : (
          <div className="form-grid">
            <div className="form-field">
              <label>Last Name {reqs?.requiresName && <Req />}</label>
              <input type="text" maxLength={FIELD_LIMITS.rawEntityIndividualLastName} value={lastName} onChange={(e) => setLastName(e.target.value)} />
              <Counter value={lastName} max={FIELD_LIMITS.rawEntityIndividualLastName} />
            </div>
            <div className="form-field">
              <label>First Name</label>
              <input type="text" maxLength={FIELD_LIMITS.rawIndividualFirstName} value={firstName} onChange={(e) => setFirstName(e.target.value)} />
              <Counter value={firstName} max={FIELD_LIMITS.rawIndividualFirstName} />
            </div>
            <div className="form-field">
              <label>Middle Name</label>
              <input type="text" maxLength={FIELD_LIMITS.rawIndividualMiddleName} value={middleName} onChange={(e) => setMiddleName(e.target.value)} />
              <Counter value={middleName} max={FIELD_LIMITS.rawIndividualMiddleName} />
            </div>
            <div className="form-field">
              <label>Suffix</label>
              <input type="text" maxLength={FIELD_LIMITS.rawIndividualNameSuffixText} value={suffix} onChange={(e) => setSuffix(e.target.value)} placeholder="Jr., Sr., III" />
            </div>
          </div>
        )}
        {typeCode === 33 && (
          <label className="form-check" style={{ marginTop: '0.5rem' }}>
            <input type="checkbox" checked={allInfoUnavailable} onChange={(e) => setAllInfoUnavailable(e.target.checked)} />
            All critical subject information unavailable (Item 3b)
          </label>
        )}
      </div>

      {/* === Address Section === */}
      {(reqs?.requiresAddress || street || city) && (
        <div className="form-section">
          <h4>Address {reqs?.requiresAddress && <Req />}</h4>
          <div className="form-grid">
            <div className="form-field" style={{ gridColumn: '1 / -1' }}>
              <label>Street Address</label>
              <input type="text" maxLength={FIELD_LIMITS.rawStreetAddress1} value={street} onChange={(e) => setStreet(e.target.value)} />
              <Counter value={street} max={FIELD_LIMITS.rawStreetAddress1} />
            </div>
            <div className="form-field">
              <label>City</label>
              <input type="text" maxLength={FIELD_LIMITS.rawCity} value={city} onChange={(e) => setCity(e.target.value)} />
            </div>
            <div className="form-field">
              <label>State</label>
              <select value={stateCode} onChange={(e) => setStateCode(e.target.value)}>
                <option value="">— Select —</option>
                {US_STATE_CODES.map((s) => <option key={s.code} value={s.code}>{s.code} — {s.label}</option>)}
              </select>
            </div>
            <div className="form-field">
              <label>ZIP Code</label>
              <input type="text" maxLength={FIELD_LIMITS.rawZipCode} value={zip} onChange={(e) => setZip(e.target.value.replace(/[^0-9]/g, ''))} placeholder="5 or 9 digits" />
              <Hint text="5-digit or 9-digit (no dash)" />
            </div>
            <div className="form-field">
              <label>Country</label>
              <input type="text" maxLength={2} value={country} onChange={(e) => setCountry(e.target.value.toUpperCase())} placeholder="US" />
              <Hint text="2-letter ISO code" />
            </div>
          </div>
        </div>
      )}

      {/* === Identification Section === */}
      {(reqs?.requiresIdentification || idNumber) && (
        <div className="form-section">
          <h4>Identification {reqs?.requiresIdentification && <Req />}</h4>
          <div className="form-grid">
            <div className="form-field">
              <label>ID Type</label>
              <select value={idTypeCode} onChange={(e) => setIdTypeCode(Number(e.target.value))}>
                {PARTY_ID_TYPE_CODES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
              </select>
            </div>
            <div className="form-field">
              <label>ID Number</label>
              <input type="text" maxLength={PARTY_ID_TYPE_CODES.find(t => t.code === idTypeCode)?.maxLength ?? FIELD_LIMITS.partyIdentificationNumber} value={idNumber} onChange={(e) => setIdNumber(e.target.value)} />
              <Hint text={`Max ${PARTY_ID_TYPE_CODES.find(t => t.code === idTypeCode)?.maxLength ?? 25} characters`} />
            </div>
          </div>
        </div>
      )}

      {/* === Institution Fields (Filing Institution, FI Where Activity Occurred) === */}
      {(reqs?.requiresRegulator || reqs?.requiresOrgClassification) && (
        <div className="form-section">
          <h4>Institution Details</h4>
          <div className="form-grid">
            {reqs.requiresRegulator && (
              <div className={`form-field${regulatorCode === '' ? ' has-error' : ''}`}>
                <label>Primary Regulator <Req /></label>
                <select value={regulatorCode} onChange={(e) => setRegulatorCode(e.target.value ? Number(e.target.value) : '')}>
                  <option value="">— Select —</option>
                  {PRIMARY_REGULATOR_CODES.map((r) => <option key={r.code} value={r.code}>{r.label}</option>)}
                </select>
                <FieldErr msg={regulatorCode === '' ? 'Primary regulator is required' : undefined} />
              </div>
            )}
            {reqs.requiresOrgClassification && (
              <>
                <div className={`form-field${orgTypeCode === '' ? ' has-error' : ''}`}>
                  <label>Organization Type <Req /></label>
                  <select value={orgTypeCode} onChange={(e) => { setOrgTypeCode(e.target.value ? Number(e.target.value) : ''); setOrgSubtypeCode('') }}>
                    <option value="">— Select —</option>
                    {ORGANIZATION_TYPE_CODES.map((o) => <option key={o.code} value={o.code}>{o.label}</option>)}
                  </select>
                  <FieldErr msg={orgTypeCode === '' ? 'Organization type is required' : undefined} />
                </div>
                {orgTypeCode !== '' && ORGANIZATION_SUBTYPES[Number(orgTypeCode)] && (
                  <div className="form-field">
                    <label>Organization Subtype</label>
                    <select value={orgSubtypeCode} onChange={(e) => setOrgSubtypeCode(e.target.value ? Number(e.target.value) : '')}>
                      <option value="">— None —</option>
                      {(ORGANIZATION_SUBTYPES[Number(orgTypeCode)] ?? []).map((s) => <option key={s.code} value={s.code}>{s.label}</option>)}
                    </select>
                  </div>
                )}
              </>
            )}
            <div className="form-field">
              <label>Loss to Financial Institution ($)</label>
              <input type="number" min="0" step="1" value={lossAmount} onChange={(e) => setLossAmount(e.target.value)} placeholder="Whole dollars only" />
              <Hint text="15-digit max, no cents" />
            </div>
          </div>
        </div>
      )}

      {/* === Subject-Specific Fields === */}
      {typeCode === 33 && (
        <div className="form-section">
          <h4>Subject Details (Part I)</h4>
          <div className="form-grid">
            <div className="form-field">
              <label>Date of Birth</label>
              <input type="date" value={birthDate} onChange={(e) => setBirthDate(e.target.value)} />
            </div>
            <div className="form-field">
              <label>Gender</label>
              <select value={gender} onChange={(e) => setGender(e.target.value as typeof gender)}>
                <option value="">— Unknown —</option>
                <option value="M">Male</option>
                <option value="F">Female</option>
                <option value="U">Unknown</option>
              </select>
            </div>
            <div className="form-field">
              <label>Admission/Confession</label>
              <div style={{ display: 'flex', gap: '1rem' }}>
                <label className="form-check"><input type="radio" name="admission" checked={admissionYes} onChange={() => { setAdmissionYes(true); setAdmissionNo(false) }} /> Yes</label>
                <label className="form-check"><input type="radio" name="admission" checked={admissionNo} onChange={() => { setAdmissionYes(false); setAdmissionNo(true) }} /> No</label>
                <label className="form-check"><input type="radio" name="admission" checked={!admissionYes && !admissionNo} onChange={() => { setAdmissionYes(false); setAdmissionNo(false) }} /> N/A</label>
              </div>
            </div>
            <div className="form-field">
              <label className="form-check"><input type="checkbox" checked={noKnownAccount} onChange={(e) => setNoKnownAccount(e.target.checked)} /> No known account involved (Item 27a)</label>
            </div>
          </div>

          {/* Association (required for subjects) */}
          <div className="form-section">
            <h4>Subject Relationship to Institution <Req /></h4>
            <div className="form-grid">
              <div className="form-field">
                <label>Institution TIN</label>
                <input type="text" maxLength={FIELD_LIMITS.subjectRelationshipInstitutionTin} value={assocTin} onChange={(e) => setAssocTin(e.target.value)} />
              </div>
              <div className="form-field">
                <label>Relationship <Req /></label>
                <select value={relationship} onChange={(e) => setRelationship(e.target.value)}>
                  <option value="customer">Customer</option>
                  <option value="employee">Employee</option>
                  <option value="officer">Officer</option>
                  <option value="director">Director</option>
                  <option value="owner">Owner/Shareholder</option>
                  <option value="none">No relationship to institution</option>
                </select>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Adding…' : 'Add Party'}</button>
      </div>
    </form>
  )
}

// ── Step 4: Suspicious Activity ───────────────────────────────────────────────

function SuspiciousStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: PatchSuspiciousActivityRequest) => void }) {
  const sa = activity.suspiciousActivity
  const [totalAmount, setTotalAmount] = useState(sa?.totalSuspiciousAmount?.toString() ?? '')
  const [fromDate, setFromDate] = useState(sa?.suspiciousActivityFromDate ?? '')
  const [toDate, setToDate] = useState(sa?.suspiciousActivityToDate ?? '')
  const [amountUnknown, setAmountUnknown] = useState(sa?.amountUnknown ?? false)
  const [noAmount, setNoAmount] = useState(sa?.noAmountInvolved ?? false)
  const [cumAmount, setCumAmount] = useState(sa?.cumulativeTotalViolationAmount?.toString() ?? '')

  // Classifications
  const [classifications, setClassifications] = useState<{ typeId: number; subtypeId: number }[]>(
    sa?.classifications?.map(c => ({ typeId: c.suspiciousActivityTypeId, subtypeId: c.suspiciousActivitySubtypeId })) ?? []
  )
  const [newTypeId, setNewTypeId] = useState(1)
  const [newSubtypeId, setNewSubtypeId] = useState(SUSPICIOUS_ACTIVITY_SUBTYPES[1]?.[0]?.code ?? 0)

  function addClassification() {
    if (newTypeId && newSubtypeId) {
      setClassifications(prev => [...prev, { typeId: newTypeId, subtypeId: newSubtypeId }])
    }
  }
  function removeClassification(idx: number) {
    setClassifications(prev => prev.filter((_, i) => i !== idx))
  }

  // Validation
  const saForValidation = {
    seqNum: 1,
    totalSuspiciousAmount: totalAmount ? Number(totalAmount) : undefined,
    suspiciousActivityFromDate: fromDate,
    suspiciousActivityToDate: toDate || undefined,
    amountUnknown,
    noAmountInvolved: noAmount,
    cumulativeTotalViolationAmount: cumAmount ? Number(cumAmount) : undefined,
    classifications: classifications.map((c, i) => ({
      seqNum: i + 1,
      suspiciousActivityTypeId: c.typeId,
      suspiciousActivitySubtypeId: c.subtypeId,
    })),
  }
  const valResult = validateSuspiciousActivity(saForValidation)

  return (
    <form onSubmit={(e) => {
      e.preventDefault()
      onSave({
        totalSuspiciousAmount: totalAmount ? Number(totalAmount) : undefined,
        suspiciousActivityFromDate: fromDate || undefined,
        suspiciousActivityToDate: toDate || undefined,
        amountUnknown,
        noAmountInvolved: noAmount,
        cumulativeTotalViolationAmount: cumAmount ? Number(cumAmount) : undefined,
      })
    }}>
      <h3>Suspicious Activity</h3>

      {valResult.errors.length > 0 && (
        <div className="validation-summary has-errors" style={{ marginTop: '0.5rem' }}>
          <ul>{valResult.errors.map((e, i) => <li key={i} className="val-error">{e.field}: {e.message}</li>)}</ul>
        </div>
      )}

      <div className="form-grid" style={{ marginTop: '0.75rem' }}>
        <div className={`form-field${!fromDate ? ' has-error' : ''}`}>
          <label>From Date <Req /></label>
          <input type="date" value={fromDate} onChange={(e) => setFromDate(e.target.value)} required />
          <FieldErr msg={!fromDate ? 'From Date is required' : undefined} />
        </div>
        <div className="form-field">
          <label>To Date</label>
          <input type="date" value={toDate} onChange={(e) => setToDate(e.target.value)} />
        </div>
      </div>

      <div className="form-section">
        <h4>Amount <Req /></h4>
        <Hint text="Provide one of: Total Amount, Amount Unknown, or No Amount Involved" />
        <div className="form-grid" style={{ marginTop: '0.5rem' }}>
          <div className="form-field">
            <label>Total Suspicious Amount ($)</label>
            <input type="number" min="0" step="1" value={totalAmount} onChange={(e) => { setTotalAmount(e.target.value); if (e.target.value) { setAmountUnknown(false); setNoAmount(false) } }} disabled={amountUnknown || noAmount} placeholder="Whole dollars" />
            <Hint text="15-digit max, no cents" />
          </div>
          <div className="form-field" style={{ display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: '0.5rem' }}>
            <label className="form-check"><input type="checkbox" checked={amountUnknown} onChange={(e) => { setAmountUnknown(e.target.checked); if (e.target.checked) { setTotalAmount(''); setNoAmount(false) } }} /> Amount Unknown</label>
            <label className="form-check"><input type="checkbox" checked={noAmount} onChange={(e) => { setNoAmount(e.target.checked); if (e.target.checked) { setTotalAmount(''); setAmountUnknown(false) } }} /> No Amount Involved</label>
          </div>
        </div>
        <div className="form-field" style={{ marginTop: '0.5rem' }}>
          <label>Cumulative Total Violation Amount ($)</label>
          <input type="number" min="0" step="1" value={cumAmount} onChange={(e) => setCumAmount(e.target.value)} placeholder="Whole dollars" />
          <Hint text="Required for Continuing Activity Reports. 15-digit max." />
        </div>
      </div>

      {/* Classifications */}
      <div className="form-section">
        <h4>Suspicious Activity Classifications <Req /></h4>
        <Hint text="At least one type/subtype classification is required per FinCEN." />

        {classifications.length > 0 && (
          <div className="tag-list" style={{ marginBottom: '0.75rem' }}>
            {classifications.map((c, i) => {
              const typeName = SUSPICIOUS_ACTIVITY_TYPES.find(t => t.code === c.typeId)?.label ?? `Type ${c.typeId}`
              const subtypeName = SUSPICIOUS_ACTIVITY_SUBTYPES[c.typeId]?.find(s => s.code === c.subtypeId)?.label ?? `Subtype ${c.subtypeId}`
              return (
                <span key={i} className="tag">
                  {typeName} → {subtypeName}
                  <button type="button" className="remove-btn" onClick={() => removeClassification(i)} title="Remove">×</button>
                </span>
              )
            })}
          </div>
        )}

        <div className="form-grid">
          <div className="form-field">
            <label>Activity Type</label>
            <select value={newTypeId} onChange={(e) => { const t = Number(e.target.value); setNewTypeId(t); const subs = SUSPICIOUS_ACTIVITY_SUBTYPES[t]; if (subs?.length) setNewSubtypeId(subs[0].code) }}>
              {SUSPICIOUS_ACTIVITY_TYPES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
            </select>
          </div>
          <div className="form-field">
            <label>Activity Subtype</label>
            <select value={newSubtypeId} onChange={(e) => setNewSubtypeId(Number(e.target.value))}>
              {(SUSPICIOUS_ACTIVITY_SUBTYPES[newTypeId] ?? []).map((s) => <option key={s.code} value={s.code}>{s.label}</option>)}
            </select>
          </div>
          <div className="form-field" style={{ display: 'flex', alignItems: 'flex-end' }}>
            <button type="button" className="btn btn-secondary" onClick={addClassification}>+ Add Classification</button>
          </div>
        </div>
        {classifications.length === 0 && <FieldErr msg="At least one classification is required" />}
      </div>

      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Saving…' : 'Save Suspicious Activity'}</button>
      </div>
    </form>
  )
}

// ── Step 5: IP Addresses ──────────────────────────────────────────────────────

function IpStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: IpAddressRequest) => void }) {
  const [ipText, setIpText] = useState('')
  const [ipDate, setIpDate] = useState('')
  const [ipTimestamp, setIpTimestamp] = useState('')

  return (
    <form onSubmit={(e) => {
      e.preventDefault()
      onSave({ seqNum: activity.ipAddresses.length + 1, ipAddressText: ipText, ipAddressDate: ipDate || undefined, ipAddressTimestamp: ipTimestamp || undefined })
      setIpText(''); setIpDate(''); setIpTimestamp('')
    }}>
      <h3>Add IP Address</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>Current: {activity.ipAddresses.length} IP address(es)</p>
      {activity.ipAddresses.length > 0 && (
        <div className="tag-list" style={{ marginBottom: '0.75rem' }}>
          {activity.ipAddresses.map((ip) => <span key={ip.id} className="tag">{ip.ipAddressText}{ip.ipAddressDate ? ` (${ip.ipAddressDate})` : ''}</span>)}
        </div>
      )}
      <div className="form-grid">
        <div className="form-field">
          <label>IP Address <Req /></label>
          <input type="text" value={ipText} onChange={(e) => setIpText(e.target.value)} required maxLength={FIELD_LIMITS.ipAddressText} placeholder="e.g. 192.168.1.1 or 2001:db8::1" />
          <Counter value={ipText} max={FIELD_LIMITS.ipAddressText} />
        </div>
        <div className="form-field">
          <label>Date</label>
          <input type="date" value={ipDate} onChange={(e) => setIpDate(e.target.value)} />
        </div>
        <div className="form-field">
          <label>Timestamp (UTC)</label>
          <input type="time" step="1" value={ipTimestamp} onChange={(e) => setIpTimestamp(e.target.value)} placeholder="HH:MM:SS" />
          <Hint text="UTC time in HH:MM:SS format" />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving || !ipText}>{saving ? 'Adding…' : 'Add IP Address'}</button>
      </div>
    </form>
  )
}

// ── Step 6: Cyber Events ──────────────────────────────────────────────────────

function CyberStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: CyberEventRequest) => void }) {
  const [typeCode, setTypeCode] = useState(1)
  const [value, setValue] = useState('')
  const [date, setDate] = useState('')
  const [timestamp, setTimestamp] = useState('')
  const [otherText, setOtherText] = useState('')

  return (
    <form onSubmit={(e) => {
      e.preventDefault()
      onSave({
        seqNum: activity.cyberEvents.length + 1,
        cyberEventIndicatorsTypeCode: typeCode,
        eventValueText: value,
        cyberEventDate: date || undefined,
        cyberEventTimestamp: timestamp || undefined,
        cyberEventTypeOtherText: typeCode === 999 ? (otherText || undefined) : undefined,
      })
      setValue(''); setDate(''); setTimestamp(''); setOtherText('')
    }}>
      <h3>Add Cyber Event Indicator</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>Current: {activity.cyberEvents.length} event(s)</p>
      {activity.cyberEvents.length > 0 && (
        <div className="tag-list" style={{ marginBottom: '0.75rem' }}>
          {activity.cyberEvents.map((ce) => {
            const lbl = CYBER_EVENT_INDICATOR_CODES.find(c => c.code === ce.cyberEventIndicatorsTypeCode)?.label ?? `Type ${ce.cyberEventIndicatorsTypeCode}`
            return <span key={ce.id} className="tag">{lbl}: {ce.eventValueText.substring(0, 40)}</span>
          })}
        </div>
      )}
      <div className="form-grid">
        <div className="form-field">
          <label>Indicator Type <Req /></label>
          <select value={typeCode} onChange={(e) => setTypeCode(Number(e.target.value))}>
            {CYBER_EVENT_INDICATOR_CODES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
          </select>
        </div>
        {typeCode === 999 && (
          <div className="form-field">
            <label>Other Type Description</label>
            <input type="text" maxLength={FIELD_LIMITS.cyberEventTypeOtherText} value={otherText} onChange={(e) => setOtherText(e.target.value)} />
            <Counter value={otherText} max={FIELD_LIMITS.cyberEventTypeOtherText} />
          </div>
        )}
        <div className="form-field" style={{ gridColumn: '1 / -1' }}>
          <label>Value <Req /></label>
          <input type="text" value={value} onChange={(e) => setValue(e.target.value)} required maxLength={FIELD_LIMITS.eventValueText} placeholder="e.g. IP address, URL, hash value…" />
          <Counter value={value} max={FIELD_LIMITS.eventValueText} />
        </div>
        <div className="form-field">
          <label>Date</label>
          <input type="date" value={date} onChange={(e) => setDate(e.target.value)} />
        </div>
        <div className="form-field">
          <label>Timestamp (UTC)</label>
          <input type="time" step="1" value={timestamp} onChange={(e) => setTimestamp(e.target.value)} />
          <Hint text="UTC time in HH:MM:SS format" />
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving || !value}>{saving ? 'Adding…' : 'Add Cyber Event'}</button>
      </div>
    </form>
  )
}

// ── Step 7: Assets ────────────────────────────────────────────────────────────

function AssetsStep({ activity, saving, onSaveAsset, onSaveAttr }: { activity: ActivityResponse; saving: boolean; onSaveAsset: (data: AssetRequest) => void; onSaveAttr: (data: AssetAttributeRequest) => void }) {
  const [assetTypeId, setAssetTypeId] = useState(5)
  const [assetSubtypeId, setAssetSubtypeId] = useState(ASSET_SUBTYPES[5]?.[0]?.code ?? 2)
  const [otherSubtypeText, setOtherSubtypeText] = useState('')
  const [attrTypeId, setAttrTypeId] = useState(ASSET_ATTRIBUTE_TYPES[0]?.code ?? 1)
  const [attrDesc, setAttrDesc] = useState('')
  const [mode, setMode] = useState<'asset' | 'attr'>('asset')

  const isOtherSubtype = [30, 41].includes(assetSubtypeId)

  return (
    <>
      <h3>Assets &amp; Attributes</h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>Assets: {activity.assets.length} · Attributes: {activity.assetAttributes.length}</p>

      {activity.assets.length > 0 && (
        <div className="tag-list" style={{ marginBottom: '0.75rem' }}>
          {activity.assets.map((a) => {
            const subLbl = [...(ASSET_SUBTYPES[5] ?? []), ...(ASSET_SUBTYPES[6] ?? [])].find(s => s.code === a.assetSubtypeId)?.label ?? `Subtype ${a.assetSubtypeId}`
            return <span key={a.id} className="tag">{subLbl}</span>
          })}
        </div>
      )}

      <div className="tab-bar" style={{ marginBottom: '0.75rem' }}>
        <button className={mode === 'asset' ? 'active' : ''} onClick={() => setMode('asset')}>Add Asset</button>
        <button className={mode === 'attr' ? 'active' : ''} onClick={() => setMode('attr')}>Add Attribute</button>
      </div>

      {mode === 'asset' ? (
        <form onSubmit={(e) => {
          e.preventDefault()
          onSaveAsset({ seqNum: activity.assets.length + 1, assetTypeId, assetSubtypeId, otherAssetSubtypeText: isOtherSubtype ? (otherSubtypeText || undefined) : undefined })
          setOtherSubtypeText('')
        }}>
          <div className="form-grid">
            <div className="form-field">
              <label>Asset Type <Req /></label>
              <select value={assetTypeId} onChange={(e) => { const t = Number(e.target.value); setAssetTypeId(t); const subs = ASSET_SUBTYPES[t]; if (subs?.length) setAssetSubtypeId(subs[0].code) }}>
                {ASSET_TYPES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
              </select>
            </div>
            <div className="form-field">
              <label>Asset Subtype <Req /></label>
              <select value={assetSubtypeId} onChange={(e) => setAssetSubtypeId(Number(e.target.value))}>
                {(ASSET_SUBTYPES[assetTypeId] ?? []).map((s) => <option key={s.code} value={s.code}>{s.label}</option>)}
              </select>
            </div>
            {isOtherSubtype && (
              <div className="form-field">
                <label>Other Subtype Description</label>
                <input type="text" maxLength={FIELD_LIMITS.otherAssetSubtypeText} value={otherSubtypeText} onChange={(e) => setOtherSubtypeText(e.target.value)} />
                <Counter value={otherSubtypeText} max={FIELD_LIMITS.otherAssetSubtypeText} />
              </div>
            )}
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={saving}>{saving ? 'Adding…' : 'Add Asset'}</button>
          </div>
        </form>
      ) : (
        <form onSubmit={(e) => {
          e.preventDefault()
          onSaveAttr({ seqNum: activity.assetAttributes.length + 1, assetAttributeTypeId: attrTypeId, assetAttributeDescriptionText: attrDesc })
          setAttrDesc('')
        }}>
          <div className="form-grid">
            <div className="form-field">
              <label>Attribute Type <Req /></label>
              <select value={attrTypeId} onChange={(e) => setAttrTypeId(Number(e.target.value))}>
                {ASSET_ATTRIBUTE_TYPES.map((t) => <option key={t.code} value={t.code}>{t.label}</option>)}
              </select>
            </div>
            <div className="form-field">
              <label>Description <Req /></label>
              <input type="text" value={attrDesc} onChange={(e) => setAttrDesc(e.target.value)} required maxLength={FIELD_LIMITS.assetAttributeDescriptionText} />
              <Counter value={attrDesc} max={FIELD_LIMITS.assetAttributeDescriptionText} />
            </div>
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary" disabled={saving || !attrDesc}>{saving ? 'Adding…' : 'Add Attribute'}</button>
          </div>
        </form>
      )}
    </>
  )
}

// ── Step 8: Narratives ────────────────────────────────────────────────────────

function NarrativeStep({ activity, saving, onSave }: { activity: ActivityResponse; saving: boolean; onSave: (data: NarrativeRequest) => void }) {
  const [seqNum, setSeqNum] = useState(1)
  const [text, setText] = useState('')

  const totalExisting = activity.narratives.reduce((sum, n) => sum + (n.narrativeText?.length ?? 0), 0)
  const projectedTotal = totalExisting + text.length

  return (
    <form onSubmit={(e) => {
      e.preventDefault()
      onSave({ seqNum: activity.narratives.length + 1, narrativeSequenceNumber: seqNum, narrativeText: text })
      setText(''); setSeqNum((s) => Math.min(s + 1, 5))
    }}>
      <h3>Add Narrative <Req /></h3>
      <p style={{ color: 'var(--muted)', marginBottom: '0.75rem' }}>
        Current: {activity.narratives.length} narrative(s) · Max sequence: 5
      </p>

      {activity.narratives.length > 0 && (
        <div style={{ marginBottom: '0.75rem' }}>
          {activity.narratives.map((n) => (
            <div key={n.id} style={{ fontSize: '0.78rem', padding: '0.35rem 0', borderBottom: '1px solid var(--border)' }}>
              <strong>Seq {n.narrativeSequenceNumber}</strong> — {n.narrativeText.substring(0, 100)}{n.narrativeText.length > 100 ? '…' : ''} ({n.narrativeText.length} chars)
            </div>
          ))}
          <div style={{ fontSize: '0.72rem', color: 'var(--muted)', marginTop: '0.25rem' }}>
            Total characters used: {totalExisting} / {FIELD_LIMITS.narrativeTotalText}
          </div>
        </div>
      )}

      <div className="form-grid">
        <div className="form-field">
          <label>Sequence Number (1-5) <Req /></label>
          <input type="number" min={1} max={FIELD_LIMITS.narrativeMaxBlocks} value={seqNum} onChange={(e) => setSeqNum(Number(e.target.value))} required />
          <Hint text="Block 1 is required. Up to 5 blocks total." />
        </div>
      </div>
      <div className="form-field" style={{ marginTop: '0.75rem' }}>
        <label>Narrative Text <Req /></label>
        <textarea value={text} onChange={(e) => setText(e.target.value)} required maxLength={FIELD_LIMITS.narrativeText} rows={10} placeholder="Describe the suspicious activity in detail…" />
        <div style={{ display: 'flex', justifyContent: 'space-between' }}>
          <Counter value={text} max={FIELD_LIMITS.narrativeText} />
          {projectedTotal > FIELD_LIMITS.narrativeTotalText && (
            <span className="field-error">Total would exceed {FIELD_LIMITS.narrativeTotalText} character limit ({projectedTotal})</span>
          )}
        </div>
      </div>
      <div className="form-actions">
        <button type="submit" className="btn btn-primary" disabled={saving || !text}>{saving ? 'Adding…' : 'Add Narrative'}</button>
      </div>
    </form>
  )
}
