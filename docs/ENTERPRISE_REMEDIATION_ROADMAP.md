# Enterprise Remediation Roadmap (Tier 1 Bank Readiness)

## Scope and Constraint
- The platform must continue to emit FinCEN SAR XML that conforms exactly to the SARX schema.
- Production controls must be secure-by-default with explicit, auditable exceptions for development.

## Completed in This Phase
- SARX-aligned XML generation implemented in the backend export path.
- Local XSD validation added and wired to export and submit transitions.
- Integration tests extended to cover valid and invalid SARX export outcomes.
- Production default configuration hardened:
  - `JWT_SECRET` is now required (no dev fallback in default profile).
  - Actuator web exposure defaults to `health,info` only.
  - Health details default to `never`.
  - Application log level default reduced to `INFO`.
  - `dev` profile now carries expanded diagnostics (`metrics`, `prometheus`, `DEBUG`).

## High Priority Remaining Gaps
- API create/update paths still allow data that may fail SARX at export time.
- Authentication is local credential + symmetric JWT only; no enterprise IdP integration.
- Secrets are still environment variable based; no external secrets manager integration.
- Audit log coverage is incomplete for all sensitive CRUD and workflow events.
- No CI quality gate enforcing schema-conformance and enterprise security checks on pull requests.

## Next Recommended Work Packages
1. Shift-left validation
- Enforce SARX-compatible field and cardinality validation during create and patch operations.
- Add deterministic validation error codes for UI and operations observability.

2. Identity and access hardening
- Integrate OIDC/SAML enterprise SSO.
- Replace coarse role model with scoped, least-privilege permissions and endpoint-level policy tests.

3. Secrets and key management
- Move `JWT_SECRET` and database credentials to external secret management (KMS + vault/operator).
- Implement secret rotation runbook and key rollover support for JWT verification.

4. Audit and monitoring
- Expand immutable audit events for all create/update/delete/workflow actions.
- Add structured security events (auth failures, privilege denials, suspicious API patterns).

5. CI/CD governance
- Add pipeline stages for: unit/integration tests, SARX conformance suite, SAST, dependency and container scans.
- Block merges on failed schema conformance and critical security findings.

## Definition of Done for Enterprise Readiness
- Every submitted batch is guaranteed SARX-valid before persistence transition to `SUBMITTED`.
- No hardcoded credentials or fallback secrets in default runtime profile.
- Complete traceability for who changed what, when, and why for all sensitive operations.
- Continuous controls in CI/CD with policy-based deployment gates.
