/**
 * FinCEN SAR Client-Side Validation
 *
 * Provides real-time field-level and form-level validation per the
 * FinCEN BSA E-Filing XML Schema 2.0 specification.
 */

import {
  FIELD_LIMITS,
  PARTY_TYPE_REQUIREMENTS,
  SAR_PARTY_TYPES,
} from './fincenReference'
import type {
  ActivityRequest,
  NarrativeRequest,
  PartyRequest,
  SuspiciousActivityRequest,
} from './types'

// ── Types ─────────────────────────────────────────────────────────────────────

export type ValidationError = {
  field: string
  message: string
  severity: 'error' | 'warning'
}

export type ValidationResult = {
  valid: boolean
  errors: ValidationError[]
  warnings: ValidationError[]
}

// ── Field Validators ──────────────────────────────────────────────────────────

export function validateRequired(value: unknown, fieldName: string): ValidationError | null {
  if (value === null || value === undefined || value === '') {
    return { field: fieldName, message: `${fieldName} is required`, severity: 'error' }
  }
  return null
}

export function validateMaxLength(
  value: string | undefined | null,
  maxLen: number,
  fieldName: string,
): ValidationError | null {
  if (value && value.length > maxLen) {
    return {
      field: fieldName,
      message: `${fieldName} must not exceed ${maxLen} characters (currently ${value.length})`,
      severity: 'error',
    }
  }
  return null
}

export function validateExactLength(
  value: string | undefined | null,
  len: number,
  fieldName: string,
): ValidationError | null {
  if (value && value.length !== len) {
    return {
      field: fieldName,
      message: `${fieldName} must be exactly ${len} characters`,
      severity: 'error',
    }
  }
  return null
}

export function validateZipCode(value: string | undefined | null): ValidationError | null {
  if (value && !/^\d{5}(\d{4})?$/.test(value)) {
    return {
      field: 'Zip Code',
      message: 'Zip Code must be 5 or 9 digits (e.g. 12345 or 123456789)',
      severity: 'error',
    }
  }
  return null
}

export function validateWholeNumber(
  value: number | undefined | null,
  fieldName: string,
): ValidationError | null {
  if (value !== null && value !== undefined && !Number.isInteger(value)) {
    return {
      field: fieldName,
      message: `${fieldName} must be a whole dollar amount (no cents)`,
      severity: 'error',
    }
  }
  return null
}

export function validateDateNotFuture(
  value: string | undefined | null,
  fieldName: string,
): ValidationError | null {
  if (value) {
    const d = new Date(value)
    const today = new Date()
    today.setHours(23, 59, 59, 999)
    if (d > today) {
      return {
        field: fieldName,
        message: `${fieldName} cannot be a future date`,
        severity: 'error',
      }
    }
  }
  return null
}

// ── Party Validation ──────────────────────────────────────────────────────────

