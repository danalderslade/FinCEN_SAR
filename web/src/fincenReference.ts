/**
 * FinCEN BSA E-Filing XML Schema 2.0 — Reference Data Constants
 *
 * All codes and labels are sourced directly from the database schema
 * reference tables. Used for client-side validation and dropdown population.
 */

// ── Party Type Codes ──────────────────────────────────────────────────────────

export const PARTY_TYPES: Record<number, string> = {
  8: 'Designated Contact Office',
  18: 'Law Enforcement Agency',
  19: 'Law Enforcement Name',
  30: 'Filing Institution',
  33: 'Subject',
  34: 'Financial Institution Where Activity Occurred',
  35: 'Transmitter',
  37: 'Transmitter Contact',
  41: 'Financial Institution Where Account is Held',
  46: 'Branch Where Activity Occurred',
}

/** Party types relevant for SAR Activity form entry */
export const SAR_PARTY_TYPES: { code: number; label: string; description: string }[] = [
  { code: 30, label: 'Filing Institution', description: 'The financial institution filing the SAR (Part III)' },
  { code: 33, label: 'Subject', description: 'Person or entity that is the subject of suspicious activity (Part I)' },
  { code: 34, label: 'FI Where Activity Occurred', description: 'The FI where the suspicious activity took place (Part IV)' },
  { code: 46, label: 'Branch Where Activity Occurred', description: 'The branch location where activity occurred' },
  { code: 8, label: 'Designated Contact Office', description: 'Contact office designated by the filing institution' },
  { code: 35, label: 'Transmitter', description: 'Entity transmitting the SAR filing to FinCEN' },
  { code: 37, label: 'Transmitter Contact', description: 'Contact person at the transmitter' },
  { code: 18, label: 'Law Enforcement Agency', description: 'Law enforcement agency contacted about the activity' },
  { code: 19, label: 'Law Enforcement Name', description: 'Name of law enforcement contact' },
  { code: 41, label: 'FI Where Account is Held', description: 'Financial institution where the subject account is held' },
]

// ── Party Name Type Codes ─────────────────────────────────────────────────────

export const PARTY_NAME_TYPE_CODES: { code: string; label: string }[] = [
  { code: 'L', label: 'Legal' },
  { code: 'AKA', label: 'Also Known As' },
  { code: 'DBA', label: 'Doing Business As' },
]

// ── Party Identification Type Codes ───────────────────────────────────────────

export const PARTY_ID_TYPE_CODES: { code: number; label: string; maxLength: number }[] = [
  { code: 1, label: 'SSN/ITIN', maxLength: 9 },
  { code: 2, label: 'EIN', maxLength: 9 },
  { code: 4, label: 'TIN', maxLength: 25 },
  { code: 5, label: "Driver's license/State ID", maxLength: 24 },
  { code: 6, label: 'Passport', maxLength: 24 },
  { code: 7, label: 'Alien registration', maxLength: 24 },
  { code: 9, label: 'Foreign Taxpayer Identification Number', maxLength: 25 },
  { code: 10, label: 'CRD number', maxLength: 10 },
  { code: 11, label: 'IARD number', maxLength: 10 },
  { code: 12, label: 'NFA ID number', maxLength: 10 },
  { code: 13, label: 'SEC number', maxLength: 10 },
  { code: 14, label: 'RSSD number', maxLength: 10 },
  { code: 28, label: 'TCC', maxLength: 14 },
  { code: 29, label: 'Internal control/file number', maxLength: 20 },
  { code: 32, label: 'NAIC number', maxLength: 10 },
  { code: 33, label: 'NMLS number', maxLength: 10 },
  { code: 999, label: 'Other Identification', maxLength: 24 },
]

// ── Primary Regulator Type Codes ──────────────────────────────────────────────

