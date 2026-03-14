-- ============================================================
-- FinCEN SAR XML Schema 2.0 — PostgreSQL Data Model
-- Based on FinCEN SAR XML Schema User Guide v1.6 (August 2021)
-- ============================================================
-- Hierarchy:
--   efiling_batch
--     └── activity
--           ├── activity_association
--           ├── activity_support_document
--           ├── party  (transmitter, filing institution, subject, etc.)
--           │     ├── party_name
--           │     ├── address
--           │     ├── phone_number
--           │     ├── party_identification
--           │     ├── organization_classification_type_subtype
--           │     ├── party_occupation_business
--           │     ├── electronic_address
--           │     ├── party_association  (subject→institution relationship + branch)
--           │     │     └── branch_party  (party type 46)
--           │     │           ├── branch_address
--           │     │           └── branch_party_identification
--           │     └── party_account_association  (subject→account)
--           │           └── account_holding_party  (party type 41)
--           │                 ├── account_holding_party_identification
--           │                 └── account
--           │                       └── account_party_association
--           ├── suspicious_activity
--           │     └── suspicious_activity_classification
--           ├── activity_ip_address
--           ├── cyber_event_indicators
--           ├── assets
--           ├── assets_attribute
--           └── activity_narrative_information
-- ============================================================

-- ============================================================
-- REFERENCE / LOOKUP TABLES
-- ============================================================

CREATE TABLE ref_party_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_party_type VALUES
    (35, 'Transmitter'),
    (37, 'Transmitter Contact'),
    (30, 'Filing Institution'),
    (8,  'Designated Contact Office'),
    (18, 'Law Enforcement Agency'),
    (19, 'Law Enforcement Name'),
    (34, 'Financial Institution Where Activity Occurred'),
    (46, 'Branch Where Activity Occurred'),
    (33, 'Subject'),
    (41, 'Financial Institution Where Account is Held');

CREATE TABLE ref_primary_regulator_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_primary_regulator_type VALUES
    (9,  'CFTC'),
    (1,  'Federal Reserve'),
    (2,  'FDIC'),
    (13, 'FHFA'),
    (7,  'IRS'),
    (3,  'NCUA'),
    (4,  'OCC'),
    (6,  'SEC'),
    (99, 'Not Applicable');

CREATE TABLE ref_party_name_type (
    code                    VARCHAR(3) PRIMARY KEY,
    description             VARCHAR(50) NOT NULL
);

INSERT INTO ref_party_name_type VALUES
    ('L',   'Legal'),
    ('DBA', 'Doing Business As'),
    ('AKA', 'Also Known As');

CREATE TABLE ref_phone_number_type (
    code                    VARCHAR(1) PRIMARY KEY,
    description             VARCHAR(30) NOT NULL
);

INSERT INTO ref_phone_number_type VALUES
    ('R', 'Residence (Home)'),
    ('W', 'Work'),
    ('M', 'Mobile'),
    ('F', 'Fax');

CREATE TABLE ref_party_identification_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_party_identification_type VALUES
    (4,   'Taxpayer Identification Number (TIN)'),
    (28,  'Transmitter Control Code (TCC)'),
    (2,   'Employer Identification Number (EIN)'),
    (1,   'Social Security Number (SSN)/ITIN'),
    (9,   'Foreign Taxpayer Identification Number'),
    (10,  'Central Registration Depository (CRD) number'),
    (11,  'Investment Adviser Registration (IARD) number'),
    (12,  'National Futures Association (NFA ID) number'),
    (14,  'Research Statistics Supervision and Discount (RSSD) number'),
    (13,  'Securities and Exchange Commission (SEC) number'),
    (32,  'National Association of Insurance Commissioners (NAIC) number'),
    (33,  'National Mortgage Licensing System (NMLS) number'),
    (29,  'Internal control/file number'),
    (5,   'Driver''s license/State ID'),
    (6,   'Passport'),
    (7,   'Alien registration'),
    (999, 'Other Identification');

CREATE TABLE ref_organization_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_organization_type VALUES
    (1,   'Casino/Card club'),
    (2,   'Depository institution'),
    (3,   'Insurance company'),
    (4,   'MSB (Money Service Business)'),
    (5,   'Securities/Futures'),
    (11,  'Loan or Finance Company'),
    (12,  'Housing GSE (Government Sponsored Enterprise)'),
    (999, 'Other');

CREATE TABLE ref_organization_subtype (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL,
    org_type_code           SMALLINT NOT NULL REFERENCES ref_organization_type(code)
);

INSERT INTO ref_organization_subtype VALUES
    (535,  'Clearing broker-securities', 5),
    (534,  'CPO/CTA', 5),
    (541,  'Execution-only broker securities', 5),
    (508,  'Futures Commission Merchant', 5),
    (504,  'Holding Company', 5),
    (513,  'Introducing broker-commodity', 5),
    (540,  'Introducing broker-securities', 5),
    (514,  'Investment adviser', 5),
    (539,  'Investment company', 5),
    (533,  'Retail foreign exchange dealer', 5),
    (542,  'Self-clearing broker-securities', 5),
    (528,  'SRO futures', 5),
    (529,  'SRO securities', 5),
    (503,  'Subsidiary of financial/bank holding company', 5),
    (5999, 'Other (Securities/Futures)', 5),
    (101,  'State casino', 1),
    (102,  'Tribal casino', 1),
    (103,  'Card club', 1),
    (1999, 'Other (Casino/Card club)', 1);

