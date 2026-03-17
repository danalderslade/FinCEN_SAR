# FinCEN SAR Filing Platform

Last updated: March 17, 2026

Full-stack FinCEN SAR application for creating, reviewing, and progressing filings through a controlled workflow.

Backend: Spring Boot 4, Java 21, PostgreSQL, Flyway, JWT security.
Frontend: React 19, Vite, TypeScript.

## Current Product Scope

- JWT-authenticated API with role-based authorization (ANALYST, REVIEWER, APPROVER, ADMIN)
- Batch lifecycle and filing workflow (DRAFT -> REVIEW -> SUBMITTED -> ACKNOWLEDGED or REJECTED)
- Full SAR activity creation and section-by-section editing via granular PATCH/POST/DELETE endpoints
- Dashboard summary metrics and XML generation for batch export
- Audit and security schema in database migrations

## Repository Layout

- `src/main` Spring Boot API and Flyway migrations
- `src/test` integration tests
- `web` React frontend
- `docs/USER_GUIDE.md` end-user workflow guide
- `k8s` Kubernetes manifests

## Quick Start (Local)

### Prerequisites

- Java 21+
- Maven 3.9+
- Node.js 20+
- Docker (recommended for local PostgreSQL)

### 1) Start PostgreSQL

```bash
docker run -d \
  --name fincen-sar-db \
  -e POSTGRES_DB=fincen_sar \
  -e POSTGRES_USER=saruser \
  -e POSTGRES_PASSWORD=sarpassword \
  -p 5432:5432 \
  postgres:16
```

### 2) Start API

```bash
export JWT_SECRET="replace-with-a-random-48-byte-secret"
mvn spring-boot:run
```

API base URL: http://localhost:8080/api/v1

Optional dev profile (extra actuator + debug logs):

```bash
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

### 3) Start Frontend

```bash
cd web
npm install
npm run dev
```

Frontend URL: http://localhost:5173

### 4) Run Tests

```bash
mvn test
```

## Docker Compose Run

Use this when you want API, DB, and web in one stack.

```bash
cp .env.example .env
# Set strong credentials/secrets in .env
docker compose up -d --build
```

Default app URL in compose: http://localhost:3000

Health checks:

```bash
curl http://localhost:8080/api/v1/actuator/health
curl http://localhost:3000/api/v1/actuator/health
```

If web returns 502, rebuild cleanly:

```bash
docker compose down --remove-orphans
docker compose up -d --build
```

## Authentication and Demo Users

- Public endpoint: `POST /api/v1/auth/login`
- All other API endpoints require Bearer JWT token
- Demo users are seeded by migration `V5__demo_users.sql`

Local demo credentials:

- admin / Admin123!
- analyst / Admin123!
- reviewer / Admin123!
- approver / Admin123!

For non-local environments, replace demo users and passwords.

## API Summary

All endpoints are under `/api/v1`.

### Core

- Authentication: `/auth/login`, `/auth/register` (ADMIN only)
- Dashboard: `GET /dashboard/summary`
- Batch XML export: `GET /batches/{batchId}/xml`

### Batches

- `POST /batches`
- `GET /batches` (supports `status`, `page`, `size`, `sort`, `direction`)
- `GET /batches/{id}`
- `DELETE /batches/{id}`

### Activities

- `POST /batches/{batchId}/activities`
- `GET /batches/{batchId}/activities` (paginated)
- `GET /activities/{id}`
- `DELETE /activities/{id}`

### Workflow

- `POST /batches/{batchId}/workflow/review`
- `POST /batches/{batchId}/workflow/draft`
- `POST /batches/{batchId}/workflow/submit`
- `POST /batches/{batchId}/workflow/acknowledge`
- `POST /batches/{batchId}/workflow/reject`

### Section-by-Section Editing

Granular activity editing is implemented via grouped PATCH and child-item endpoints:

- Activity header and filing flags
- Support document
- Party tree (names, addresses, phones, IDs, occupation, org classification, associations, branches, account association)
- Suspicious activity and classifications
- IP addresses
- Cyber events
- Assets and asset attributes
- Narratives (including text patch by sequence)

See controller implementations under `src/main/java/com/fincen/sar/controller` for the complete endpoint list.

## Configuration

Primary environment variables:

- `DB_URL` (default `jdbc:postgresql://localhost:5432/fincen_sar`)
- `DB_USERNAME` (default `saruser`)
- `DB_PASSWORD` (default `sarpassword`)
- `PORT` (default `8080`)
- `APP_CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`)
- `JWT_SECRET` (required)
- `JWT_EXPIRATION_MS` (default `86400000`)

## Additional Documentation

- End-user guide: `docs/USER_GUIDE.md`
- Enterprise roadmap: `docs/ENTERPRISE_REMEDIATION_ROADMAP.md`
- Frontend guide: `web/README.md`