export function validateParty(party: PartyRequest): ValidationResult {
  const errors: ValidationError[] = []
  const warnings: ValidationError[] = []

  // Type code must be valid
  const validTypes = SAR_PARTY_TYPES.map((t) => t.code)
  if (!validTypes.includes(party.activityPartyTypeCode)) {
    errors.push({
      field: 'Party Type',
      message: `Party type code ${party.activityPartyTypeCode} is not valid for SAR filing`,
      severity: 'error',
    })
  }

  const reqs = PARTY_TYPE_REQUIREMENTS[party.activityPartyTypeCode]
  if (reqs) {
    // Name requirements
    if (reqs.requiresName) {
      const hasLegalName = party.names.some((n) => n.partyNameTypeCode === 'L')
      if (!hasLegalName && party.activityPartyTypeCode !== 33) {
        errors.push({
          field: 'Name',
          message: 'At least one Legal Name (type "L") is required',
          severity: 'error',
        })
      }
      // Subject can skip name if allCriticalSubjectInfoUnavailable
      if (
        party.activityPartyTypeCode === 33 &&
        !hasLegalName &&
        !party.allCriticalSubjectInfoUnavailable
      ) {
        errors.push({
          field: 'Name',
          message:
            'At least one Legal Name is required, or check "All critical subject information unavailable"',
          severity: 'error',
        })
      }
    }

    // Address requirements
    if (reqs.requiresAddress && party.addresses.length === 0) {
      errors.push({
        field: 'Address',
        message: 'At least one address is required for this party type',
        severity: 'error',
      })
    }

    // Identification requirements
    if (reqs.requiresIdentification && party.identifications.length === 0) {
      warnings.push({
        field: 'Identification',
        message: 'At least one identification (TIN, SSN, etc.) is recommended',
        severity: 'warning',
      })
    }

    // Primary regulator
    if (reqs.requiresRegulator && !party.primaryRegulatorTypeCode) {
      errors.push({
        field: 'Primary Regulator',
        message: 'Primary Regulator is required for this party type',
        severity: 'error',
      })
    }

    // Org classification
    if (reqs.requiresOrgClassification && party.orgClassifications.length === 0) {
      errors.push({
        field: 'Organization Type',
        message: 'At least one organization classification is required',
        severity: 'error',
      })
    }

    // Association (for subjects)
    if (reqs.requiresAssociation && party.partyAssociations.length === 0) {
      warnings.push({
        field: 'Association',
        message: 'At least one institution relationship/association is recommended for subject',
        severity: 'warning',
      })
    }
  }

  // Validate name field lengths
  for (const name of party.names) {
    const e1 = validateMaxLength(
      name.rawEntityIndividualLastName,
      FIELD_LIMITS.rawEntityIndividualLastName,
      'Last Name',
    )
    if (e1) errors.push(e1)
    const e2 = validateMaxLength(
      name.rawIndividualFirstName,
      FIELD_LIMITS.rawIndividualFirstName,
      'First Name',
    )
    if (e2) errors.push(e2)
    const e3 = validateMaxLength(
      name.rawIndividualMiddleName,
      FIELD_LIMITS.rawIndividualMiddleName,
      'Middle Name',
    )
    if (e3) errors.push(e3)
    const e4 = validateMaxLength(
      name.rawPartyFullName,
      FIELD_LIMITS.rawPartyFullName,
      'Full Name',
    )
    if (e4) errors.push(e4)
  }

  // Validate address field lengths
  for (const addr of party.addresses) {
    const z = validateZipCode(addr.rawZipCode)
    if (z) errors.push(z)
  }

  // Gender mutual exclusion (subjects only)
  if (party.activityPartyTypeCode === 33) {
    const genderCount = [
      party.maleGenderIndicator,
      party.femaleGenderIndicator,
      party.unknownGenderIndicator,
    ].filter(Boolean).length
    if (genderCount > 1) {
      errors.push({
        field: 'Gender',
        message: 'Only one gender indicator should be selected',
        severity: 'error',
      })
    }
  }

  // Amount validation
  const amtErr = validateWholeNumber(
    party.lossToFinancialAmount as number | undefined,
    'Loss to Financial Institution',
  )
  if (amtErr) errors.push(amtErr)

  return {
    valid: errors.length === 0,
    errors,
    warnings,
  }
}

// ── Suspicious Activity Validation ────────────────────────────────────────────

