// ── Filing Status ─────────────────────────────────────────────────────────────

export const FILING_STATUSES = ['DRAFT', 'REVIEW', 'SUBMITTED', 'ACKNOWLEDGED', 'REJECTED'] as const
export type FilingStatus = (typeof FILING_STATUSES)[number]

export const STATUS_COLORS: Record<FilingStatus, string> = {
  DRAFT: '#6b7356',
  REVIEW: '#b8860b',
  SUBMITTED: '#2563eb',
  ACKNOWLEDGED: '#16a34a',
  REJECTED: '#dc2626',
}

// ── Pagination ────────────────────────────────────────────────────────────────

export type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

// ── Dashboard ─────────────────────────────────────────────────────────────────

export type DashboardSummary = {
  totalBatches: number
  totalActivities: number
  totalParties: number
  draftCount: number
  reviewCount: number
  submittedCount: number
  acknowledgedCount: number
  rejectedCount: number
}

export const PARTY_TYPE_LABELS: Record<number, string> = {
  30: 'Filing Institution',
  8: 'Branch Where Activity Occurred',
  23: 'Contact for Assistance',
  33: 'Subject',
  34: 'Seller',
  35: 'Payor',
  37: 'Financial Institution Where Account is Held',
  46: 'Person on Behalf of Subject',
}

// ── Batch ──────────────────────────────────────────────────────────────────────

export type BatchSummary = {
  id: number
  activityCount: number
  partyCount: number
  formTypeCode: string
  filingStatus: string
  createdAt: string
  updatedAt: string
}

export type BatchResponse = {
  id: number
  activityCount: number
  totalAmount: number | null
  partyCount: number
  activityAttachmentCount: number
  attachmentCount: number
  formTypeCode: string
  filingStatus: string
  createdAt: string
  updatedAt: string
  activities: ActivitySummary[]
}

export type BatchRequest = {
  activityCount: number
  totalAmount?: number
  partyCount: number
  activityAttachmentCount?: number
  attachmentCount?: number
}

// ── Activity ──────────────────────────────────────────────────────────────────

export type ActivitySummary = {
  id: number
  seqNum: number
  filingDate: string
  bsaIdentifier: string | null
  filingStatus: string
  createdAt: string
}

export type ActivityResponse = {
  id: number
  batchId: number
  seqNum: number
  efilingPriorDocumentNumber: string | null
  filingDate: string
  filingInstitutionNoteToFincen: string | null
  bsaIdentifier: string | null
  filingStatus: string
  createdAt: string
  activityAssociation: ActivityAssociationResponse | null
  activitySupportDocument: ActivitySupportDocumentResponse | null
  parties: PartyResponse[]
  suspiciousActivity: SuspiciousActivityResponse | null
  ipAddresses: IpAddressResponse[]
  cyberEvents: CyberEventResponse[]
  assets: AssetResponse[]
  assetAttributes: AssetAttributeResponse[]
  narratives: NarrativeResponse[]
}

export type ActivityRequest = {
  seqNum: number
  filingDate: string
  efilingPriorDocumentNumber?: string
  filingInstitutionNoteToFincen?: string
  activityAssociation?: ActivityAssociationRequest
  activitySupportDocument?: ActivitySupportDocumentRequest
  parties: PartyRequest[]
  suspiciousActivity?: SuspiciousActivityRequest
  ipAddresses: IpAddressRequest[]
  cyberEvents: CyberEventRequest[]
  assets: AssetRequest[]
  assetAttributes: AssetAttributeRequest[]
  narratives: NarrativeRequest[]
}

// ── Activity Association ──────────────────────────────────────────────────────

export type ActivityAssociationResponse = {
  id: number
  initialReportIndicator: boolean
  correctsAmendsPriorReport: boolean
  continuingActivityReport: boolean
  jointReportIndicator: boolean
}

export type ActivityAssociationRequest = {
  seqNum: number
  initialReportIndicator?: boolean
  correctsAmendsPriorReport?: boolean
  continuingActivityReport?: boolean
  jointReportIndicator?: boolean
}

// ── Activity Support Document ─────────────────────────────────────────────────

export type ActivitySupportDocumentResponse = {
  id: number
  originalAttachmentFileName: string
}

export type ActivitySupportDocumentRequest = {
  seqNum: number
  originalAttachmentFileName: string
}

// ── Party ─────────────────────────────────────────────────────────────────────