export const PRIMARY_REGULATOR_CODES: { code: number; label: string }[] = [
  { code: 1, label: 'Federal Reserve' },
  { code: 2, label: 'FDIC' },
  { code: 3, label: 'NCUA' },
  { code: 4, label: 'OCC' },
  { code: 6, label: 'SEC' },
  { code: 7, label: 'IRS' },
  { code: 9, label: 'CFTC' },
  { code: 13, label: 'FHFA' },
  { code: 99, label: 'Not Applicable' },
]

// ── Organization Type / Subtype Codes ─────────────────────────────────────────

export const ORGANIZATION_TYPE_CODES: { code: number; label: string }[] = [
  { code: 1, label: 'Casino/Card club' },
  { code: 2, label: 'Depository institution' },
  { code: 3, label: 'Insurance company' },
  { code: 4, label: 'MSB (Money Service Business)' },
  { code: 5, label: 'Securities/Futures' },
  { code: 11, label: 'Loan or Finance Company' },
  { code: 12, label: 'Housing GSE' },
  { code: 999, label: 'Other' },
]

export const ORGANIZATION_SUBTYPES: Record<number, { code: number; label: string }[]> = {
  1: [
    { code: 101, label: 'State casino' },
    { code: 102, label: 'Tribal casino' },
    { code: 103, label: 'Card club' },
    { code: 1999, label: 'Other (Casino/Card club)' },
  ],
  5: [
    { code: 503, label: 'Subsidiary of financial/bank holding company' },
    { code: 504, label: 'Holding Company' },
    { code: 508, label: 'Futures Commission Merchant' },
    { code: 513, label: 'Introducing broker-commodity' },
    { code: 514, label: 'Investment adviser' },
    { code: 528, label: 'SRO futures' },
    { code: 529, label: 'SRO securities' },
    { code: 533, label: 'Retail foreign exchange dealer' },
    { code: 534, label: 'CPO/CTA' },
    { code: 535, label: 'Clearing broker-securities' },
    { code: 539, label: 'Investment company' },
    { code: 540, label: 'Introducing broker-securities' },
    { code: 541, label: 'Execution-only broker securities' },
    { code: 542, label: 'Self-clearing broker-securities' },
    { code: 5999, label: 'Other (Securities/Futures)' },
  ],
}

// ── Suspicious Activity Type / Subtype Codes ──────────────────────────────────

export const SUSPICIOUS_ACTIVITY_TYPES: { code: number; label: string }[] = [
  { code: 1, label: 'Structuring' },
  { code: 3, label: 'Fraud' },
  { code: 4, label: 'Identification/Documentation' },
  { code: 5, label: 'Insurance' },
  { code: 6, label: 'Securities/Futures/Options' },
  { code: 7, label: 'Terrorist financing' },
  { code: 8, label: 'Money Laundering' },
  { code: 9, label: 'Other suspicious activities' },
  { code: 10, label: 'Mortgage fraud' },
  { code: 11, label: 'Cyber event' },
  { code: 12, label: 'Gaming activities' },
]