CREATE TABLE ref_electronic_address_type (
    code                    VARCHAR(1) PRIMARY KEY,
    description             VARCHAR(30) NOT NULL
);

INSERT INTO ref_electronic_address_type VALUES
    ('E', 'E-mail address'),
    ('U', 'Website (URL) address');

CREATE TABLE ref_suspicious_activity_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_suspicious_activity_type VALUES
    (1,  'Structuring'),
    (7,  'Terrorist financing'),
    (3,  'Fraud'),
    (12, 'Gaming activities'),
    (8,  'Money Laundering'),
    (4,  'Identification/Documentation'),
    (9,  'Other suspicious activities'),
    (5,  'Insurance'),
    (6,  'Securities/Futures/Options'),
    (10, 'Mortgage fraud'),
    (11, 'Cyber event');

CREATE TABLE ref_suspicious_activity_subtype (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(150) NOT NULL,
    activity_type_code      SMALLINT NOT NULL REFERENCES ref_suspicious_activity_type(code)
);

INSERT INTO ref_suspicious_activity_subtype VALUES
    -- Structuring (1)
    (111,  'Alters or cancels transaction to avoid BSA recordkeeping requirement', 1),
    (112,  'Alters or cancels transaction to avoid CTR requirement', 1),
    (106,  'Suspicious inquiry by customer regarding BSA reporting or recordkeeping requirements', 1),
    (113,  'Transaction(s) below BSA recordkeeping threshold', 1),
    (114,  'Transaction(s) below CTR threshold', 1),
    (1999, 'Other (Structuring)', 1),
    -- Terrorist financing (7)
    (701,  'Known or suspected terrorist/terrorist organization', 7),
    (7999, 'Other (Terrorist financing)', 7),
    -- Fraud (3)
    (320,  'ACH', 3),
    (322,  'Advance fee', 3),
    (321,  'Business loan', 3),
    (301,  'Check', 3),
    (304,  'Consumer Loan', 3),
    (305,  'Credit/Debit Card', 3),
    (323,  'Healthcare/Public or private health insurance', 3),
    (308,  'Mail', 3),
    (309,  'Mass-marketing', 3),
    (324,  'Ponzi scheme', 3),
    (310,  'Pyramid scheme', 3),
    (325,  'Securities fraud', 3),
    (312,  'Wire transfer', 3),
    (3999, 'Other (Fraud)', 3),
    -- Gaming activities (12)
    (1201,  'Chip walking', 12),
    (1202,  'Minimal gaming with large transactions', 12),
    (1203,  'Suspicious use of counter checks or markers', 12),
    (1204,  'Unknown source of chips', 12),
    (12999, 'Other (Gaming activities)', 12),
    -- Money Laundering (8)
    (801,  'Exchanges small bills for large bills or vice versa', 8),
    (824,  'Funnel account', 8),
    (820,  'Suspicious concerning the physical condition of funds', 8),
    (821,  'Suspicious concerning the source of funds', 8),
    (804,  'Suspicious designation of beneficiaries assignees or joint owners', 8),
    (805,  'Suspicious EFT/wire transfers', 8),
    (822,  'Suspicious exchange of currencies', 8),
    (806,  'Suspicious receipt of government payments/benefits', 8),
    (807,  'Suspicious use of multiple accounts', 8),
    (808,  'Suspicious use of noncash monetary instruments', 8),
    (809,  'Suspicious use of third-party transactions (straw-man)', 8),
    (823,  'Trade Based Money Laundering/Black Market Peso Exchange', 8),
    (812,  'Transaction out of pattern for customer(s)', 8),
    (8999, 'Other (Money Laundering)', 8),
    -- Identification/Documentation (4)
    (401,  'Changes spelling or arrangement of name', 4),
    (402,  'Multiple individuals with same or similar identities', 4),
    (403,  'Provided questionable or false documentation', 4),
    (409,  'Provided questionable or false identification', 4),
    (404,  'Refused or avoided request for documentation', 4),
    (405,  'Single individual with multiple identities', 4),
    (4999, 'Other (Identification/Documentation)', 4),
    -- Other suspicious activities (9)
    (920,  'Account takeover', 9),
    (901,  'Bribery or gratuity', 9),
    (917,  'Counterfeit Instrument (other)', 9),
    (921,  'Elder financial exploitation', 9),
    (903,  'Embezzlement/theft/disappearance of funds', 9),
    (904,  'Forgeries', 9),
    (926,  'Human smuggling', 9),
    (927,  'Human trafficking', 9),
    (905,  'Identity theft', 9),
    (922,  'Little or no concern for product performance penalties fees or tax consequences', 9),
    (924,  'Misuse of position or self-dealing', 9),
    (907,  'Suspected public/private corruption (domestic)', 9),
    (908,  'Suspected public/private corruption (foreign)', 9),
    (909,  'Suspicious use of informal value transfer system', 9),
    (910,  'Suspicious use of multiple locations', 9),
    (925,  'Transaction with no apparent economic business or lawful purpose', 9),
    (928,  'Transaction(s) involving foreign high risk jurisdiction', 9),
    (911,  'Two or more individuals working together', 9),
    (913,  'Unlicensed or unregistered MSB', 9),
    (9999, 'Other (Other suspicious activities)', 9),
    -- Insurance (5)
    (501,  'Excessive insurance', 5),
    (502,  'Excessive or unusual cash borrowing against policy/annuity', 5),
    (504,  'Proceeds sent to unrelated third party', 5),
    (505,  'Suspicious life settlement sales insurance (e.g. STOLI''s Viaticals)', 5),
    (506,  'Suspicious termination of policy or contract', 5),
    (507,  'Unclear or no insurable interest', 5),
    (5999, 'Other (Insurance)', 5),
    -- Securities/Futures/Options (6)
    (601,  'Insider trading', 6),
    (608,  'Market manipulation', 6),
    (603,  'Misappropriation', 6),
    (604,  'Unauthorized pooling', 6),
    (609,  'Wash trading', 6),
    (6999, 'Other (Securities/Futures/Options)', 6),
    -- Mortgage fraud (10)
    (1005,  'Application fraud', 10),
    (1001,  'Appraisal fraud', 10),
    (1006,  'Foreclosure/Short sale fraud', 10),
    (1003,  'Loan Modification fraud', 10),
    (1007,  'Origination fraud', 10),
    (10999, 'Other (Mortgage fraud)', 10),
    -- Cyber event (11)
    (1101,  'Against financial institution(s)', 11),
    (1102,  'Against financial institution customer(s)', 11),
    (11999, 'Other (Cyber event)', 11);