export type PartyResponse = {
  id: number
  seqNum: number
  activityPartyTypeCode: number
  lossToFinancialAmount: number | null
  noBranchActivityInvolved: boolean | null
  primaryRegulatorTypeCode: number | null
  admissionConfessionYes: boolean | null
  admissionConfessionNo: boolean | null
  individualBirthDate: string | null
  maleGenderIndicator: boolean | null
  femaleGenderIndicator: boolean | null
  unknownGenderIndicator: boolean | null
  partyAsEntityOrganization: boolean | null
  names: PartyNameResponse[]
  addresses: PartyAddressResponse[]
  phones: PartyPhoneResponse[]
  identifications: PartyIdentificationResponse[]
  orgClassifications: OrgClassificationResponse[]
  occupation: PartyOccupationResponse | null
  electronicAddresses: ElectronicAddressResponse[]
  partyAssociations: PartyAssociationResponse[]
  partyAccountAssociation: PartyAccountAssociationResponse | null
}

export type PartyRequest = {
  seqNum: number
  activityPartyTypeCode: number
  lossToFinancialAmount?: number
  noBranchActivityInvolved?: boolean
  primaryRegulatorTypeCode?: number
  admissionConfessionNo?: boolean
  admissionConfessionYes?: boolean
  individualBirthDate?: string
  maleGenderIndicator?: boolean
  femaleGenderIndicator?: boolean
  unknownGenderIndicator?: boolean
  partyAsEntityOrganization?: boolean
  names: PartyNameRequest[]
  addresses: PartyAddressRequest[]
  phones: PartyPhoneRequest[]
  identifications: PartyIdentificationRequest[]
  orgClassifications: OrgClassificationRequest[]
  occupation?: PartyOccupationRequest
  electronicAddresses: ElectronicAddressRequest[]
  partyAssociations: PartyAssociationRequest[]
  partyAccountAssociation?: PartyAccountAssociationRequest
}

// ── Party Name ────────────────────────────────────────────────────────────────

export type PartyNameResponse = {
  id: number
  partyNameTypeCode: string
  rawPartyFullName: string | null
  rawEntityIndividualLastName: string | null
  rawIndividualFirstName: string | null
  rawIndividualMiddleName: string | null
  rawIndividualNameSuffixText: string | null
}

export type PartyNameRequest = {
  seqNum: number
  partyNameTypeCode: string
  rawPartyFullName?: string
  rawEntityIndividualLastName?: string
  rawIndividualFirstName?: string
  rawIndividualMiddleName?: string
  rawIndividualNameSuffixText?: string
}

// ── Party Address ─────────────────────────────────────────────────────────────

export type PartyAddressResponse = {
  id: number
  rawStreetAddress1: string | null
  rawCity: string | null
  rawStateCode: string | null
  rawZipCode: string | null
  rawCountryCode: string | null
  cityUnknown: boolean | null
  streetAddressUnknown: boolean | null
}

export type PartyAddressRequest = {
  seqNum: number
  rawStreetAddress1?: string
  rawCity?: string
  rawStateCode?: string
  rawZipCode?: string
  rawCountryCode?: string
}

// ── Party Phone ───────────────────────────────────────────────────────────────

export type PartyPhoneResponse = {
  id: number
  phoneNumberText: string | null
  phoneNumberExtension: string | null
  phoneNumberTypeCode: string | null
}

export type PartyPhoneRequest = {
  seqNum: number
  phoneNumberText?: string
  phoneNumberExtension?: string
  phoneNumberTypeCode?: string
}

// ── Party Identification ──────────────────────────────────────────────────────

export type PartyIdentificationResponse = {
  id: number
  partyIdentificationTypeCode: number | null
  partyIdentificationNumber: string | null
  tinUnknown: boolean | null
  otherIssuerCountry: string | null
  otherIssuerState: string | null
}

export type PartyIdentificationRequest = {
  seqNum: number
  partyIdentificationTypeCode?: number
  partyIdentificationNumber?: string
  tinUnknown?: boolean
  otherIssuerCountry?: string
  otherIssuerState?: string
}

// ── Org Classification ────────────────────────────────────────────────────────

export type OrgClassificationResponse = {
  id: number
  organizationTypeId: number
  organizationSubtypeId: number | null
  otherOrganizationTypeText: string | null
}

