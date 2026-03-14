-- V5: Fix demo admin password hash (password: Admin123!)
UPDATE app_user
SET password = '$2b$10$kmKrjUOfaesG63THVZ502.AbccGtvseZNqwQ.ksEOvRUMQ7TeBX2q'
WHERE username = 'admin';

-- Seed additional demo users for different roles
INSERT INTO app_user (username, full_name, password, role)
VALUES
    ('analyst', 'Demo Analyst', '$2b$10$kmKrjUOfaesG63THVZ502.AbccGtvseZNqwQ.ksEOvRUMQ7TeBX2q', 'ANALYST'),
    ('reviewer', 'Demo Reviewer', '$2b$10$kmKrjUOfaesG63THVZ502.AbccGtvseZNqwQ.ksEOvRUMQ7TeBX2q', 'REVIEWER'),
    ('approver', 'Demo Approver', '$2b$10$kmKrjUOfaesG63THVZ502.AbccGtvseZNqwQ.ksEOvRUMQ7TeBX2q', 'APPROVER')
ON CONFLICT (username) DO NOTHING;
