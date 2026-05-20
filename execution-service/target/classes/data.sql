INSERT INTO user_record_status (
  record_id, tenant_id, status_code, status_reason, updated_by, updated_at
) VALUES
('candidate-1001', 'default', 'APPLIED', 'Initial seeded status', 'system', CURRENT_TIMESTAMP)
ON CONFLICT (record_id) DO NOTHING;