export type OrgClassificationRequest = {
  seqNum: number
  organizationTypeId: number
  organizationSubtypeId?: number
  otherOrganizationTypeText?: string
}

// ── Party Occupation ──────────────────────────────────────────────────────────

export type PartyOccupationResponse = {
  id: number
  naicsCode: string | null
  occupationBusinessText: string | null
}

export type PartyOccupationRequest = {
  seqNum: number
  naicsCode?: string
  occupationBusinessText?: string
}

// ── Electronic Address ────────────────────────────────────────────────────────

export type ElectronicAddressResponse = {
  id: number
  electronicAddressTypeCode: string
  electronicAddressText: string
}

export type ElectronicAddressRequest = {
  seqNum: number
  electronicAddressTypeCode: string
  electronicAddressText: string
}

// ── Party Association ─────────────────────────────────────────────────────────

export type PartyAssociationResponse = {
  id: number
  subjectRelationshipInstitutionTin: string | null
  customerIndicator: boolean | null
  employeeIndicator: boolean | null
  officerIndicator: boolean | null
  noRelationshipToInstitution: boolean | null
  relationshipContinues: boolean | null
  terminatedIndicator: boolean | null
  actionTakenDate: string | null
  branchParties: BranchPartyResponse[]
}

export type PartyAssociationRequest = {
  seqNum: number
  subjectRelationshipInstitutionTin?: string
  customerIndicator?: boolean
  employeeIndicator?: boolean
  officerIndicator?: boolean
  noRelationshipToInstitution?: boolean
  relationshipContinues?: boolean
  terminatedIndicator?: boolean
  actionTakenDate?: string
  branchParties: BranchPartyRequest[]
}

// ── Branch Party ──────────────────────────────────────────────────────────────

export type BranchPartyResponse = {
  id: number
  sellingLocationIndicator: boolean | null
  payLocationIndicator: boolean | null
  sellingPayingLocationIndicator: boolean | null
  addresses: BranchAddressResponse[]
  identification: BranchIdentificationResponse | null
}

export type BranchPartyRequest = {
  seqNum: number
  sellingLocationIndicator?: boolean
  payLocationIndicator?: boolean
  sellingPayingLocationIndicator?: boolean
  addresses: BranchAddressRequest[]
  identification?: BranchIdentificationRequest
}

export type BranchAddressResponse = {
  id: number
  rawStreetAddress1: string | null
  rawCity: string | null
  rawStateCode: string | null
  rawZipCode: string | null
  rawCountryCode: string | null
}

export type BranchAddressRequest = {
  seqNum: number
  rawStreetAddress1?: string
  rawCity?: string
  rawStateCode?: string
  rawZipCode?: string
  rawCountryCode: string
}

export type BranchIdentificationResponse = {
  id: number
  partyIdentificationNumber: string
}

export type BranchIdentificationRequest = {
  seqNum: number
  partyIdentificationNumber: string
}

// ── Party Account Association ─────────────────────────────────────────────────

export type PartyAccountAssociationResponse = {
  id: number
  accountHoldingParties: AccountHoldingPartyResponse[]
}

export type PartyAccountAssociationRequest = {
  seqNum: number
  accountHoldingParties: AccountHoldingPartyRequest[]
}

export type AccountHoldingPartyResponse = {
  id: number
  nonUsFinancialInstitution: boolean | null
  identification: AccountHoldingPartyIdentificationResponse | null
  accounts: AccountResponse[]
}

export type AccountHoldingPartyRequest = {
  seqNum: number
  nonUsFinancialInstitution?: boolean
  identification?: AccountHoldingPartyIdentificationRequest
  accounts: AccountRequest[]
}

export type AccountHoldingPartyIdentificationResponse = {
  id: number
  partyIdentificationNumber: string
}

export type AccountHoldingPartyIdentificationRequest = {
  seqNum: number
  partyIdentificationNumber?: string
}

export type AccountResponse = {
  id: number
  accountNumberText: string
  accountClosedIndicator: boolean | null
}

export type AccountRequest = {
  seqNum: number
  accountNumberText: string
  accountClosedIndicator?: boolean
}

// ── Suspicious Activity ───────────────────────────────────────────────────────

export type SuspiciousActivityResponse = {
  id: number
  amountUnknown: boolean | null
  noAmountInvolved: boolean | null
  totalSuspiciousAmount: number | null
  suspiciousActivityFromDate: string | null
  suspiciousActivityToDate: string | null
  cumulativeTotalViolationAmount: number | null
  classifications: SuspiciousActivityClassificationResponse[]
}

