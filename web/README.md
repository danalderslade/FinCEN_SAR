# FinCEN SAR Web

Last updated: March 17, 2026

React + Vite + TypeScript frontend for the FinCEN SAR Filing Platform.

## Current Features

- JWT login flow with persisted session in local storage
- Protected application shell with role display and sign-out
- Dashboard with summary metrics and recent batches
- Batch list with status filtering, pagination, create, and delete
- Batch detail with workflow actions and XML download
- Activity detail with tabbed read view and delete
- Multi-step SAR activity wizard with validation and section saves

## Local Development

Start the backend API first on port 8080, then run:

```bash
npm install
npm run dev
```

Dev app URL: http://localhost:5173

The Vite dev server proxies `/api/*` requests to `http://localhost:8080`.

## Scripts

- `npm run dev` start Vite dev server
- `npm run build` run type-check build and produce production assets
- `npm run lint` run ESLint
- `npm run preview` serve built assets locally

## Routes

- `/` dashboard
- `/batches` batch inventory
- `/batches/:batchId` batch detail
- `/activities/:activityId` activity detail
- `/activities/:activityId/wizard/:step` step-based editor

## Wizard Scope

Current wizard steps:

1. Header
2. Filing Type
3. Parties
4. Suspicious Activity
5. IP Addresses
6. Cyber Events
7. Assets
8. Narratives

## Auth Notes

- Login endpoint: `/api/v1/auth/login`
- Unauthorized API responses trigger automatic logout via global auth-error handling
- Local default credential (for seeded demo data): `admin` / `Admin123!`

## Production Build

```bash
npm run build
```

The generated static files are used by the web container image.
