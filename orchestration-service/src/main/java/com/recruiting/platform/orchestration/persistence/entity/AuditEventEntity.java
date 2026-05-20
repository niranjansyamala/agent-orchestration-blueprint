package com.recruiting.platform.orchestration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_event")
public class AuditEventEntity {

    @Id
    @Column(name = "audit_event_id", nullable = false, length = 100)
    private String auditEventId;
    @Column(name = "request_id", nullable = false, length = 100)
    private String requestId;
    @Column(name = "workflow_id", nullable = false, length = 100)
    private String workflowId;
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    @Column(name = "agent_id", length = 100)
    private String agentId;
    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;
    @Column(name = "event_timestamp", nullable = false)
    private Instant eventTimestamp;
    @Column(name = "actor_type", nullable = false, length = 30)
    private String actorType;
    @Column(name = "actor_id", length = 100)
    private String actorId;
    @Column(name = "input_context", length = 4000)
    private String inputContext;
    @Column(name = "decision_summary", length = 4000)
    private String decisionSummary;
    @Column(name = "result_payload", length = 4000)
    private String resultPayload;

    public String getAuditEventId() { return auditEventId; }
    public void setAuditEventId(String auditEventId) { this.auditEventId = auditEventId; }
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getAgentId() { return agentId; }
    public void setAgentId(String agentId) { this.agentId = agentId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Instant getEventTimestamp() { return eventTimestamp; }
    public void setEventTimestamp(Instant eventTimestamp) { this.eventTimestamp = eventTimestamp; }
    public String getActorType() { return actorType; }
    public void setActorType(String actorType) { this.actorType = actorType; }
    public String getActorId() { return actorId; }
    public void setActorId(String actorId) { this.actorId = actorId; }
    public String getInputContext() { return inputContext; }
    public void setInputContext(String inputContext) { this.inputContext = inputContext; }
    public String getDecisionSummary() { return decisionSummary; }
    public void setDecisionSummary(String decisionSummary) { this.decisionSummary = decisionSummary; }
    public String getResultPayload() { return resultPayload; }
    public void setResultPayload(String resultPayload) { this.resultPayload = resultPayload; }
}
