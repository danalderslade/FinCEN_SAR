-- V2: Add filing_status column to efiling_batch and activity tables
ALTER TABLE efiling_batch
    ADD COLUMN filing_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

ALTER TABLE activity
    ADD COLUMN filing_status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';
