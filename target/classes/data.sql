INSERT INTO agent_registry (
  agent_id, agent_name, version, status, supported_intents, capabilities,
  dispatch_type, dispatch_target, tenant_scope, priority, cost_tier,
  owner_team, input_schema, output_schema, health_status, created_at, updated_at
) VALUES
('candidate_screening_agent', 'Candidate Screening Agent', 'v1', 'ACTIVE',
 '["SCREEN_CANDIDATE"]',
 '["candidate_lookup","job_lookup","fit_summary"]',
 'WORKFLOW', 'candidate_screening_agent', '["default"]', 100, 'standard',
 'recruiting-platform', '{}', '{}', 'healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('interview_scheduling_agent', 'Interview Scheduling Agent', 'v1', 'ACTIVE',
 '["SCHEDULE_INTERVIEW"]',
 '["calendar_lookup","status_update"]',
 'WORKFLOW', 'interview_scheduling_agent', '["default"]', 95, 'standard',
 'recruiting-platform', '{}', '{}', 'healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('candidate_status_agent', 'Candidate Status Agent', 'v1', 'ACTIVE',
 '["UPDATE_CANDIDATE_STATUS"]',
 '["db_read","db_update"]',
 'WORKFLOW', 'candidate_status_agent', '["default"]', 90, 'standard',
 'recruiting-platform', '{}', '{}', 'healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('recruiter_copilot_agent', 'Recruiter Copilot Agent', 'v1', 'ACTIVE',
 '["GENERATE_RECRUITER_BRIEF","GENERAL_RECRUITING_SUPPORT"]',
 '["candidate_lookup","job_lookup","brief_generation"]',
 'WORKFLOW', 'recruiter_copilot_agent', '["default"]', 80, 'standard',
 'recruiting-platform', '{}', '{}', 'healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('offer_approval_agent', 'Offer Approval Agent', 'v1', 'ACTIVE',
 '["REQUEST_OFFER_APPROVAL"]',
 '["approval_workflow","status_update"]',
 'WORKFLOW', 'offer_approval_agent', '["default"]', 120, 'premium',
 'recruiting-platform', '{}', '{}', 'healthy', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_record_status (
  record_id, tenant_id, status_code, status_reason, updated_by, updated_at
) VALUES
('candidate-1001', 'default', 'APPLIED', 'Initial seeded status', 'system', CURRENT_TIMESTAMP);
