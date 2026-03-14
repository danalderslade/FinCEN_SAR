# FinCEN SAR Filing Platform

Spring Boot and React full-stack application for FinCEN SAR (Suspicious Activity Report) workflows. The backend exposes a REST API over the SAR domain model, and the frontend provides a browser-based workspace for batch intake and activity drafting.

---

## Architecture

```
PostgreSQL ← Flyway migration (V1__initial_schema.sql)
              ↕
Spring Boot 4 / Java 21
  ├── Controllers  (REST endpoints)
  ├── Services     (business logic + entity builders)
  ├── Mapper       (entity → response DTO)
  ├── Repositories (Spring Data JPA)
  └── Entities     (JPA, 44 tables)

React 19 / Vite / TypeScript
  ├── Workspace dashboard
  ├── Batch and activity API client
  └── Browser dev server with /api proxy to Spring Boot
```

---

## Quick Start

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL 14+ running locally (or use Docker below)

### 1. Start PostgreSQL via Docker
```bash
docker run -d \
  --name fincen-sar-db \
  -e POSTGRES_DB=fincen_sar \
  -e POSTGRES_USER=saruser \
  -e POSTGRES_PASSWORD=sarpassword \
  -p 5432:5432 \
  postgres:16
```

### 2. Run the service
```bash
mvn spring-boot:run
```
The API starts at **http://localhost:8080/api/v1**

### 3. Run the frontend
```bash
cd web
npm install
npm run dev
```
The UI starts at **http://localhost:5173** and proxies `/api/*` requests to the Spring Boot service.

### 4. Run tests
```bash
mvn test
```

### 5. Build the frontend
```bash
cd web
npm run build
```

---

## API Reference

All paths are relative to `/api/v1`.

### EFiling Batch

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/batches` | Create a new batch |
| `GET` | `/batches` | List all batches |
| `GET` | `/batches/{id}` | Get batch with activity summaries |
| `DELETE` | `/batches/{id}` | Delete batch + all activities (cascade) |

### Activity (SAR Document)

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/batches/{batchId}/activities` | Create a full SAR activity (all nested data in one request) |
| `GET` | `/batches/{batchId}/activities` | List activity summaries for a batch |
| `GET` | `/activities/{id}` | Get full activity with all children |
| `DELETE` | `/activities/{id}` | Delete activity + all children (cascade) |

### Party

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{activityId}/parties` | Add a party to an existing activity |
| `GET` | `/activities/{activityId}/parties` | List all parties for an activity |
| `GET` | `/parties/{id}` | Get one party |
| `DELETE` | `/parties/{id}` | Delete party + all sub-records |

### Suspicious Activity

| Method | Path | Description |
|--------|------|-------------|
| `PUT` | `/activities/{activityId}/suspicious-activity` | Create or replace suspicious activity data |
| `GET` | `/activities/{activityId}/suspicious-activity` | Get suspicious activity |
| `DELETE` | `/activities/{activityId}/suspicious-activity` | Delete |

### IP Addresses

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{activityId}/ip-addresses` | Add an IP address record |
| `GET` | `/activities/{activityId}/ip-addresses` | List IP addresses |
| `DELETE` | `/ip-addresses/{id}` | Delete one record |

### Narratives

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/activities/{activityId}/narratives` | Add a narrative block (max 5 per activity) |
| `GET` | `/activities/{activityId}/narratives` | List in sequence order |
| `DELETE` | `/narratives/{id}` | Delete one block |

---

## Example Requests

### Create a Batch
```bash
curl -X POST http://localhost:8080/api/v1/batches \
  -H "Content-Type: application/json" \
  -d '{
    "activityCount": 1,
    "partyCount": 2
  }'
```

### Create a Full SAR Activity
```bash
curl -X POST http://localhost:8080/api/v1/batches/1/activities \
  -H "Content-Type: application/json" \
  -d '{
    "seqNum": 1,
    "filingDate": "2024-06-01",
    "activityAssociation": {
      "seqNum": 1,
      "initialReportIndicator": true
    },
    "parties": [
      {
        "seqNum": 1,
        "activityPartyTypeCode": 30,
        "primaryRegulatorTypeCode": 2,
        "names": [{ "seqNum": 1, "partyNameTypeCode": "L", "rawPartyFullName": "First National Bank" }],
        "addresses": [{ "seqNum": 1, "rawStreetAddress1": "100 Main St", "rawCity": "Washington", "rawStateCode": "DC", "rawZipCode": "20001", "rawCountryCode": "US" }],
        "orgClassifications": [{ "seqNum": 1, "organizationTypeId": 2 }]
      },
      {
        "seqNum": 2,
        "activityPartyTypeCode": 33,
        "maleGenderIndicator": true,
        "individualBirthDate": "1985-03-15",
        "names": [{ "seqNum": 1, "partyNameTypeCode": "L", "rawEntityIndividualLastName": "Doe", "rawIndividualFirstName": "John" }]
      }
    ],
    "suspiciousActivity": {
      "seqNum": 1,
      "totalSuspiciousAmount": 50000,
      "suspiciousActivityFromDate": "2024-01-01",
      "suspiciousActivityToDate": "2024-05-31",
      "classifications": [
        { "seqNum": 1, "suspiciousActivityTypeId": 8, "suspiciousActivitySubtypeId": 807 }
      ]
    },
    "narratives": [
      { "seqNum": 1, "narrativeSequenceNumber": 1, "narrativeText": "Subject conducted multiple large cash transactions structured to avoid CTR reporting." }
    ]
  }'
