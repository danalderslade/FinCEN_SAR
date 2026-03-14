-- V4: Performance indexes for pagination, filtering, and dashboard queries

-- ── Batch list filtering & sorting ─────────────────────────────────────────
CREATE INDEX idx_batch_filing_status ON efiling_batch (filing_status);
CREATE INDEX idx_batch_created_at ON efiling_batch (created_at DESC);
CREATE INDEX idx_batch_status_created ON efiling_batch (filing_status, created_at DESC);

-- ── Activity filtering ─────────────────────────────────────────────────────
CREATE INDEX idx_activity_filing_status ON activity (filing_status);
CREATE INDEX idx_activity_filing_date ON activity (filing_date);
CREATE INDEX idx_activity_batch_id ON activity (efiling_batch_id);
