CREATE TABLE IF NOT EXISTS agent_registry (
  agent_id VARCHAR(100) PRIMARY KEY,
  agent_name VARCHAR(200) NOT NULL,
  version VARCHAR(40) NOT NULL,
  status VARCHAR(30) NOT NULL,
  supported_intents VARCHAR(4000) NOT NULL,
  capabilities VARCHAR(4000) NOT NULL,
  dispatch_type VARCHAR(30) NOT NULL,
  dispatch_target VARCHAR(255) NOT NULL,
  tenant_scope VARCHAR(4000) NOT NULL,
  priority INTEGER NOT NULL,
  health_status VARCHAR(30),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);