export type SuspiciousActivityRequest = {
  seqNum: number
  amountUnknown?: boolean
  noAmountInvolved?: boolean
  totalSuspiciousAmount?: number
  suspiciousActivityFromDate: string
  suspiciousActivityToDate?: string
  cumulativeTotalViolationAmount?: number
  classifications: SuspiciousActivityClassificationRequest[]
}

export type SuspiciousActivityClassificationResponse = {
  id: number
  suspiciousActivityTypeId: number
  suspiciousActivitySubtypeId: number
  otherSuspiciousActivityTypeText: string | null
}

export type SuspiciousActivityClassificationRequest = {
  seqNum: number
  suspiciousActivityTypeId: number
  suspiciousActivitySubtypeId: number
  otherSuspiciousActivityTypeText?: string
}

// ── IP Address ────────────────────────────────────────────────────────────────

export type IpAddressResponse = {
  id: number
  ipAddressText: string
  ipAddressDate: string | null
  ipAddressTimestamp: string | null
}

export type IpAddressRequest = {
  seqNum: number
  ipAddressText: string
  ipAddressDate?: string
  ipAddressTimestamp?: string
}

// ── Cyber Event ───────────────────────────────────────────────────────────────

export type CyberEventResponse = {
  id: number
  cyberEventIndicatorsTypeCode: number
  eventValueText: string
  cyberEventDate: string | null
  cyberEventTimestamp: string | null
  cyberEventTypeOtherText: string | null
}

export type CyberEventRequest = {
  seqNum: number
  cyberEventIndicatorsTypeCode: number
  eventValueText: string
  cyberEventDate?: string
  cyberEventTimestamp?: string
  cyberEventTypeOtherText?: string
}

// ── Asset ─────────────────────────────────────────────────────────────────────

export type AssetResponse = {
  id: number
  assetTypeId: number
  assetSubtypeId: number
  otherAssetSubtypeText: string | null
}

export type AssetRequest = {
  seqNum: number
  assetTypeId: number
  assetSubtypeId: number
  otherAssetSubtypeText?: string
}

// ── Asset Attribute ───────────────────────────────────────────────────────────

export type AssetAttributeResponse = {
  id: number
  assetAttributeTypeId: number
  assetAttributeDescriptionText: string
}

export type AssetAttributeRequest = {
  seqNum: number
  assetAttributeTypeId: number
  assetAttributeDescriptionText: string
}

// ── Narrative ─────────────────────────────────────────────────────────────────

export type NarrativeResponse = {
  id: number
  narrativeSequenceNumber: number
  narrativeText: string
}

export type NarrativeRequest = {
  seqNum: number
  narrativeSequenceNumber: number
  narrativeText: string
}

// ── Patch DTOs ────────────────────────────────────────────────────────────────

export type PatchActivityHeaderRequest = {
  filingDate?: string
  efilingPriorDocumentNumber?: string
  filingInstitutionNoteToFincen?: string
}

export type PatchFilingTypeRequest = {
  initialReportIndicator?: boolean
  correctsAmendsPriorReport?: boolean
  continuingActivityReport?: boolean
  jointReportIndicator?: boolean
}

export type PatchSupportDocumentRequest = {
  originalAttachmentFileName?: string
}

export type PatchPartyHeaderRequest = {
  lossToFinancialAmount?: number
  noBranchActivityInvolved?: boolean
  primaryRegulatorTypeCode?: number
  admissionConfessionNo?: boolean
  admissionConfessionYes?: boolean
  individualBirthDate?: string
  maleGenderIndicator?: boolean
  femaleGenderIndicator?: boolean
  unknownGenderIndicator?: boolean
  partyAsEntityOrganization?: boolean
}

export type PatchSuspiciousActivityRequest = {
  amountUnknown?: boolean
  noAmountInvolved?: boolean
  totalSuspiciousAmount?: number
  suspiciousActivityFromDate?: string
  suspiciousActivityToDate?: string
  cumulativeTotalViolationAmount?: number
}

export type PatchNarrativeRequest = {
  narrativeText: string
}

// ── API Error ─────────────────────────────────────────────────────────────────

export type ApiError = {
  status: number
  error: string
  message: string
  path: string
  timestamp: string
}