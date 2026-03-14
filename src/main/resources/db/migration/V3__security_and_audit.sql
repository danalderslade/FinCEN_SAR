-- V3: Security (app_user) + Audit trail (audit_log) + created_by / updated_by

-- ── App User table ─────────────────────────────────────────────────────────
CREATE TABLE app_user (
    id              BIGSERIAL PRIMARY KEY,
    username        VARCHAR(100)  NOT NULL UNIQUE,
    full_name       VARCHAR(200)  NOT NULL,
    password        VARCHAR(200)  NOT NULL,
    role            VARCHAR(20)   NOT NULL,
    enabled         BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TRIGGER trigger_app_user_updated_at
    BEFORE UPDATE ON app_user
    FOR EACH ROW EXECUTE FUNCTION trigger_set_updated_at();

COMMENT ON TABLE app_user IS 'Application users with role-based access';

-- ── Seed default admin user (password: Admin123!) ──────────────────────────
-- BCrypt hash of 'Admin123!' with strength 10
INSERT INTO app_user (username, full_name, password, role)
VALUES (
    'admin',
    'System Administrator',
    '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
    'ADMIN'
);

-- ── Audit log table ────────────────────────────────────────────────────────
CREATE TABLE audit_log (
    id              BIGSERIAL PRIMARY KEY,
    entity_type     VARCHAR(50)   NOT NULL,
    entity_id       BIGINT        NOT NULL,
    action          VARCHAR(30)   NOT NULL,
    performed_by    VARCHAR(100)  NOT NULL,
    old_value       TEXT,
    new_value       TEXT,
    created_at      TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_log_performer ON audit_log (performed_by);
CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);

COMMENT ON TABLE  audit_log IS 'Immutable audit trail for compliance tracking';
COMMENT ON COLUMN audit_log.entity_type IS 'E.g. EfilingBatch, Activity';
COMMENT ON COLUMN audit_log.action IS 'E.g. CREATE, UPDATE, DELETE, STATUS_CHANGE';
COMMENT ON COLUMN audit_log.old_value IS 'Previous state (JSON or status string)';
COMMENT ON COLUMN audit_log.new_value IS 'New state (JSON or status string)';

-- ── Add created_by / updated_by to key tables ─────────────────────────────
ALTER TABLE efiling_batch
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);

ALTER TABLE activity
    ADD COLUMN created_by VARCHAR(100),
    ADD COLUMN updated_by VARCHAR(100);