export const SUSPICIOUS_ACTIVITY_SUBTYPES: Record<number, { code: number; label: string }[]> = {
  1: [
    { code: 111, label: 'Alters or cancels transaction to avoid BSA recordkeeping requirement' },
    { code: 112, label: 'Alters or cancels transaction to avoid CTR requirement' },
    { code: 106, label: 'Suspicious inquiry by customer regarding BSA reporting or recordkeeping requirements' },
    { code: 113, label: 'Transaction(s) below BSA recordkeeping threshold' },
    { code: 114, label: 'Transaction(s) below CTR threshold' },
    { code: 1999, label: 'Other (Structuring)' },
  ],
  3: [
    { code: 320, label: 'ACH' },
    { code: 322, label: 'Advance fee' },
    { code: 321, label: 'Business loan' },
    { code: 301, label: 'Check' },
    { code: 304, label: 'Consumer Loan' },
    { code: 305, label: 'Credit/Debit Card' },
    { code: 323, label: 'Healthcare/Public or private health insurance' },
    { code: 308, label: 'Mail' },
    { code: 309, label: 'Mass-marketing' },
    { code: 324, label: 'Ponzi scheme' },
    { code: 310, label: 'Pyramid scheme' },
    { code: 325, label: 'Securities fraud' },
    { code: 312, label: 'Wire transfer' },
    { code: 3999, label: 'Other (Fraud)' },
  ],
  4: [
    { code: 401, label: 'Changes spelling or arrangement of name' },
    { code: 402, label: 'Multiple individuals with same or similar identities' },
    { code: 403, label: 'Provided questionable or false documentation' },
    { code: 409, label: 'Provided questionable or false identification' },
    { code: 404, label: 'Refused or avoided request for documentation' },
    { code: 405, label: 'Single individual with multiple identities' },
    { code: 4999, label: 'Other (Identification/Documentation)' },
  ],
  5: [
    { code: 501, label: 'Excessive insurance' },
    { code: 502, label: 'Excessive or unusual cash borrowing against policy/annuity' },
    { code: 504, label: 'Proceeds sent to unrelated third party' },
    { code: 505, label: 'Suspicious life settlement sales insurance (e.g. STOLI\'s Viaticals)' },
    { code: 506, label: 'Suspicious termination of policy or contract' },
    { code: 507, label: 'Unclear or no insurable interest' },
    { code: 5999, label: 'Other (Insurance)' },
  ],
  6: [
    { code: 601, label: 'Insider trading' },
    { code: 608, label: 'Market manipulation' },
    { code: 603, label: 'Misappropriation' },
    { code: 604, label: 'Unauthorized pooling' },
    { code: 609, label: 'Wash trading' },
    { code: 6999, label: 'Other (Securities/Futures/Options)' },
  ],
  7: [
    { code: 701, label: 'Known or suspected terrorist/terrorist organization' },
    { code: 7999, label: 'Other (Terrorist financing)' },
  ],
  8: [
    { code: 801, label: 'Exchanges small bills for large bills or vice versa' },
    { code: 824, label: 'Funnel account' },
    { code: 820, label: 'Suspicious concerning the physical condition of funds' },
    { code: 821, label: 'Suspicious concerning the source of funds' },
    { code: 804, label: 'Suspicious designation of beneficiaries assignees or joint owners' },
    { code: 805, label: 'Suspicious EFT/wire transfers' },
    { code: 822, label: 'Suspicious exchange of currencies' },
    { code: 806, label: 'Suspicious receipt of government payments/benefits' },
    { code: 807, label: 'Suspicious use of multiple accounts' },
    { code: 808, label: 'Suspicious use of noncash monetary instruments' },
    { code: 809, label: 'Suspicious use of third-party transactions (straw-man)' },
    { code: 823, label: 'Trade Based Money Laundering/Black Market Peso Exchange' },
    { code: 812, label: 'Transaction out of pattern for customer(s)' },
    { code: 8999, label: 'Other (Money Laundering)' },
  ],
  9: [
    { code: 920, label: 'Account takeover' },
    { code: 901, label: 'Bribery or gratuity' },
    { code: 917, label: 'Counterfeit Instrument (other)' },
    { code: 921, label: 'Elder financial exploitation' },
    { code: 903, label: 'Embezzlement/theft/disappearance of funds' },
    { code: 904, label: 'Forgeries' },
    { code: 926, label: 'Human smuggling' },
    { code: 927, label: 'Human trafficking' },
    { code: 905, label: 'Identity theft' },
    { code: 922, label: 'Little or no concern for product performance penalties fees or tax consequences' },
    { code: 924, label: 'Misuse of position or self-dealing' },
    { code: 907, label: 'Suspected public/private corruption (domestic)' },
    { code: 908, label: 'Suspected public/private corruption (foreign)' },
    { code: 909, label: 'Suspicious use of informal value transfer system' },
    { code: 910, label: 'Suspicious use of multiple locations' },
    { code: 925, label: 'Transaction with no apparent economic business or lawful purpose' },
    { code: 928, label: 'Transaction(s) involving foreign high risk jurisdiction' },
    { code: 911, label: 'Two or more individuals working together' },
    { code: 913, label: 'Unlicensed or unregistered MSB' },
    { code: 9999, label: 'Other (Other suspicious activities)' },
  ],
  10: [
    { code: 1005, label: 'Application fraud' },
    { code: 1001, label: 'Appraisal fraud' },
    { code: 1006, label: 'Foreclosure/Short sale fraud' },
    { code: 1003, label: 'Loan Modification fraud' },
    { code: 1007, label: 'Origination fraud' },
    { code: 10999, label: 'Other (Mortgage fraud)' },
  ],
  11: [
    { code: 1101, label: 'Against financial institution(s)' },
    { code: 1102, label: 'Against financial institution customer(s)' },
    { code: 11999, label: 'Other (Cyber event)' },
  ],
  12: [
    { code: 1201, label: 'Chip walking' },
    { code: 1202, label: 'Minimal gaming with large transactions' },
    { code: 1203, label: 'Suspicious use of counter checks or markers' },
    { code: 1204, label: 'Unknown source of chips' },
    { code: 12999, label: 'Other (Gaming activities)' },
  ],
}

