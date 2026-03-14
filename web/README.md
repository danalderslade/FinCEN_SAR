# FinCEN SAR Web

React 19.2 + Vite + TypeScript frontend for the FinCEN SAR platform.

## Development

Start the Spring Boot API on port 8080 first, then run:

```bash
npm run dev
```

The Vite dev server runs on port 5173 and proxies `/api/*` traffic to `http://localhost:8080`.

## Current Scope

- Workspace shell for the SAR platform
- Batch inventory view backed by `GET /api/v1/batches`
- Styling baseline for the future filing wizard and review screens

## Next UI Slices

- Batch creation flow
- Activity workspace with step-based PATCH saves
- Party and suspicious activity editors
- Authentication, review queue, and audit history