```

### Delete an Activity
```bash
curl -X DELETE http://localhost:8080/api/v1/activities/1
# Returns 204 No Content — all nested records deleted via CascadeType.ALL
```

---


---

## Granular PATCH API — UI Section-by-Section

Each section of the SAR form can be saved independently. All PATCH endpoints return the **full `ActivityResponse`** so the UI can refresh its state from one response — no follow-up GET needed.

### PATCH Semantics
| HTTP Method | Behaviour |
|-------------|-----------|
| `PATCH` | Only non-null fields applied; omitted fields unchanged |
| `POST` (add-item) | Appends one new child record to the collection |
| `DELETE` (remove-item) | Removes one child by ID; cascades to its sub-children |
| `PUT` (upsert-one) | Replaces a single-child record entirely (occupation, account tree) |

### Step 1 — Activity Header
`PATCH /activities/{id}/header` — update filingDate, efilingPriorDocumentNumber, filingInstitutionNoteToFincen

### Step 2 — Filing Type Flags
`PATCH /activities/{id}/filing-type` — toggle initial / corrects / continuing / joint indicators

### Step 3 — Support Document
`PATCH /activities/{id}/support-document` — set/update CSV attachment filename

### Step 4 — Parties
```
POST   /activities/{id}/parties                              add party
PATCH  /parties/{id}/header                                  party indicator fields
POST   /parties/{id}/names                                   add name
DELETE /parties/{id}/names/{nameId}                          remove name
POST   /parties/{id}/addresses                               add address
DELETE /parties/{id}/addresses/{addrId}                      remove address
POST   /parties/{id}/phones                                  add phone
DELETE /parties/{id}/phones/{phoneId}                        remove phone
POST   /parties/{id}/identifications                         add identification
DELETE /parties/{id}/identifications/{identId}               remove identification
POST   /parties/{id}/org-classifications                     add org type/subtype
DELETE /parties/{id}/org-classifications/{classId}           remove
PUT    /parties/{id}/occupation                              upsert occupation / NAICS
DELETE /parties/{id}/occupation                              remove occupation
POST   /parties/{id}/electronic-addresses                    add email or URL
DELETE /parties/{id}/electronic-addresses/{addrId}           remove
POST   /parties/{id}/associations                            add subject→institution relationship
PATCH  /party-associations/{id}                              patch relationship indicators
DELETE /parties/{id}/associations/{assocId}                  remove association
POST   /party-associations/{id}/branches                     add branch party
PATCH  /branch-parties/{id}                                  patch branch location flags
DELETE /party-associations/{assocId}/branches/{branchId}     remove branch
PUT    /parties/{id}/account-association                     upsert full account tree
```

### Step 5 — Suspicious Activity
```
PUT    /activities/{id}/suspicious-activity                  create or replace
PATCH  /activities/{id}/suspicious-activity                  patch amounts / date range
POST   /activities/{id}/suspicious-activity/classifications  add type/subtype
DELETE /activities/{id}/suspicious-activity/classifications/{classId}
```

### Step 6 — IP Addresses
```
POST   /activities/{id}/ip-addresses                         add
DELETE /activities/{id}/ip-addresses/{ipId}                  remove
```

### Step 7 — Cyber Events
```
POST   /activities/{id}/cyber-events                         add
DELETE /activities/{id}/cyber-events/{eventId}               remove
```

### Step 8 — Assets & Asset Attributes
```
POST   /activities/{id}/assets                               add product/instrument
DELETE /activities/{id}/assets/{assetId}                     remove
POST   /activities/{id}/asset-attributes                     add CUSIP/commodity/market
DELETE /activities/{id}/asset-attributes/{attrId}            remove
```

### Step 9 — Narratives
```
POST   /activities/{id}/narratives                           add block (seqNum 1-5)
PATCH  /activities/{id}/narratives/{seqNum}                  update text only (autosave)
DELETE /activities/{id}/narratives/{narrativeId}             remove block
```

---
## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **One-call activity creation** | `POST /batches/{id}/activities` accepts the entire nested SAR document in one JSON payload, mirroring the XML schema hierarchy. All children are persisted in a single transaction via `CascadeType.ALL`. |
| **Cascade deletes** | Every child table uses `CascadeType.ALL + orphanRemoval=true`. Deleting a batch removes all activities, parties, suspicious activity records, etc. |
| **Flyway migrations** | Schema is managed by Flyway (`V1__initial_schema.sql`). Hibernate validates only — it never modifies the schema. |
| **Party type discrimination** | The `party` table uses a `activity_party_type_code` column (35/37/30/8/18/19/34/33) rather than JPA table-per-class inheritance, matching the flat XML schema structure. |
| **Sub-resource endpoints** | Individual resources (parties, IP addresses, narratives) can also be managed independently for incremental updates without re-submitting the entire activity. |

---

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DB_URL` | `jdbc:postgresql://localhost:5432/fincen_sar` | Database URL |
| `DB_USERNAME` | `saruser` | Database user |
| `DB_PASSWORD` | `sarpassword` | Database password |
| `PORT` | `8080` | HTTP port |

---

## Health Check

```bash
curl http://localhost:8080/api/v1/actuator/health
```