CREATE TABLE ref_asset_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_asset_type VALUES
    (5, 'Product type(s) involved in suspicious activity'),
    (6, 'Instrument type(s)/payment mechanism(s) involved in suspicious activity');

CREATE TABLE ref_asset_subtype (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL,
    asset_type_code         SMALLINT NOT NULL REFERENCES ref_asset_type(code)
);

INSERT INTO ref_asset_subtype VALUES
    -- Product types (AssetTypeID=5)
    (2,  'Bonds/Notes', 5),
    (3,  'Commercial mortgage', 5),
    (4,  'Commercial paper', 5),
    (5,  'Credit card', 5),
    (6,  'Debit card', 5),
    (46, 'Deposit Account', 5),
    (7,  'Forex transactions', 5),
    (8,  'Futures/Options on futures', 5),
    (9,  'Hedge fund', 5),
    (11, 'Home equity line of credit', 5),
    (10, 'Home equity loan', 5),
    (12, 'Insurance/Annuity products', 5),
    (47, 'Microcap securities', 5),
    (13, 'Mutual fund', 5),
    (14, 'Options on securities', 5),
    (16, 'Prepaid access', 5),
    (17, 'Residential mortgage', 5),
    (18, 'Security futures products', 5),
    (19, 'Stocks', 5),
    (20, 'Swap hybrid or other derivative', 5),
    (30, 'Other (Product type)', 5),
    -- Instrument/payment mechanism types (AssetTypeID=6)
    (31, 'Bank/Cashier''s check', 6),
    (32, 'Foreign currency', 6),
    (33, 'Funds transfer', 6),
    (34, 'Gaming instruments', 6),
    (35, 'Government payment', 6),
    (36, 'Money orders', 6),
    (37, 'Personal/Business check', 6),
    (38, 'Travelers checks', 6),
    (39, 'U.S. Currency', 6),
    (41, 'Other (Instrument/Payment mechanism)', 6);

CREATE TABLE ref_asset_attribute_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(50) NOT NULL
);

INSERT INTO ref_asset_attribute_type VALUES
    (2, 'Commodity type'),
    (3, 'Product/Instrument type'),
    (4, 'Market where traded'),
    (1, 'CUSIP number');

CREATE TABLE ref_cyber_event_type (
    code                    SMALLINT PRIMARY KEY,
    description             VARCHAR(100) NOT NULL
);

INSERT INTO ref_cyber_event_type VALUES
    (1,   'Command and control IP address'),
    (2,   'Command and control URL/domain'),
    (3,   'Malware MD5 SHA-1 or SHA-256'),
    (4,   'Media Access Control (MAC) Address'),
    (5,   'Port'),
    (6,   'Suspicious e-mail address'),
    (7,   'Suspicious file name'),
    (8,   'Suspicious IP address'),
    (9,   'Suspicious URL/domain'),
    (10,  'Targeted system'),
    (999, 'Other');

-- ============================================================
-- CORE TABLES
-- ============================================================

-- EFilingBatchXML (root container)
-- Represents a single batch submission to FinCEN.
CREATE TABLE efiling_batch (
    id                              BIGSERIAL PRIMARY KEY,
    activity_count                  INTEGER NOT NULL,
    total_amount                    NUMERIC(18,0),
    party_count                     INTEGER NOT NULL,
    activity_attachment_count       INTEGER NOT NULL DEFAULT 0,
    attachment_count                INTEGER NOT NULL DEFAULT 0,
    form_type_code                  VARCHAR(4) NOT NULL DEFAULT 'SARX',
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_form_type CHECK (form_type_code = 'SARX')
);