// ── Cyber Event Indicator Type Codes ──────────────────────────────────────────

export const CYBER_EVENT_INDICATOR_CODES: { code: number; label: string }[] = [
  { code: 1, label: 'Command and control IP address' },
  { code: 2, label: 'Command and control URL/domain' },
  { code: 3, label: 'Malware MD5 SHA-1 or SHA-256' },
  { code: 4, label: 'Media Access Control (MAC) Address' },
  { code: 5, label: 'Port' },
  { code: 6, label: 'Suspicious e-mail address' },
  { code: 7, label: 'Suspicious file name' },
  { code: 8, label: 'Suspicious IP address' },
  { code: 9, label: 'Suspicious URL/domain' },
  { code: 10, label: 'Targeted system' },
  { code: 999, label: 'Other' },
]

// ── Asset Type / Subtype Codes ────────────────────────────────────────────────

export const ASSET_TYPES: { code: number; label: string }[] = [
  { code: 5, label: 'Product type(s) involved in suspicious activity' },
  { code: 6, label: 'Instrument type(s)/payment mechanism(s) involved in suspicious activity' },
]

export const ASSET_SUBTYPES: Record<number, { code: number; label: string }[]> = {
  5: [
    { code: 2, label: 'Bonds/Notes' },
    { code: 3, label: 'Commercial mortgage' },
    { code: 4, label: 'Commercial paper' },
    { code: 5, label: 'Credit card' },
    { code: 6, label: 'Debit card' },
    { code: 46, label: 'Deposit Account' },
    { code: 7, label: 'Forex transactions' },
    { code: 8, label: 'Futures/Options on futures' },
    { code: 9, label: 'Hedge fund' },
    { code: 11, label: 'Home equity line of credit' },
    { code: 10, label: 'Home equity loan' },
    { code: 12, label: 'Insurance/Annuity products' },
    { code: 47, label: 'Microcap securities' },
    { code: 13, label: 'Mutual fund' },
    { code: 14, label: 'Options on securities' },
    { code: 16, label: 'Prepaid access' },
    { code: 17, label: 'Residential mortgage' },
    { code: 18, label: 'Security futures products' },
    { code: 19, label: 'Stocks' },
    { code: 20, label: 'Swap hybrid or other derivative' },
    { code: 30, label: 'Other (Product type)' },
  ],
  6: [
    { code: 31, label: 'Bank/Cashier\'s check' },
    { code: 32, label: 'Foreign currency' },
    { code: 33, label: 'Funds transfer' },
    { code: 34, label: 'Gaming instruments' },
    { code: 35, label: 'Government payment' },
    { code: 36, label: 'Money orders' },
    { code: 37, label: 'Personal/Business check' },
    { code: 38, label: 'Travelers checks' },
    { code: 39, label: 'U.S. Currency' },
    { code: 41, label: 'Other (Instrument/Payment mechanism)' },
  ],
}

