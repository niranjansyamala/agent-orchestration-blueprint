CREATE TABLE agent_registry (
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
  cost_tier VARCHAR(30),
  owner_team VARCHAR(100),
  input_schema VARCHAR(4000),
  output_schema VARCHAR(4000),
  health_status VARCHAR(30),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE workflow_execution (
  workflow_id VARCHAR(100) PRIMARY KEY,
  request_id VARCHAR(100) NOT NULL,
  tenant_id VARCHAR(100) NOT NULL,
  session_id VARCHAR(100) NOT NULL,
  user_id VARCHAR(100),
  status VARCHAR(30) NOT NULL,
  intent VARCHAR(100),
  target_agent_id VARCHAR(100),
  router_confidence DOUBLE,
  current_step VARCHAR(100),
  error_code VARCHAR(100),
  error_message VARCHAR(4000),
  result_payload VARCHAR(4000),
  started_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE workflow_step_execution (
  step_execution_id VARCHAR(100) PRIMARY KEY,
  workflow_id VARCHAR(100) NOT NULL,
  step_name VARCHAR(100) NOT NULL,
  step_type VARCHAR(50) NOT NULL,
  status VARCHAR(30) NOT NULL,
  attempt_no INTEGER NOT NULL,
  input_payload VARCHAR(4000),
  output_payload VARCHAR(4000),
  started_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  error_code VARCHAR(100),
  error_message VARCHAR(4000)
);

CREATE TABLE audit_event (
  audit_event_id VARCHAR(100) PRIMARY KEY,
  request_id VARCHAR(100) NOT NULL,
  workflow_id VARCHAR(100) NOT NULL,
  tenant_id VARCHAR(100) NOT NULL,
  agent_id VARCHAR(100),
  event_type VARCHAR(100) NOT NULL,
  event_timestamp TIMESTAMP NOT NULL,
  actor_type VARCHAR(30) NOT NULL,
  actor_id VARCHAR(100),
  input_context VARCHAR(4000),
  decision_summary VARCHAR(4000),
  result_payload VARCHAR(4000)
);

CREATE TABLE user_record_status (
  record_id VARCHAR(100) PRIMARY KEY,
  tenant_id VARCHAR(100) NOT NULL,
  status_code VARCHAR(50) NOT NULL,
  status_reason VARCHAR(255),
  updated_by VARCHAR(100),
  updated_at TIMESTAMP NOT NULL
);

CREATE TABLE hitl_task (
  hitl_task_id VARCHAR(100) PRIMARY KEY,
  workflow_id VARCHAR(100) NOT NULL,
  tenant_id VARCHAR(100) NOT NULL,
  task_type VARCHAR(100) NOT NULL,
  status VARCHAR(30) NOT NULL,
  assigned_to VARCHAR(100),
  approval_payload VARCHAR(4000),
  created_at TIMESTAMP NOT NULL,
  resolved_at TIMESTAMP
);