-- Activity — one FinCEN SAR document
CREATE TABLE activity (
    id                              BIGSERIAL PRIMARY KEY,
    efiling_batch_id                BIGINT NOT NULL REFERENCES efiling_batch(id),
    seq_num                         BIGINT NOT NULL,                    -- XML SeqNum attribute (unique per batch)
    efiling_prior_document_number   VARCHAR(14),                          -- Item 1e: 14-digit BSA ID or '00000000000000'
    filing_date                     DATE NOT NULL,                     -- Item 95: Date filed
    filing_institution_note_to_fincen VARCHAR(50),                    -- Item 2: Optional note to FinCEN
    bsa_identifier                  VARCHAR(14),                         -- Assigned by FinCEN on acknowledgement
    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_activity_seq_num UNIQUE (efiling_batch_id, seq_num)
);

-- ActivityAssociation — filing type (Item 1)
CREATE TABLE activity_association (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL UNIQUE REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    initial_report_indicator        BOOLEAN NOT NULL DEFAULT FALSE,    -- Item 1a
    corrects_amends_prior_report    BOOLEAN NOT NULL DEFAULT FALSE,    -- Item 1b
    continuing_activity_report      BOOLEAN NOT NULL DEFAULT FALSE,    -- Item 1c
    joint_report_indicator          BOOLEAN NOT NULL DEFAULT FALSE     -- Item 1d
);

-- ActivitySupportDocument — attachment metadata (Part V)
CREATE TABLE activity_support_document (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL UNIQUE REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    original_attachment_file_name   VARCHAR(255) NOT NULL              -- Must end in .csv
);

-- ============================================================
-- PARTY (all types at the activity level)
-- Party type codes: 35, 37, 30, 8, 18, 19, 34, 33
-- ============================================================

CREATE TABLE party (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    activity_party_type_code        SMALLINT NOT NULL REFERENCES ref_party_type(code),

    -- Used by Financial Institution Where Activity Occurred (34)
    loss_to_financial_amount        NUMERIC(15,0),                     -- Item 67
    no_branch_activity_involved     BOOLEAN,                           -- Item 69a
    pay_location_indicator          BOOLEAN,                           -- Item 56b
    primary_regulator_type_code     SMALLINT REFERENCES ref_primary_regulator_type(code), -- Items 52, 75
    selling_location_indicator      BOOLEAN,                           -- Item 56a
    selling_paying_location_indicator BOOLEAN,                        -- Item 56c

    -- Used by Subject (33)
    admission_confession_no         BOOLEAN,                           -- Item 23b
    admission_confession_yes        BOOLEAN,                           -- Item 23a
    all_critical_subject_info_unavailable BOOLEAN,                    -- Item 3b
    birth_date_unknown              BOOLEAN,                           -- Item 19a
    both_purchaser_sender_payee_receiver BOOLEAN,                     -- Item 28c
    female_gender_indicator         BOOLEAN,                           -- Item 8b
    individual_birth_date           DATE,                              -- Item 19
    male_gender_indicator           BOOLEAN,                           -- Item 8a
    no_known_account_involved       BOOLEAN,                           -- Item 27a
    party_as_entity_organization    BOOLEAN,                           -- Item 3a
    payee_receiver_indicator        BOOLEAN,                           -- Item 28b
    purchaser_sender_indicator      BOOLEAN,                           -- Item 28a
    unknown_gender_indicator        BOOLEAN,                           -- Item 8c

    -- Used by LE Contact Name (19)
    contact_date                    DATE,                              -- Item 92: LE contact date

    -- Used by Financial Institution Where Account is Held (41) via NonUSFinancialInstitutionIndicator
    non_us_financial_institution    BOOLEAN,                           -- Item 27b

    created_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_party_seq_num UNIQUE (activity_id, seq_num)
);

CREATE INDEX idx_party_activity_id ON party(activity_id);
CREATE INDEX idx_party_type_code ON party(activity_party_type_code);

-- ============================================================
-- PARTY NAME (Items 4–7, 9, 57, 58, 76, 87, 89, 90, 93)
-- ============================================================