export function validateSuspiciousActivity(
  sa: SuspiciousActivityRequest,
): ValidationResult {
  const errors: ValidationError[] = []
  const warnings: ValidationError[] = []

  // From date is mandatory
  if (!sa.suspiciousActivityFromDate) {
    errors.push({
      field: 'From Date',
      message: 'Suspicious activity From Date is required',
      severity: 'error',
    })
  }

  // Amount mutual exclusion
  const hasAmount = sa.totalSuspiciousAmount !== undefined && sa.totalSuspiciousAmount !== null
  const isUnknown = sa.amountUnknown === true
  const noAmount = sa.noAmountInvolved === true
  const amountOptions = [hasAmount, isUnknown, noAmount].filter(Boolean).length
  if (amountOptions === 0) {
    errors.push({
      field: 'Amount',
      message:
        'One of: Total Suspicious Amount, Amount Unknown, or No Amount Involved must be provided',
      severity: 'error',
    })
  } else if (amountOptions > 1) {
    errors.push({
      field: 'Amount',
      message:
        'Only one of: Total Suspicious Amount, Amount Unknown, or No Amount Involved should be selected',
      severity: 'error',
    })
  }

  // Amount must be whole dollar
  if (hasAmount) {
    const wErr = validateWholeNumber(sa.totalSuspiciousAmount, 'Total Suspicious Amount')
    if (wErr) errors.push(wErr)
  }

  // At least one classification
  if (!sa.classifications || sa.classifications.length === 0) {
    errors.push({
      field: 'Classifications',
      message: 'At least one suspicious activity classification is required',
      severity: 'error',
    })
  }

  return { valid: errors.length === 0, errors, warnings }
}

// ── Activity-Level Validation ─────────────────────────────────────────────────

export function validateActivity(activity: ActivityRequest): ValidationResult {
  const errors: ValidationError[] = []
  const warnings: ValidationError[] = []

  // Filing date
  if (!activity.filingDate) {
    errors.push({ field: 'Filing Date', message: 'Filing Date is required', severity: 'error' })
  }

  // Activity association is required
  if (!activity.activityAssociation) {
    errors.push({
      field: 'Filing Type',
      message: 'Filing Type (activity association) is required',
      severity: 'error',
    })
  } else {
    const assoc = activity.activityAssociation
    const filingTypeCount = [
      assoc.initialReportIndicator,
      assoc.correctsAmendsPriorReport,
      assoc.continuingActivityReport,
    ].filter(Boolean).length
    if (filingTypeCount === 0) {
      errors.push({
        field: 'Filing Type',
        message:
          'Exactly one filing type must be selected: Initial Report, Corrects/Amends, or Continuing Activity',
        severity: 'error',
      })
    } else if (filingTypeCount > 1) {
      errors.push({
        field: 'Filing Type',
        message:
          'Only one filing type should be selected: Initial Report, Corrects/Amends, or Continuing Activity',
        severity: 'error',
      })
    }

    // Prior doc number required for corrects/continuing
    if (
      (assoc.correctsAmendsPriorReport || assoc.continuingActivityReport) &&
      !activity.efilingPriorDocumentNumber
    ) {
      errors.push({
        field: 'Prior Document Number',
        message: 'Prior Document Number is required when filing type is Corrects/Amends or Continuing',
        severity: 'error',
      })
    }
  }

  // Suspicious activity is required
  if (!activity.suspiciousActivity) {
    errors.push({
      field: 'Suspicious Activity',
      message: 'Suspicious Activity information is required',
      severity: 'error',
    })
  }

  // Must have at least one Filing Institution (30) and one Subject (33)
  const partyTypes = activity.parties.map((p) => p.activityPartyTypeCode)
  if (!partyTypes.includes(30)) {
    errors.push({
      field: 'Parties',
      message: 'At least one Filing Institution (type 30) party is required',
      severity: 'error',
    })
  }
  if (!partyTypes.includes(33)) {
    errors.push({
      field: 'Parties',
      message: 'At least one Subject (type 33) party is required',
      severity: 'error',
    })
  }

  // At least one narrative with sequence 1
  if (!activity.narratives || activity.narratives.length === 0) {
    errors.push({
      field: 'Narrative',
      message: 'At least one narrative block is required',
      severity: 'error',
    })
  } else {
    const hasSeq1 = activity.narratives.some(
      (n) => n.narrativeSequenceNumber === 1,
    )
    if (!hasSeq1) {
      errors.push({
        field: 'Narrative',
        message: 'A narrative block with Sequence Number = 1 is required',
        severity: 'error',
      })
    }
    // Total text limit
    const totalChars = activity.narratives.reduce(
      (sum, n) => sum + (n.narrativeText?.length ?? 0),
      0,
    )
    if (totalChars > FIELD_LIMITS.narrativeTotalText) {
      errors.push({
        field: 'Narrative',
        message: `Total narrative text is ${totalChars} characters — maximum is ${FIELD_LIMITS.narrativeTotalText}`,
        severity: 'error',
      })
    }
  }

  return { valid: errors.length === 0, errors, warnings }
}

