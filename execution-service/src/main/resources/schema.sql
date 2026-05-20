CREATE TABLE IF NOT EXISTS user_record_status (
  record_id VARCHAR(100) PRIMARY KEY,
  tenant_id VARCHAR(100) NOT NULL,
  status_code VARCHAR(50) NOT NULL,
  status_reason VARCHAR(255),
  updated_by VARCHAR(100),
  updated_at TIMESTAMP NOT NULL
);