CREATE TABLE party_name (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,
    party_name_type_code            VARCHAR(3) NOT NULL REFERENCES ref_party_name_type(code),

    -- For non-Subject parties (Transmitter, Filing Institution, etc.)
    raw_party_full_name             VARCHAR(150),

    -- For Subject legal name (Items 4–7)
    entity_last_name_unknown        BOOLEAN,                           -- Item 4a
    first_name_unknown              BOOLEAN,                           -- Item 5a
    raw_entity_individual_last_name VARCHAR(150),                      -- Item 4
    raw_individual_first_name       VARCHAR(35),                       -- Item 5
    raw_individual_middle_name      VARCHAR(35),                       -- Item 6
    raw_individual_name_suffix_text VARCHAR(35),                       -- Item 7

    CONSTRAINT uq_party_name_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_party_name_party_id ON party_name(party_id);

-- ============================================================
-- ADDRESS (Items 11–15, 61–65, 82–86)
-- ============================================================

CREATE TABLE address (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,

    -- Unknown indicators (for Subject and Financial Institution Where Activity Occurred only)
    city_unknown                    BOOLEAN,                           -- Items 12a, 62a
    country_code_unknown            BOOLEAN,                           -- Items 15a, 65a
    state_code_unknown              BOOLEAN,                           -- Item 13a (Subject only)
    street_address_unknown          BOOLEAN,                           -- Items 11a, 61a
    zip_code_unknown                BOOLEAN,                           -- Items 14a, 64a

    -- Address fields
    raw_street_address1             VARCHAR(100),                      -- Items 11, 61, 69, 82
    raw_city                        VARCHAR(50),                       -- Items 12, 62, 71, 83
    raw_state_code                  VARCHAR(2),                           -- Items 13, 63, 72, 84
    raw_zip_code                    VARCHAR(9),                        -- Items 14, 64, 73, 85
    raw_country_code                VARCHAR(2),                           -- Items 15, 65, 74, 86 (ISO 3166-2)

    CONSTRAINT uq_address_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_address_party_id ON address(party_id);

-- ============================================================
-- PHONE NUMBER (Items 20–21, 91, 94)
-- ============================================================

CREATE TABLE phone_number (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,
    phone_number_text               VARCHAR(16),                       -- Items 21, 91, 94; transmitter is 10 digits
    phone_number_extension          VARCHAR(6),                        -- Items 21a, 91a, 94a
    phone_number_type_code          VARCHAR(1) REFERENCES ref_phone_number_type(code), -- Item 20 (Subject only)

    CONSTRAINT uq_phone_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_phone_party_id ON phone_number(party_id);

-- ============================================================
-- PARTY IDENTIFICATION (Items 16–18, 55, 59–60, 66, 77–78, 81, 88)
-- ============================================================

CREATE TABLE party_identification (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,
    party_identification_type_code  SMALLINT REFERENCES ref_party_identification_type(code),
    party_identification_number     VARCHAR(25),                       -- Number text (TCC=8, TIN=25, ID=24, ICN=20)
    tin_unknown                     BOOLEAN,                           -- Items 16a, 59a (Subject, FI Activity Occurred)

    -- Subject form-of-identification fields (Item 18)
    identification_present_unknown  BOOLEAN,                           -- Item 18a
    other_issuer_country            VARCHAR(2),                           -- Item 18g (ISO 3166-2)
    other_issuer_state              VARCHAR(2),                           -- Item 18f
    other_party_identification_type_text VARCHAR(50),                 -- Item 18z

    CONSTRAINT uq_party_id_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_party_identification_party_id ON party_identification(party_id);

-- ============================================================
-- ORGANIZATION CLASSIFICATION TYPE/SUBTYPE (Items 51, 53–54, 79–80)
-- ============================================================

CREATE TABLE organization_classification_type_subtype (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,
    organization_type_id            SMALLINT NOT NULL REFERENCES ref_organization_type(code), -- Item 51/79
    organization_subtype_id         SMALLINT REFERENCES ref_organization_subtype(code),       -- Items 53–54/80
    other_organization_type_text    VARCHAR(50),                       -- Items 51z, 79z
    other_organization_subtype_text VARCHAR(50),                       -- Items 53z, 54z, 80z

    CONSTRAINT uq_org_class_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_org_class_party_id ON organization_classification_type_subtype(party_id);

-- ============================================================
-- PARTY OCCUPATION / BUSINESS (Item 10, Subject only)
-- ============================================================

CREATE TABLE party_occupation_business (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL UNIQUE REFERENCES party(id),  -- max 1 per subject
    seq_num                         BIGINT NOT NULL,
    naics_code                      VARCHAR(6),                        -- Item 10a: 3–6 digit NAICS
    occupation_business_text        VARCHAR(50)                        -- Item 10
);

-- ============================================================
-- ELECTRONIC ADDRESS (Item 22, Subject only)
-- ============================================================

CREATE TABLE electronic_address (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,
    electronic_address_type_code    VARCHAR(1) NOT NULL REFERENCES ref_electronic_address_type(code), -- E or U
    electronic_address_text         VARCHAR(517) NOT NULL,            -- Email ≤50 chars; URL ≤517 chars

    CONSTRAINT uq_electronic_address_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_electronic_address_party_id ON electronic_address(party_id);

-- ============================================================
-- PARTY ASSOCIATION — Subject→Institution relationship (Items 24–26)
-- AND Financial Institution→Branch link (Items 68–74)
-- ============================================================

CREATE TABLE party_association (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL REFERENCES party(id),
    seq_num                         BIGINT NOT NULL,

    -- Subject relationship fields (Items 24–26, party type 33)
    subject_relationship_institution_tin VARCHAR(25),                 -- Item 24a
    accountant_indicator            BOOLEAN,                           -- Item 24b
    agent_indicator                 BOOLEAN,                           -- Item 24c
    appraiser_indicator             BOOLEAN,                           -- Item 24d
    attorney_indicator              BOOLEAN,                           -- Item 24e
    borrower_indicator              BOOLEAN,                           -- Item 24f
    customer_indicator              BOOLEAN,                           -- Item 24g
    director_indicator              BOOLEAN,                           -- Item 24h
    employee_indicator              BOOLEAN,                           -- Item 24i
    no_relationship_to_institution  BOOLEAN,                           -- Item 24j
    officer_indicator               BOOLEAN,                           -- Item 24k
    owner_shareholder_indicator     BOOLEAN,                           -- Item 24l
    other_relationship_indicator    BOOLEAN,                           -- Item 24z indicator
    other_party_association_type_text VARCHAR(50),                    -- Item 24z description
    relationship_continues          BOOLEAN,                           -- Item 25a
    terminated_indicator            BOOLEAN,                           -- Item 25b
    suspended_barred_indicator      BOOLEAN,                           -- Item 25c
    resigned_indicator              BOOLEAN,                           -- Item 25d
    action_taken_date               DATE,                              -- Item 26

    CONSTRAINT uq_party_assoc_seq_num UNIQUE (party_id, seq_num)
);

CREATE INDEX idx_party_association_party_id ON party_association(party_id);

-- Branch party (party type 46) — nested inside PartyAssociation of a Financial Institution (34)
CREATE TABLE branch_party (
    id                              BIGSERIAL PRIMARY KEY,
    party_association_id            BIGINT NOT NULL REFERENCES party_association(id),
    seq_num                         BIGINT NOT NULL,
    activity_party_type_code        SMALLINT NOT NULL DEFAULT 46,

    -- Branch transaction role (Items 68a–c)
    selling_location_indicator      BOOLEAN,
    pay_location_indicator          BOOLEAN,
    selling_paying_location_indicator BOOLEAN,

    CONSTRAINT uq_branch_party_seq_num UNIQUE (party_association_id, seq_num),
    CONSTRAINT chk_branch_type_code CHECK (activity_party_type_code = 46)
);

-- Branch address (Items 69, 71–74)
CREATE TABLE branch_address (
    id                              BIGSERIAL PRIMARY KEY,
    branch_party_id                 BIGINT NOT NULL REFERENCES branch_party(id),
    seq_num                         BIGINT NOT NULL,
    raw_street_address1             VARCHAR(100),                      -- Item 69
    raw_city                        VARCHAR(50),                       -- Item 71
    raw_state_code                  VARCHAR(2),                           -- Item 72
    raw_zip_code                    VARCHAR(9),                        -- Item 73
    raw_country_code                VARCHAR(2) NOT NULL,                  -- Item 74 (required)

    CONSTRAINT uq_branch_address_seq_num UNIQUE (branch_party_id, seq_num)
);

-- Branch party identification — RSSD number only (Item 70)
CREATE TABLE branch_party_identification (
    id                              BIGSERIAL PRIMARY KEY,
    branch_party_id                 BIGINT NOT NULL UNIQUE REFERENCES branch_party(id), -- max 1 per branch
    seq_num                         BIGINT NOT NULL,
    party_identification_number     VARCHAR(20) NOT NULL,              -- RSSD number
    party_identification_type_code  SMALLINT NOT NULL DEFAULT 14,      -- Always 14 (RSSD)
    CONSTRAINT chk_branch_id_type CHECK (party_identification_type_code = 14)
);

-- ============================================================
-- PARTY ACCOUNT ASSOCIATION — Subject→Account (Item 27)
-- ============================================================

-- Top-level container linking a Subject to its associated accounts (max 1 per Subject)
CREATE TABLE party_account_association (
    id                              BIGSERIAL PRIMARY KEY,
    party_id                        BIGINT NOT NULL UNIQUE REFERENCES party(id), -- Subject only
    seq_num                         BIGINT NOT NULL,
    party_account_association_type_code SMALLINT NOT NULL DEFAULT 7,  -- Always 7
    CONSTRAINT chk_paa_type_code CHECK (party_account_association_type_code = 7)
);

-- Financial Institution Where Account is Held (party type 41, nested in PartyAccountAssociation)
CREATE TABLE account_holding_party (
    id                              BIGSERIAL PRIMARY KEY,
    party_account_association_id    BIGINT NOT NULL REFERENCES party_account_association(id),
    seq_num                         BIGINT NOT NULL,
    activity_party_type_code        SMALLINT NOT NULL DEFAULT 41,
    non_us_financial_institution    BOOLEAN,                           -- Item 27b

    CONSTRAINT uq_ahp_seq_num UNIQUE (party_account_association_id, seq_num),
    CONSTRAINT chk_ahp_type_code CHECK (activity_party_type_code = 41)
);

-- Financial institution TIN for account-holding party (Item 27c)
CREATE TABLE account_holding_party_identification (
    id                              BIGSERIAL PRIMARY KEY,
    account_holding_party_id        BIGINT NOT NULL UNIQUE REFERENCES account_holding_party(id),
    seq_num                         BIGINT NOT NULL,
    party_identification_number     VARCHAR(25),                       -- Item 27c: TIN
    party_identification_type_code  SMALLINT NOT NULL DEFAULT 4,       -- Always 4 (TIN)
    CONSTRAINT chk_ahpi_type_code CHECK (party_identification_type_code = 4)
);

-- Account (Items 27d–e)
CREATE TABLE account (
    id                              BIGSERIAL PRIMARY KEY,
    account_holding_party_id        BIGINT NOT NULL REFERENCES account_holding_party(id),
    seq_num                         BIGINT NOT NULL,
    account_number_text             VARCHAR(40) NOT NULL,              -- Item 27d

    CONSTRAINT uq_account_seq_num UNIQUE (account_holding_party_id, seq_num)
);

CREATE INDEX idx_account_ahp_id ON account(account_holding_party_id);

-- Account-level PartyAccountAssociation — links account to institution (Item 27e)
CREATE TABLE account_party_association (
    id                              BIGSERIAL PRIMARY KEY,
    account_id                      BIGINT NOT NULL UNIQUE REFERENCES account(id),
    seq_num                         BIGINT NOT NULL,
    account_closed_indicator        BOOLEAN,                           -- Item 27e
    party_account_association_type_code SMALLINT NOT NULL DEFAULT 5,  -- Always 5
    CONSTRAINT chk_apa_type_code CHECK (party_account_association_type_code = 5)
);

-- ============================================================
-- SUSPICIOUS ACTIVITY (Items 29–31, Part II)
-- ============================================================

CREATE TABLE suspicious_activity (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL UNIQUE REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    amount_unknown                  BOOLEAN,                           -- Item 29a
    no_amount_involved              BOOLEAN,                           -- Item 29b
    total_suspicious_amount         NUMERIC(15,0),                    -- Item 29 (whole USD, no leading zeros)
    suspicious_activity_from_date   DATE NOT NULL,                    -- Item 30a
    suspicious_activity_to_date     DATE,                             -- Item 30b
    cumulative_total_violation_amount NUMERIC(15,0)                   -- Item 31 (continuing activity reports)
);

-- Suspicious Activity Classification (Items 32–42)
CREATE TABLE suspicious_activity_classification (
    id                              BIGSERIAL PRIMARY KEY,
    suspicious_activity_id          BIGINT NOT NULL REFERENCES suspicious_activity(id),
    seq_num                         BIGINT NOT NULL,
    suspicious_activity_type_id     SMALLINT NOT NULL REFERENCES ref_suspicious_activity_type(code),
    suspicious_activity_subtype_id  SMALLINT NOT NULL REFERENCES ref_suspicious_activity_subtype(code),
    other_suspicious_activity_type_text VARCHAR(50),                  -- Items 32z–42z

    CONSTRAINT uq_sac_seq_num UNIQUE (suspicious_activity_id, seq_num)
);

CREATE INDEX idx_sac_sa_id ON suspicious_activity_classification(suspicious_activity_id);

-- ============================================================
-- ACTIVITY IP ADDRESS (Item 43)
-- ============================================================

CREATE TABLE activity_ip_address (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    ip_address_text                 VARCHAR(45) NOT NULL,             -- Item 43: IPv4 or IPv6
    ip_address_date                 DATE,                             -- Item 43a
    ip_address_timestamp            TIME,                             -- Item 43b (UTC HH:MM:SS)

    CONSTRAINT uq_ip_address_seq_num UNIQUE (activity_id, seq_num)
);

CREATE INDEX idx_ip_address_activity_id ON activity_ip_address(activity_id);

-- ============================================================
-- CYBER EVENT INDICATORS (Item 44)
-- ============================================================

CREATE TABLE cyber_event_indicators (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    cyber_event_indicators_type_code SMALLINT NOT NULL REFERENCES ref_cyber_event_type(code), -- Item 44
    event_value_text                TEXT NOT NULL,                     -- Item 44 value (≤4000 chars)
    cyber_event_date                DATE,                              -- Items 44a2, 44h2
    cyber_event_timestamp           TIME,                              -- Items 44a2, 44h2 (UTC HH:MM:SS)
    cyber_event_type_other_text     VARCHAR(50),                       -- Item 44z2

    CONSTRAINT uq_cyber_event_seq_num UNIQUE (activity_id, seq_num)
);

CREATE INDEX idx_cyber_event_activity_id ON cyber_event_indicators(activity_id);

-- ============================================================
-- ASSETS (Items 45–46)
-- ============================================================

CREATE TABLE assets (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    asset_type_id                   SMALLINT NOT NULL REFERENCES ref_asset_type(code),     -- Items 45, 46
    asset_subtype_id                SMALLINT NOT NULL REFERENCES ref_asset_subtype(code),  -- Items 45a–t,z / 46a–i,z
    other_asset_subtype_text        VARCHAR(50),                       -- Items 45z, 46z

    CONSTRAINT uq_assets_seq_num UNIQUE (activity_id, seq_num)
);

CREATE INDEX idx_assets_activity_id ON assets(activity_id);

-- ============================================================
-- ASSETS ATTRIBUTE (Items 47–50)
-- ============================================================

CREATE TABLE assets_attribute (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    asset_attribute_type_id         SMALLINT NOT NULL REFERENCES ref_asset_attribute_type(code), -- Items 47–50
    asset_attribute_description_text VARCHAR(50) NOT NULL,            -- Commodity/Product/Market/CUSIP

    CONSTRAINT uq_assets_attr_seq_num UNIQUE (activity_id, seq_num)
);

CREATE INDEX idx_assets_attr_activity_id ON assets_attribute(activity_id);

-- ============================================================
-- ACTIVITY NARRATIVE INFORMATION (Part V Narrative)
-- Up to 5 blocks of 4000 chars each = 20,000 chars total
-- ============================================================

CREATE TABLE activity_narrative_information (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL REFERENCES activity(id),
    seq_num                         BIGINT NOT NULL,
    narrative_sequence_number       SMALLINT NOT NULL,                 -- 1–5
    narrative_text                  VARCHAR(4000) NOT NULL,

    CONSTRAINT uq_narrative_seq_num UNIQUE (activity_id, seq_num),
    CONSTRAINT uq_narrative_block   UNIQUE (activity_id, narrative_sequence_number),
    CONSTRAINT chk_narrative_seq    CHECK (narrative_sequence_number BETWEEN 1 AND 5)
);

-- ============================================================
-- ACKNOWLEDGEMENT TABLES (FinCEN response)
-- ============================================================

CREATE TABLE efiling_activity_acknowledgement (
    id                              BIGSERIAL PRIMARY KEY,
    activity_id                     BIGINT NOT NULL UNIQUE REFERENCES activity(id),
    bsa_identifier                  VARCHAR(14),
    status_code                     VARCHAR(1),
    acknowledged_at                 TIMESTAMPTZ
);

CREATE TABLE efiling_activity_error (
    id                              BIGSERIAL PRIMARY KEY,
    activity_acknowledgement_id     BIGINT NOT NULL REFERENCES efiling_activity_acknowledgement(id),
    seq_num                         BIGINT NOT NULL,
    error_context_text              VARCHAR(4000),
    error_element_name_text         VARCHAR(512),
    error_level_text                VARCHAR(50),                       -- 'WARN' or 'REJECT'
    error_text                      VARCHAR(525),
    error_type_code                 VARCHAR(50),

    CONSTRAINT uq_error_seq_num UNIQUE (activity_acknowledgement_id, seq_num)
);

-- ============================================================
-- UPDATED_AT TRIGGER FUNCTION
-- ============================================================

CREATE OR REPLACE FUNCTION trigger_set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER set_updated_at_efiling_batch
    BEFORE UPDATE ON efiling_batch
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_activity
    BEFORE UPDATE ON activity
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

CREATE TRIGGER set_updated_at_party
    BEFORE UPDATE ON party
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

-- ============================================================
-- COMMENTS
-- ============================================================

COMMENT ON TABLE efiling_batch IS
    'Root container representing one FinCEN SAR XML batch submission (EFilingBatchXML).';
COMMENT ON TABLE activity IS
    'One FinCEN SAR document within a batch (Activity element). Corresponds to a complete SAR filing.';
COMMENT ON TABLE activity_association IS
    'Filing type flags for the SAR (Item 1: Initial, Corrects/Amends, Continuing, Joint).';
COMMENT ON TABLE activity_support_document IS
    'Attachment metadata (Part V). Points to a CSV attachment file.';
COMMENT ON TABLE party IS
    'Any person or entity associated with the SAR. Discriminated by activity_party_type_code.
     Type 35=Transmitter, 37=Transmitter Contact, 30=Filing Institution, 8=Contact Office,
     18=LE Agency, 19=LE Name, 34=FI Where Activity Occurred, 33=Subject.';
COMMENT ON TABLE party_name IS
    'One name entry for a party. Type L=Legal, DBA=Doing Business As, AKA=Also Known As.
     For Subject legal names the individual name parts (first/middle/last/suffix) are used.
     For all other party types raw_party_full_name is used.';
COMMENT ON TABLE address IS
    'Address for Transmitter (35), Filing Institution (30), FI Where Activity Occurred (34), Subject (33).
     Unknown indicators replace the corresponding text fields when data is unavailable.';
COMMENT ON TABLE phone_number IS
    'Phone numbers for Transmitter (35), Contact Office (8), LE Contact Name (19), Subject (33).';
COMMENT ON TABLE party_identification IS
    'Identification numbers for Transmitter (TCC+TIN), Filing Institution (TIN, CRD/IARD/etc., ICN),
     FI Where Activity Occurred (TIN, FI ID, ICN), and Subject (TIN, Form of ID).';
COMMENT ON TABLE organization_classification_type_subtype IS
    'Institution type and subtype for Filing Institution (30) and FI Where Activity Occurred (34).';
COMMENT ON TABLE party_occupation_business IS
    'Occupation or type of business for Subject (33). Items 10 and 10a.';
COMMENT ON TABLE electronic_address IS
    'Email addresses and website URLs for Subject (33). Item 22.';
COMMENT ON TABLE party_association IS
    'For Subject (33): relationship to an institution (Items 24–26).
     For FI Where Activity Occurred (34): container linking to branch_party records (Item 68–74).';
COMMENT ON TABLE branch_party IS
    'Branch where activity occurred (party type 46). Nested inside party_association of a FI (34).';
COMMENT ON TABLE party_account_association IS
    'Container linking a Subject (33) to their associated financial accounts. Item 27.';
COMMENT ON TABLE account_holding_party IS
    'Financial institution where an account is held (party type 41). Item 27b–e.';
COMMENT ON TABLE account IS
    'Individual account number at an account_holding_party institution. Item 27d.';
COMMENT ON TABLE suspicious_activity IS
    'Suspicious activity details: amounts and date ranges. Part II, Items 29–31.';
COMMENT ON TABLE suspicious_activity_classification IS
    'Type and subtype of suspicious activity (Items 32–42). Up to 99 per SAR.';
COMMENT ON TABLE activity_ip_address IS
    'IP addresses of the subject''s electronic contact with the institution. Item 43.';
COMMENT ON TABLE cyber_event_indicators IS
    'Cyber event indicators (command-and-control IPs, malware hashes, URLs, etc.). Item 44.';
COMMENT ON TABLE assets IS
    'Product types (Item 45) and instrument/payment mechanisms (Item 46) involved in the SAR.';
COMMENT ON TABLE assets_attribute IS
    'Commodity types, product descriptions, markets, and CUSIP numbers. Items 47–50.';
COMMENT ON TABLE activity_narrative_information IS
    'Narrative text for the SAR (Part V). Split into up to 5 blocks of 4000 chars each.';
COMMENT ON TABLE efiling_activity_acknowledgement IS
    'FinCEN acknowledgement for a submitted SAR activity, including the assigned BSA Identifier.';
COMMENT ON TABLE efiling_activity_error IS
    'Individual error or warning entries returned in the FinCEN acknowledgement file.';
