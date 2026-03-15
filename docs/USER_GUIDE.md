# FinCEN SAR Platform — User Guide

> A comprehensive guide for using the FinCEN Suspicious Activity Report (SAR) e-filing platform.

---

## Table of Contents

1. [Overview](#1-overview)
2. [Getting Started](#2-getting-started)
   - [System Requirements](#system-requirements)
   - [Starting the Application](#starting-the-application)
   - [Logging In](#logging-in)
3. [User Roles & Permissions](#3-user-roles--permissions)
4. [Dashboard](#4-dashboard)
5. [Managing E-Filing Batches](#5-managing-e-filing-batches)
   - [Creating a Batch](#creating-a-batch)
   - [Browsing Batches](#browsing-batches)
   - [Deleting a Batch](#deleting-a-batch)
6. [Creating & Editing Activities (SAR Documents)](#6-creating--editing-activities-sar-documents)
   - [Activity Wizard Overview](#activity-wizard-overview)
   - [Step 1 — Activity Header](#step-1--activity-header)
   - [Step 2 — Filing Type](#step-2--filing-type)
   - [Step 3 — Support Document](#step-3--support-document)
   - [Step 4 — Parties](#step-4--parties)
   - [Step 5 — Suspicious Activity](#step-5--suspicious-activity)
   - [Step 6 — IP Addresses](#step-6--ip-addresses)
   - [Step 7 — Cyber Events](#step-7--cyber-events)
   - [Step 8 — Assets](#step-8--assets)
   - [Step 9 — Narratives](#step-9--narratives)
7. [Filing Workflow](#7-filing-workflow)
   - [Workflow Diagram](#workflow-diagram)
   - [Status Transitions](#status-transitions)
   - [Submission Validation](#submission-validation)
8. [Generating BSA XML](#8-generating-bsa-xml)
9. [FinCEN Reference Data](#9-fincen-reference-data)
   - [Party Types](#party-types)
   - [Identification Types](#identification-types)
   - [Regulator Codes](#regulator-codes)
   - [Organization Types](#organization-types)
   - [Suspicious Activity Types](#suspicious-activity-types)
   - [Cyber Event Types](#cyber-event-types)
   - [Asset & Instrument Types](#asset--instrument-types)
   - [Field Length Limits](#field-length-limits)
10. [API Reference](#10-api-reference)
    - [Authentication](#authentication-endpoints)
    - [Batches](#batch-endpoints)
    - [Workflow](#workflow-endpoints)
    - [Activities](#activity-endpoints)
    - [Parties](#party-endpoints)
    - [Suspicious Activity](#suspicious-activity-endpoints)
    - [IP Addresses & Cyber Events](#ip-address--cyber-event-endpoints)
    - [Assets](#asset-endpoints)
    - [Narratives](#narrative-endpoints)
    - [Dashboard](#dashboard-endpoints)
11. [Deployment](#11-deployment)
    - [Docker Compose (Recommended)](#docker-compose-recommended)
    - [Environment Variables](#environment-variables)
    - [Running Locally (Development)](#running-locally-development)
12. [Troubleshooting](#12-troubleshooting)

---

## 1. Overview

The FinCEN SAR Platform is a web application for creating, managing, and submitting Suspicious Activity Reports (SARs) to the Financial Crimes Enforcement Network (FinCEN). It implements the full FinCEN BSA e-filing specification including:

- **E-Filing Batches** — containers that group one or more SAR documents for submission
- **Activities** — individual SAR documents with all required FinCEN fields
- **Parties** — filing institutions, subjects, transmitters, and other involved entities
- **Suspicious Activity Details** — classification of the suspicious activity being reported
- **Narratives** — free-text descriptions of the activity (Part V of the SAR form)
- **BSA XML Generation** — export SARs in FinCEN-compliant XML format

The platform enforces FinCEN schema requirements including field lengths, allowed values, party-type-specific rules, and submission completeness checks.

---

## 2. Getting Started

### System Requirements

| Component | Requirement |
|-----------|-------------|
| Docker & Docker Compose | v20+ (for containerized deployment) |
| Java | 21+ (for local development) |
| Node.js | 22+ (for frontend development) |
| PostgreSQL | 16+ (if running database separately) |
| Browser | Chrome, Firefox, Edge, or Safari (latest) |

### Starting the Application

The fastest way to start the platform is with Docker Compose:

```bash
# Clone the repository
git clone https://github.com/danalderslade/FinCEN_SAR.git
cd FinCEN_SAR

# Start all services (PostgreSQL, API, Web UI)
docker compose up --build -d

# Verify all containers are healthy
docker compose ps
```

Once started, the following services are available:

| Service | URL | Description |
|---------|-----|-------------|
| Web UI | http://localhost:3000 | Browser-based user interface |
| REST API | http://localhost:8080/api/v1 | Backend API |
| PostgreSQL | localhost:5432 | Database (internal) |

### Logging In

Navigate to **http://localhost:3000** in your browser. You will see the login screen.

**Demo Accounts:**

| Username | Password | Role | What You Can Do |
|----------|----------|------|-----------------|
| `admin` | `Admin123!` | ADMIN | Everything — create users, manage all data, all workflow transitions |
| `analyst` | `Admin123!` | ANALYST | Create and edit batches and activities |
| `reviewer` | `Admin123!` | REVIEWER | Submit batches for review, return to draft |
| `approver` | `Admin123!` | APPROVER | Submit to FinCEN, acknowledge, reject |

Enter your username and password, then click **Sign In**. Your session token lasts 24 hours.

---

## 3. User Roles & Permissions

The platform uses role-based access control (RBAC). Every user is assigned exactly one role.

| Capability | ANALYST | REVIEWER | APPROVER | ADMIN |
|------------|:-------:|:--------:|:--------:|:-----:|
| Create / edit batches | ✓ | ✓ | ✓ | ✓ |
| Create / edit activities | ✓ | ✓ | ✓ | ✓ |
| Add / remove parties | ✓ | ✓ | ✓ | ✓ |
| Add / edit narratives | ✓ | ✓ | ✓ | ✓ |
| Delete batches / activities | ✓ | ✓ | ✓ | ✓ |
| Submit for Review (DRAFT → REVIEW) | — | ✓ | ✓ | ✓ |
| Return to Draft (REVIEW → DRAFT) | — | ✓ | ✓ | ✓ |
| Submit to FinCEN (REVIEW → SUBMITTED) | — | — | ✓ | ✓ |
| Acknowledge (SUBMITTED → ACKNOWLEDGED) | — | — | ✓ | ✓ |
| Reject (SUBMITTED → REJECTED) | — | — | ✓ | ✓ |
| Register new users | — | — | — | ✓ |

> **Tip:** Start with the `admin` account for full access while learning the system.

---

## 4. Dashboard

After logging in, you land on the **Dashboard**. It displays:

- **Summary Metrics** — Total batches, total activities, total parties
- **Status Counts** — Number of batches in each filing status (Draft, Review, Submitted, Acknowledged, Rejected)
- **Recent Batches** — Quick access to your most recent work

Use the **sidebar navigation** on the left to move between Dashboard and Batches.

---

## 5. Managing E-Filing Batches

A **batch** is the top-level container for a FinCEN e-filing submission. Each batch can contain one or more SAR activities (individual reports).

### Creating a Batch

1. Navigate to **Batches** in the sidebar
2. Click **Create Batch**
3. The batch is created in **DRAFT** status
4. You are taken to the batch detail page where you can add activities

### Browsing Batches

The batch list page supports:

- **Pagination** — 20 batches per page, with page navigation
- **Status Filter** — Filter by filing status (Draft, Review, Submitted, Acknowledged, Rejected)
- **Sorting** — Sort by creation date (newest first by default)

Each batch row shows:
- Batch ID
- Filing status (color-coded badge)
- Number of activities
- Creation date
- Actions (view, delete)

### Deleting a Batch

Click the **Delete** button on a batch to remove it along with all its activities and child data. This action is permanent and cascades to all nested records.

---

## 6. Creating & Editing Activities (SAR Documents)

Each **activity** represents a single Suspicious Activity Report (SAR). Activities are created within a batch and edited through a step-by-step wizard.

### Activity Wizard Overview

From the batch detail page, click **Create Activity** to add a new SAR. Then click **Edit** or the activity row to open it in the **Activity Wizard**.

The wizard has **9 steps**, matching the sections of the FinCEN SAR form:

| Step | Section | FinCEN Items | Description |
|------|---------|--------------|-------------|
| 1 | Activity Header | 95, 1e, 2 | Filing date, prior document number, notes |
| 2 | Filing Type | 1a–1d | Initial, corrects/amends, continuing, joint |
| 3 | Support Document | Part V | CSV attachment filename |
| 4 | Parties | Parts I, III, IV | All involved parties (institution, subject, etc.) |
| 5 | Suspicious Activity | Part II (29–42) | Activity types, amounts, date range |
| 6 | IP Addresses | Item 43 | Internet protocol addresses |
| 7 | Cyber Events | Item 44 | Cyber event indicators |
| 8 | Assets | Items 45–50 | Product types and instruments |
| 9 | Narratives | Part V | Free-text narrative (up to 5 blocks) |

You can navigate between steps freely — there is no requirement to complete them in order. Data is saved as you go.

> **Important:** Activities in **SUBMITTED** or **ACKNOWLEDGED** status cannot be edited. Return the batch to DRAFT status to make changes.

---

### Step 1 — Activity Header

| Field | Required | Max Length | Description |
|-------|----------|-----------|-------------|
| Filing Date | Yes | — | Date of the SAR filing (cannot be a future date) |
| Prior Document Number | No | 14 chars | FinCEN BSA ID of a prior filing being corrected/amended (Item 1e) |
| Note to FinCEN | No | 50 chars | Optional message to FinCEN (Item 2) |

---

### Step 2 — Filing Type

Select one or more filing type indicators:

| Field | FinCEN Item | Description |
|-------|-------------|-------------|
| Initial Report | 1a | First-time filing for this activity |
| Corrects/Amends Prior Report | 1b | Corrects a previously filed SAR |
| Continuing Activity Report | 1c | Ongoing monitoring of same activity |
| Joint Report | 1d | Filed jointly with another institution |

If **Corrects/Amends** is selected, you should also provide the Prior Document Number in Step 1.

---

### Step 3 — Support Document

Attach a CSV support document to the filing.

| Field | Required | Constraint | Description |
|-------|----------|-----------|-------------|
| Attachment Filename | No | Must end in `.csv`, max 255 chars | Original filename of the CSV attachment |

---

### Step 4 — Parties

This is the most complex step. Parties represent all people and organizations involved in the SAR. Each party has a **type code** that determines what information is required.

#### Adding a Party

1. Click **Add Party**
2. Select the **party type** from the dropdown
3. Fill in the required information based on the party type
4. Save the party

#### Party Types and Requirements

| Type | Name | Required Data |
|------|------|---------------|
| **30** | Filing Institution | Legal name, address, TIN (EIN), primary regulator, organization classification |
| **33** | Subject | Legal name OR "all critical info unavailable" flag, address, identification |
| **34** | FI Where Activity Occurred | Legal name, address, TIN, primary regulator, organization classification |
| **35** | Transmitter | Legal name, address, TCC or TIN |
| **37** | Transmitter Contact | Legal name |
| **8** | Designated Contact Office | Legal name |
| **46** | Branch Where Activity Occurred | Address (with country), RSSD number |
| **41** | FI Where Account is Held | Name, address, TIN |

#### Party Sub-Records

Each party can have multiple sub-records:

- **Names** — Legal name, DBA (Doing Business As), AKA (Also Known As)
- **Addresses** — Street, city, state, zip, country (with "unknown" indicators)
- **Phone Numbers** — Residence, work, mobile, fax
- **Identifications** — SSN, EIN, TIN, driver's license, passport, etc.
- **Organization Classifications** — Institution type and subtype (for financial institutions)
- **Electronic Addresses** — Email and website (subjects only)
- **Occupation** — NAICS code and business description (subjects only)
- **Associations** — Subject-to-institution relationships with date range and role indicators
- **Account Associations** — Subject's account details at an institution

#### Subject Party Special Fields

The **Subject** (type 33) party has additional fields:

| Field | Description |
|-------|-------------|
| All Critical Subject Info Unavailable | Check if no identifying info is available |
| Birth Date Unknown | Check if date of birth is unknown |
| Gender | Male, Female, or Unknown (mutually exclusive) |
| No Known Account Involved | Check if subject has no known accounts |
| Purchaser/Sender Indicator | Subject is the purchaser/sender in the transaction |
| Payee/Receiver Indicator | Subject is the payee/receiver in the transaction |
| Both Purchaser/Sender & Payee/Receiver | Subject is both |

---

### Step 5 — Suspicious Activity

Define the suspicious activity being reported (Part II of the SAR form).

#### Activity Details

| Field | Required | Description |
|-------|----------|-------------|
| Total Suspicious Amount | Conditional | Dollar amount (whole dollars only) |
| Amount Unknown | Conditional | Check if total amount is unknown |
| No Amount Involved | Conditional | Check if no monetary amount is involved |
| Activity Start Date | Yes | When the suspicious activity began |
| Activity End Date | Yes | When the suspicious activity ended (or ongoing) |
| Date Unknown | Conditional | Check if dates are unknown |

> **Note:** Total Amount, Amount Unknown, and No Amount Involved are mutually exclusive — select exactly one.

#### Activity Classifications

Add one or more suspicious activity classifications:

| Field | Required | Description |
|-------|----------|-------------|
| Activity Type | Yes | Category (Structuring, Fraud, Money Laundering, etc.) |
| Activity Subtype | Yes | Specific classification within the type |
| Other Type Text | Conditional | Required when subtype is "Other" (code ending in 999) |

Common activity types include:
- **Structuring** — Transactions structured to avoid reporting
- **Fraud** — ACH, advance fee, check, wire, mortgage, etc.
- **Money Laundering** — Layering, placement, integration
- **Terrorist Financing** — Known or suspected terrorist activity
- **Cyber Event** — Computer intrusion, ransomware, phishing
- **Identity/Documentation** — Altered/forged documents, identity theft

---

### Step 6 — IP Addresses

Record internet protocol addresses associated with the suspicious activity (Item 43).

| Field | Required | Max Length | Description |
|-------|----------|-----------|-------------|
| IP Address | Yes | 45 chars | IPv4 (e.g., 192.168.1.1) or IPv6 address |
| Date | No | — | Date the IP address was observed |
| Timestamp | No | — | Time the IP address was observed |

---

### Step 7 — Cyber Events

Record cyber event indicators (Item 44) such as malware hashes, suspicious URLs, or command-and-control addresses.

| Field | Required | Description |
|-------|----------|-------------|
| Event Type | Yes | Type of cyber indicator (see reference table below) |
| Value | Yes | The indicator value (IP, URL, hash, etc.) |
| Date | No | Date observed |
| Timestamp | No | Time observed |
| Other Type Text | Conditional | Required when type is "Other" (code 999) |

**Cyber Event Types:**

| Code | Label |
|------|-------|
| 1 | Command and Control IP Address |
| 2 | Command and Control URL/Domain |
| 3 | Malware Hash (MD5/SHA-1/SHA-256) |
| 4 | MAC Address |
| 5 | Port |
| 6 | Suspicious E-mail Address |
| 7 | Suspicious File Name |
| 8 | Suspicious IP Address |
| 9 | Suspicious URL/Domain |
| 10 | Targeted System |
| 999 | Other |

---

### Step 8 — Assets

Identify the financial products and payment instruments involved (Items 45–50).

#### Product Types (Item 45)

Examples: Credit Card, Debit Card, Deposit Account, Forex, Futures/Options, Hedge Fund, Insurance/Annuity, Loan, Money Orders, Mutual Fund, Stocks, Virtual Currency

#### Instrument/Payment Mechanism Types (Item 46)

Examples: U.S. Currency, Foreign Currency, Wire Transfer, Money Order, Check, Cashier's Check, Personal/Business Check, Traveler's Check, Gaming Instruments, Prepaid Access

#### Asset Attributes (Items 47–50)

For each asset, you can add attributes:

| Attribute | Description |
|-----------|-------------|
| CUSIP Number | Committee on Uniform Securities Identification Procedures number |
| Commodity Type | Type of commodity involved |
| Product/Instrument Description | Detailed description |
| Market Where Traded | Where the product was traded |

---

### Step 9 — Narratives

Write the free-text description of the suspicious activity (Part V). This is a critical section of the SAR.

**Constraints:**

| Rule | Limit |
|------|-------|
| Maximum blocks | 5 |
| Maximum characters per block | 4,000 |
| Maximum total characters | 20,000 |
| Sequence numbers | 1 through 5 |
| Minimum for submission | At least 1 block |

**Tips for writing narratives:**
- Describe the suspicious activity in detail
- Include who, what, when, where, why, and how
- Reference specific transactions, dates, and amounts
- Explain why the activity is suspicious
- Note any law enforcement contacts
- Use multiple blocks to organize long narratives

---

## 7. Filing Workflow

### Workflow Diagram

```
                    ┌──────────────────────────────────────────────┐
                    │           FinCEN SAR Filing Workflow          │
                    └──────────────────────────────────────────────┘

   ┌───────┐      ┌────────┐      ┌───────────┐      ┌──────────────┐
   │ DRAFT │─────→│ REVIEW │─────→│ SUBMITTED │─────→│ ACKNOWLEDGED │
   └───────┘      └────────┘      └───────────┘      └──────────────┘
       ↑               │                │
       │               │                │
       └───────────────┘                │
     (Return to Draft)                  ↓
                                  ┌──────────┐
       ┌──────────────────────────│ REJECTED │
       │   (Return to Draft)      └──────────┘
       ↓
   ┌───────┐
   │ DRAFT │
   └───────┘
```

### Status Transitions

| Current Status | Action | New Status | Required Role | UI Button |
|----------------|--------|------------|---------------|-----------|
| DRAFT | Submit for Review | REVIEW | REVIEWER+ | "Submit for Review" |
| REVIEW | Return to Draft | DRAFT | REVIEWER+ | "Return to Draft" |
| REVIEW | Submit to FinCEN | SUBMITTED | APPROVER+ | "Submit to FinCEN" |
| SUBMITTED | Acknowledge | ACKNOWLEDGED | APPROVER+ | "Acknowledge" |
| SUBMITTED | Reject | REJECTED | APPROVER+ | "Reject" |
| REJECTED | (Re-open) | DRAFT | Any | "Return to Draft" |

**Status Descriptions:**

| Status | Color | Meaning |
|--------|-------|---------|
| **DRAFT** | Gray | Being prepared — editable |
| **REVIEW** | Blue | Under review — editable |
| **SUBMITTED** | Yellow | Sent to FinCEN — **locked, no edits allowed** |
| **ACKNOWLEDGED** | Green | Accepted by FinCEN — **terminal state** |
| **REJECTED** | Red | Rejected by FinCEN — can return to draft for corrections |

### Submission Validation

When submitting a batch to FinCEN (REVIEW → SUBMITTED), the system validates that every activity in the batch meets minimum FinCEN requirements:

- [ ] **At least 1 activity** in the batch
- [ ] **Filing type** is set (initial, corrects/amends, continuing, or joint)
- [ ] **Filing Institution** (type 30) is present with:
  - Legal name
  - Address
  - TIN (EIN or RSSD)
  - Primary regulator code
  - Organization classification
- [ ] **Subject** (type 33) is present with:
  - Legal name OR "all critical info unavailable" flag
- [ ] **FI Where Activity Occurred** (type 34) is present with:
  - Legal name
  - Address
  - TIN
  - Primary regulator code
  - Organization classification
- [ ] **Suspicious activity** is defined
- [ ] **At least 1 narrative** block is present

If validation fails, you will see a **422 Unprocessable Entity** error listing the specific violations. Fix the issues and retry.

---

## 8. Generating BSA XML

To export a batch in FinCEN BSA XML format:

1. Navigate to the batch detail page
2. Click **Download XML** (or use the API: `GET /batches/{batchId}/xml`)
3. The system generates XML conforming to the FinCEN BSA schema
4. Save the XML file for submission through the FinCEN BSA E-Filing system

The XML includes all activities, parties, suspicious activity details, narratives, and supporting data from the batch.

---

## 9. FinCEN Reference Data

### Party Types

| Code | Label | SAR Section |
|------|-------|-------------|
| 30 | Filing Institution | Part III |
| 33 | Subject | Part I |
| 34 | Financial Institution Where Activity Occurred | Part IV |
| 35 | Transmitter | Transmitter Record |
| 37 | Transmitter Contact | Transmitter Record |
| 8 | Designated Contact Office | Part III |
| 46 | Branch Where Activity Occurred | Part IV (Item 68-74) |
| 41 | Financial Institution Where Account is Held | Part I (Item 27) |
| 18 | Law Enforcement Agency | Part II |
| 19 | Law Enforcement Contact Name | Part II |

### Identification Types

| Code | Label | Max Length | Typical Use |
|------|-------|-----------|-------------|
| 1 | SSN/ITIN | 9 | Individual subjects |
| 2 | EIN | 9 | Filing institutions, entities |
| 4 | TIN | 25 | Generic tax identification |
| 5 | Driver's License / State ID | 24 | Individual subjects |
| 6 | Passport | 24 | Individual subjects |
| 7 | Alien Registration | 24 | Individual subjects |
| 9 | Foreign TIN | 25 | Foreign entities |
| 10 | CRD Number | 10 | Broker-dealers |
| 11 | IARD Number | 10 | Investment advisers |
| 12 | NFA ID | 10 | Commodity operators |
| 13 | SEC Number | 10 | SEC-regulated entities |
| 14 | RSSD Number | 10 | Bank branches |
| 28 | TCC (Transmitter Control Code) | 14 | Transmitter only |
| 29 | Internal Control / File Number | 20 | Any entity |
| 32 | NAIC Number | 10 | Insurance companies |
| 33 | NMLS Number | 10 | Mortgage lenders |
| 999 | Other | 24 | User-specified |

### Regulator Codes

| Code | Regulator |
|------|-----------|
| 1 | Federal Reserve |
| 2 | FDIC |
| 3 | NCUA |
| 4 | OCC |
| 6 | SEC |
| 7 | IRS |
| 9 | CFTC |
| 13 | FHFA |
| 99 | Not Applicable |

### Organization Types

| Code | Type | Subtypes Available |
|------|------|--------------------|
| 1 | Casino / Card Club | Yes |
| 2 | Depository Institution | No |
| 3 | Insurance Company | No |
| 4 | MSB (Money Service Business) | No |
| 5 | Securities / Futures | Yes |
| 11 | Loan or Finance Company | No |
| 12 | Housing GSE | No |
| 999 | Other | No |

### Suspicious Activity Types

| Code | Category | Has Subtypes |
|------|----------|:------------:|
| 1 | Structuring | ✓ |
| 3 | Fraud | ✓ |
| 4 | Identification / Documentation | ✓ |
| 5 | Insurance | ✓ |
| 6 | Securities / Futures / Options | ✓ |
| 7 | Terrorist Financing | ✓ |
| 8 | Money Laundering | ✓ |
| 9 | Other Suspicious Activities | ✓ |
| 10 | Mortgage Fraud | ✓ |
| 11 | Cyber Event | ✓ |
| 12 | Gaming Activities | ✓ |

### Cyber Event Types

| Code | Description |
|------|-------------|
| 1 | Command and Control IP Address |
| 2 | Command and Control URL / Domain |
| 3 | Malware Hash (MD5 / SHA-1 / SHA-256) |
| 4 | MAC Address |
| 5 | Port |
| 6 | Suspicious E-mail Address |
| 7 | Suspicious File Name |
| 8 | Suspicious IP Address |
| 9 | Suspicious URL / Domain |
| 10 | Targeted System |
| 999 | Other |

### Asset & Instrument Types

**Product Types (Item 45):** Credit Card, Debit Card, Deposit Account, Forex, Futures/Options on Futures, Hedge Fund, Insurance/Annuity Contract, Loan, Money Orders, Mutual Fund, Stocks, Options on Stocks, Bonds/Notes, Virtual Currency

**Instrument/Payment Mechanisms (Item 46):** U.S. Currency, Foreign Currency, Funds Transfer (Wire), Money Order, Personal/Business Check, Cashier's Check, Official Bank Check, Traveler's Check, Gaming Instruments, Prepaid Access (cards/devices)

### Field Length Limits

| Field | Maximum Length |
|-------|:-------------:|
| Party Full Name (organization) | 150 |
| Individual Last Name | 150 |
| Individual First Name | 35 |
| Individual Middle Name | 35 |
| Individual Name Suffix | 35 |
| Street Address | 100 |
| City | 50 |
| Zip Code | 9 |
| Phone Number | 16 |
| Identification Number | 25 |
| Occupation / Business Text | 50 |
| NAICS Code | 6 |
| Electronic Address (Email/URL) | 517 |
| Narrative Block | 4,000 |
| Narrative Total (all blocks) | 20,000 |
| Note to FinCEN | 50 |
| Prior Document Number | 14 |
| Account Number | 40 |
| IP Address | 45 |
| CSV Attachment Filename | 255 |
| "Other" type text fields | 50 |

---

## 10. API Reference

All API endpoints are prefixed with `/api/v1`. Authentication is required for all endpoints except login.

Include the JWT token in every request:
```
Authorization: Bearer <your-jwt-token>
```

### Authentication Endpoints

| Method | Path | Description | Auth |
|--------|------|-------------|------|
| `POST` | `/auth/login` | Authenticate and receive JWT token | Public |
| `POST` | `/auth/register` | Register new user | ADMIN only |

**Login Request:**
```json
{
  "username": "admin",
  "password": "Admin123!"
}
```

**Login Response:**
```json
{
  "token": "eyJhbGci...",
  "username": "admin",
  "fullName": "System Administrator",
  "role": "ADMIN"
}
```

### Batch Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/batches` | Create a new batch |
| `GET` | `/batches` | List batches (paginated) |
| `GET` | `/batches/{id}` | Get batch with activity summaries |
| `DELETE` | `/batches/{id}` | Delete batch and all children |
| `GET` | `/batches/{id}/xml` | Generate BSA XML |

**Query Parameters for List:**
- `page` — Page number (default: 0)
- `size` — Page size (default: 20)
- `status` — Filter by filing status (DRAFT, REVIEW, SUBMITTED, etc.)
- `sort` — Sort field (createdAt)
- `direction` — Sort direction (asc, desc)

### Workflow Endpoints

| Method | Path | Description | Required Role |
|--------|------|-------------|---------------|
| `POST` | `/batches/{id}/workflow/review` | DRAFT → REVIEW | REVIEWER+ |
| `POST` | `/batches/{id}/workflow/draft` | REVIEW → DRAFT | REVIEWER+ |
| `POST` | `/batches/{id}/workflow/submit` | REVIEW → SUBMITTED | APPROVER+ |
| `POST` | `/batches/{id}/workflow/acknowledge` | SUBMITTED → ACKNOWLEDGED | APPROVER+ |
| `POST` | `/batches/{id}/workflow/reject` | SUBMITTED → REJECTED | APPROVER+ |

### Activity Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/batches/{batchId}/activities` | Create activity in batch |
| `GET` | `/batches/{batchId}/activities` | List activities in batch |
| `GET` | `/activities/{id}` | Get full activity detail |
| `DELETE` | `/activities/{id}` | Delete activity |
| `PATCH` | `/activities/{id}/header` | Update activity header |
| `PATCH` | `/activities/{id}/filing-type` | Update filing type |
| `PATCH` | `/activities/{id}/support-document` | Update support document |

### Party Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{activityId}/parties` | Add party to activity |
| `GET` | `/activities/{activityId}/parties` | List parties for activity |
| `GET` | `/parties/{id}` | Get party detail |
| `DELETE` | `/parties/{id}` | Delete party |
| `PATCH` | `/parties/{id}/header` | Update party header fields |
| `POST` | `/parties/{id}/names` | Add party name |
| `DELETE` | `/parties/{id}/names/{nameId}` | Remove party name |
| `POST` | `/parties/{id}/addresses` | Add address |
| `DELETE` | `/parties/{id}/addresses/{addrId}` | Remove address |
| `POST` | `/parties/{id}/phones` | Add phone number |
| `DELETE` | `/parties/{id}/phones/{phoneId}` | Remove phone number |
| `POST` | `/parties/{id}/identifications` | Add identification |
| `DELETE` | `/parties/{id}/identifications/{identId}` | Remove identification |
| `POST` | `/parties/{id}/org-classifications` | Add org classification |
| `DELETE` | `/parties/{id}/org-classifications/{classId}` | Remove org classification |
| `PUT` | `/parties/{id}/occupation` | Set/update occupation |
| `DELETE` | `/parties/{id}/occupation` | Remove occupation |
| `POST` | `/parties/{id}/electronic-addresses` | Add email/URL |
| `DELETE` | `/parties/{id}/electronic-addresses/{addrId}` | Remove email/URL |
| `POST` | `/parties/{id}/associations` | Add party association |
| `PATCH` | `/party-associations/{id}` | Update association |
| `DELETE` | `/parties/{id}/associations/{assocId}` | Remove association |
| `POST` | `/party-associations/{id}/branches` | Add branch to association |
| `PATCH` | `/branch-parties/{id}` | Update branch |
| `DELETE` | `/party-associations/{assocId}/branches/{branchId}` | Remove branch |
| `PUT` | `/parties/{id}/account-association` | Set account association |

### Suspicious Activity Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `PUT` | `/activities/{id}/suspicious-activity` | Create/update suspicious activity |
| `GET` | `/activities/{id}/suspicious-activity` | Get suspicious activity |
| `DELETE` | `/activities/{id}/suspicious-activity` | Remove suspicious activity |
| `PATCH` | `/activities/{id}/suspicious-activity` | Patch suspicious activity fields |
| `POST` | `/activities/{id}/suspicious-activity/classifications` | Add classification |
| `DELETE` | `/activities/{id}/suspicious-activity/classifications/{classId}` | Remove classification |

### IP Address & Cyber Event Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{id}/ip-addresses` | Add IP address |
| `GET` | `/activities/{id}/ip-addresses` | List IP addresses |
| `DELETE` | `/activities/{id}/ip-addresses/{ipId}` | Remove IP address |
| `POST` | `/activities/{id}/cyber-events` | Add cyber event |
| `DELETE` | `/activities/{id}/cyber-events/{eventId}` | Remove cyber event |

### Asset Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{id}/assets` | Add asset |
| `DELETE` | `/activities/{id}/assets/{assetId}` | Remove asset |
| `POST` | `/activities/{id}/asset-attributes` | Add asset attribute |
| `DELETE` | `/activities/{id}/asset-attributes/{attrId}` | Remove asset attribute |

### Narrative Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{id}/narratives` | Add narrative block |
| `PATCH` | `/activities/{id}/narratives/{seqNum}` | Update narrative text |
| `GET` | `/activities/{id}/narratives` | List narratives |
| `DELETE` | `/activities/{id}/narratives/{narrativeId}` | Remove narrative block |

### Dashboard Endpoints

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/dashboard/summary` | Get dashboard summary metrics |

---

## 11. Deployment

### Docker Compose (Recommended)

The easiest way to run the full stack:

```bash
# Start all services
docker compose up --build -d

# Check status
docker compose ps

# View logs
docker compose logs -f sar-api    # API logs
docker compose logs -f sar-web    # Web server logs
docker compose logs -f postgres   # Database logs

# Stop all services
docker compose down

# Stop and remove data volumes
docker compose down -v
```

**Services started by Docker Compose:**

| Service | Image | Port | Health Check |
|---------|-------|------|-------------|
| postgres | postgres:16-alpine | 5432 | `pg_isready` every 10s |
| sar-api | Custom (Java 21) | 8080 | `/api/v1/actuator/health` every 30s |
| sar-web | Custom (Nginx) | 3000 | `curl localhost:80` every 30s |

### Environment Variables

Create a `.env` file in the project root to customize settings:

```env
# Database
POSTGRES_DB=fincen_sar
POSTGRES_USER=saruser
POSTGRES_PASSWORD=sarpassword

# API
DB_URL=jdbc:postgresql://postgres:5432/fincen_sar
DB_USERNAME=saruser
DB_PASSWORD=sarpassword
JWT_SECRET=your-256-bit-secret-key-for-production
JWT_EXPIRATION_MS=86400000
SPRING_PROFILES_ACTIVE=dev

# CORS
APP_CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

> **Production Note:** Always change the default passwords and JWT secret for production deployments. Use a cryptographically strong random value of at least 256 bits for `JWT_SECRET`.

### Running Locally (Development)

**Backend:**
```bash
# Start PostgreSQL (or use Docker)
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_DB=fincen_sar \
  -e POSTGRES_USER=saruser \
  -e POSTGRES_PASSWORD=sarpassword \
  postgres:16-alpine

# Run the API
./mvnw spring-boot:run
```

**Frontend:**
```bash
cd web
npm install
npm run dev
# Opens at http://localhost:5173
```

**Running Tests:**
```bash
# Backend tests (uses H2 in-memory database)
./mvnw test

# Frontend tests
cd web && npm test
```

---

## 12. Troubleshooting

### Common Issues

**Cannot connect to the application**
- Verify all containers are running: `docker compose ps`
- Check that ports 3000, 8080, and 5432 are not in use by other applications
- Check container logs: `docker compose logs -f`

**Login fails**
- Verify you are using the correct credentials (see [Demo Accounts](#logging-in))
- Check that the API container is healthy: `docker compose ps`
- Try `admin` / `Admin123!` for full access

**"Cannot modify activity in current filing status"**
- The batch is in SUBMITTED or ACKNOWLEDGED status
- Return the batch to DRAFT status before making changes (REVIEWER+ role required)

**"Validation failed" on submission (422 error)**
- The batch or activity is missing required data for FinCEN submission
- Check the error message for specific violations
- Ensure each activity has: filing type, filing institution (type 30), subject (type 33), suspicious activity, and at least one narrative

**Narrative too long**
- Each narrative block is limited to 4,000 characters
- Total across all blocks cannot exceed 20,000 characters
- Split long narratives across multiple blocks (up to 5)

**Build fails**
- Ensure Java 21+ is installed: `java -version`
- Ensure Maven wrapper is executable: `chmod +x mvnw`
- Clear and rebuild: `./mvnw clean install`

**Database migration errors**
- Check Flyway migration logs: `docker compose logs sar-api | grep Flyway`
- Ensure no manual schema changes were made to a managed database
- For a fresh start: `docker compose down -v && docker compose up --build -d`

### Health Check Endpoints

| Endpoint | Description |
|----------|-------------|
| `GET /api/v1/actuator/health` | Overall application health |
| `GET /api/v1/actuator/health/readiness` | Readiness probe (includes DB check) |
| `GET /api/v1/actuator/health/liveness` | Liveness probe |
| `GET /api/v1/actuator/info` | Application info |
| `GET /api/v1/actuator/metrics` | Application metrics |

### Getting Help

- Review the [README.md](../README.md) for project overview
- Check the [database schema](../src/main/resources/db/migration/) for data model details
- API documentation is available at `http://localhost:8080/api/v1/swagger-ui.html` (when running)

---

*This guide covers FinCEN SAR Platform version as of the latest commit. For schema details, refer to the FinCEN BSA E-Filing specifications.*