// ── Asset Attribute Type Codes ────────────────────────────────────────────────

export const ASSET_ATTRIBUTE_TYPES: { code: number; label: string }[] = [
  { code: 1, label: 'CUSIP number' },
  { code: 2, label: 'Commodity type' },
  { code: 3, label: 'Product/Instrument type' },
  { code: 4, label: 'Market where traded' },
]

// ── Electronic Address Type Codes ─────────────────────────────────────────────

export const ELECTRONIC_ADDRESS_TYPE_CODES: { code: string; label: string }[] = [
  { code: 'E', label: 'E-mail address' },
  { code: 'U', label: 'Website (URL) address' },
]

// ── Phone Number Type Codes ───────────────────────────────────────────────────

export const PHONE_NUMBER_TYPE_CODES: { code: string; label: string }[] = [
  { code: 'R', label: 'Residence (Home)' },
  { code: 'W', label: 'Work' },
  { code: 'M', label: 'Mobile' },
  { code: 'F', label: 'Fax' },
]

// ── US State Codes ────────────────────────────────────────────────────────────

export const US_STATE_CODES: { code: string; label: string }[] = [
  { code: 'AL', label: 'Alabama' }, { code: 'AK', label: 'Alaska' },
  { code: 'AZ', label: 'Arizona' }, { code: 'AR', label: 'Arkansas' },
  { code: 'CA', label: 'California' }, { code: 'CO', label: 'Colorado' },
  { code: 'CT', label: 'Connecticut' }, { code: 'DE', label: 'Delaware' },
  { code: 'DC', label: 'District of Columbia' }, { code: 'FL', label: 'Florida' },
  { code: 'GA', label: 'Georgia' }, { code: 'HI', label: 'Hawaii' },
  { code: 'ID', label: 'Idaho' }, { code: 'IL', label: 'Illinois' },
  { code: 'IN', label: 'Indiana' }, { code: 'IA', label: 'Iowa' },
  { code: 'KS', label: 'Kansas' }, { code: 'KY', label: 'Kentucky' },
  { code: 'LA', label: 'Louisiana' }, { code: 'ME', label: 'Maine' },
  { code: 'MD', label: 'Maryland' }, { code: 'MA', label: 'Massachusetts' },
  { code: 'MI', label: 'Michigan' }, { code: 'MN', label: 'Minnesota' },
  { code: 'MS', label: 'Mississippi' }, { code: 'MO', label: 'Missouri' },
  { code: 'MT', label: 'Montana' }, { code: 'NE', label: 'Nebraska' },
  { code: 'NV', label: 'Nevada' }, { code: 'NH', label: 'New Hampshire' },
  { code: 'NJ', label: 'New Jersey' }, { code: 'NM', label: 'New Mexico' },
  { code: 'NY', label: 'New York' }, { code: 'NC', label: 'North Carolina' },
  { code: 'ND', label: 'North Dakota' }, { code: 'OH', label: 'Ohio' },
  { code: 'OK', label: 'Oklahoma' }, { code: 'OR', label: 'Oregon' },
  { code: 'PA', label: 'Pennsylvania' }, { code: 'RI', label: 'Rhode Island' },
  { code: 'SC', label: 'South Carolina' }, { code: 'SD', label: 'South Dakota' },
  { code: 'TN', label: 'Tennessee' }, { code: 'TX', label: 'Texas' },
  { code: 'UT', label: 'Utah' }, { code: 'VT', label: 'Vermont' },
  { code: 'VA', label: 'Virginia' }, { code: 'WA', label: 'Washington' },
  { code: 'WV', label: 'West Virginia' }, { code: 'WI', label: 'Wisconsin' },
  { code: 'WY', label: 'Wyoming' },
  { code: 'AS', label: 'American Samoa' }, { code: 'GU', label: 'Guam' },
  { code: 'MP', label: 'Northern Mariana Islands' }, { code: 'PR', label: 'Puerto Rico' },
  { code: 'VI', label: 'U.S. Virgin Islands' },
]