// ── Narrative Validation ──────────────────────────────────────────────────────

export function validateNarrative(
  narrative: NarrativeRequest,
  existingNarratives: NarrativeRequest[] = [],
): ValidationResult {
  const errors: ValidationError[] = []
  const warnings: ValidationError[] = []

  if (!narrative.narrativeText || narrative.narrativeText.trim() === '') {
    errors.push({
      field: 'Narrative Text',
      message: 'Narrative text is required',
      severity: 'error',
    })
  }

  if (narrative.narrativeText && narrative.narrativeText.length > FIELD_LIMITS.narrativeText) {
    errors.push({
      field: 'Narrative Text',
      message: `Narrative text must not exceed ${FIELD_LIMITS.narrativeText} characters per block`,
      severity: 'error',
    })
  }

  if (
    narrative.narrativeSequenceNumber < 1 ||
    narrative.narrativeSequenceNumber > 5
  ) {
    errors.push({
      field: 'Sequence Number',
      message: 'Narrative sequence number must be between 1 and 5',
      severity: 'error',
    })
  }

  // Check aggregate limit
  const existingChars = existingNarratives.reduce(
    (sum, n) => sum + (n.narrativeText?.length ?? 0),
    0,
  )
  const newTotal = existingChars + (narrative.narrativeText?.length ?? 0)
  if (newTotal > FIELD_LIMITS.narrativeTotalText) {
    warnings.push({
      field: 'Narrative',
      message: `Adding this block would bring total to ${newTotal} characters (max: ${FIELD_LIMITS.narrativeTotalText})`,
      severity: 'warning',
    })
  }

  return { valid: errors.length === 0, errors, warnings }
}

// ── Helper: Completeness Check ────────────────────────────────────────────────

/** Returns a percentage (0-100) indicating how complete an activity is per FinCEN requirements */
export function activityCompleteness(activity: {
  filingDate?: string
  activityAssociation?: { initialReportIndicator?: boolean; correctsAmendsPriorReport?: boolean; continuingActivityReport?: boolean } | null
  parties: { activityPartyTypeCode: number }[]
  suspiciousActivity?: { suspiciousActivityFromDate?: string | null; classifications?: unknown[] } | null
  narratives: { narrativeSequenceNumber: number }[]
}): { percent: number; missing: string[] } {
  const checks: { label: string; met: boolean }[] = [
    { label: 'Filing Date', met: !!activity.filingDate },
    {
      label: 'Filing Type',
      met: !!(
        activity.activityAssociation?.initialReportIndicator ||
        activity.activityAssociation?.correctsAmendsPriorReport ||
        activity.activityAssociation?.continuingActivityReport
      ),
    },
    {
      label: 'Filing Institution (type 30)',
      met: activity.parties.some((p) => p.activityPartyTypeCode === 30),
    },
    {
      label: 'Subject (type 33)',
      met: activity.parties.some((p) => p.activityPartyTypeCode === 33),
    },
    {
      label: 'Suspicious Activity',
      met: !!(activity.suspiciousActivity?.suspiciousActivityFromDate),
    },
    {
      label: 'Classification(s)',
      met: (activity.suspiciousActivity?.classifications?.length ?? 0) > 0,
    },
    {
      label: 'Narrative (seq 1)',
      met: activity.narratives.some((n) => n.narrativeSequenceNumber === 1),
    },
  ]

  const met = checks.filter((c) => c.met).length
  const missing = checks.filter((c) => !c.met).map((c) => c.label)
  return { percent: Math.round((met / checks.length) * 100), missing }
}