// ── Field Length Constraints ──────────────────────────────────────────────────

export const FIELD_LIMITS = {
  rawPartyFullName: 150,
  rawEntityIndividualLastName: 150,
  rawIndividualFirstName: 35,
  rawIndividualMiddleName: 35,
  rawIndividualNameSuffixText: 35,
  rawStreetAddress1: 100,
  rawCity: 50,
  rawZipCode: 9,
  phoneNumberText: 16,
  phoneNumberExtension: 6,
  partyIdentificationNumber: 25,
  occupationBusinessText: 50,
  naicsCode: 6,
  electronicAddressText: 517,
  narrativeText: 4000,
  narrativeTotalText: 20000,
  narrativeMaxBlocks: 5,
  filingInstitutionNoteToFincen: 50,
  efilingPriorDocumentNumber: 14,
  accountNumberText: 40,
  otherOrganizationTypeText: 50,
  otherOrganizationSubtypeText: 50,
  otherSuspiciousActivityTypeText: 50,
  otherAssetSubtypeText: 50,
  otherPartyAssociationTypeText: 50,
  otherPartyIdentificationTypeText: 50,
  cyberEventTypeOtherText: 50,
  eventValueText: 4000,
  assetAttributeDescriptionText: 50,
  subjectRelationshipInstitutionTin: 25,
  ipAddressText: 45,
  originalAttachmentFileName: 255,
} as const

// ── Required Fields per Party Type ────────────────────────────────────────────

export const PARTY_TYPE_REQUIREMENTS: Record<number, {
  requiresName: boolean
  requiresAddress: boolean
  requiresIdentification: boolean
  requiresRegulator: boolean
  requiresOrgClassification: boolean
  requiresAssociation: boolean
  isEntity: boolean
  description: string
}> = {
  30: {
    requiresName: true,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: true,
    requiresOrgClassification: true,
    requiresAssociation: false,
    isEntity: true,
    description: 'Filing Institution — Requires legal name, address, TIN, primary regulator, and organization type.',
  },
  33: {
    requiresName: true,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: true,
    isEntity: false,
    description: 'Subject — Requires name (or allCriticalSubjectInfoUnavailable), address, identification, and at least one association.',
  },
  34: {
    requiresName: true,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: true,
    requiresOrgClassification: true,
    requiresAssociation: false,
    isEntity: true,
    description: 'FI Where Activity Occurred — Requires legal name, address, TIN, primary regulator, and organization type.',
  },
  46: {
    requiresName: false,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: false,
    isEntity: true,
    description: 'Branch Where Activity Occurred — Requires branch address and RSSD/ID number.',
  },
  8: {
    requiresName: true,
    requiresAddress: false,
    requiresIdentification: false,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: false,
    isEntity: false,
    description: 'Designated Contact Office — Requires contact name.',
  },
  35: {
    requiresName: true,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: false,
    isEntity: true,
    description: 'Transmitter — Requires legal name, address, and TCC.',
  },
  37: {
    requiresName: true,
    requiresAddress: false,
    requiresIdentification: false,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: false,
    isEntity: false,
    description: 'Transmitter Contact — Requires contact name.',
  },
  41: {
    requiresName: true,
    requiresAddress: true,
    requiresIdentification: true,
    requiresRegulator: false,
    requiresOrgClassification: false,
    requiresAssociation: false,
    isEntity: true,
    description: 'FI Where Account is Held — Requires institution name, address, and identification.',
  },
}
