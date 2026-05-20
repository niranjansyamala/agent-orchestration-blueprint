package com.recruiting.platform.orchestration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "hitl_task")
public class HitlTaskEntity {

    @Id
    @Column(name = "hitl_task_id", nullable = false, length = 100)
    private String hitlTaskId;
    @Column(name = "workflow_id", nullable = false, length = 100)
    private String workflowId;
    @Column(name = "tenant_id", nullable = false, length = 100)
    private String tenantId;
    @Column(name = "task_type", nullable = false, length = 100)
    private String taskType;
    @Column(name = "status", nullable = false, length = 30)
    private String status;
    @Column(name = "assigned_to", length = 100)
    private String assignedTo;
    @Column(name = "approval_payload", length = 4000)
    private String approvalPayload;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "resolved_at")
    private Instant resolvedAt;

    public String getHitlTaskId() { return hitlTaskId; }
    public void setHitlTaskId(String hitlTaskId) { this.hitlTaskId = hitlTaskId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAssignedTo() { return assignedTo; }
    public void setAssignedTo(String assignedTo) { this.assignedTo = assignedTo; }
    public String getApprovalPayload() { return approvalPayload; }
    public void setApprovalPayload(String approvalPayload) { this.approvalPayload = approvalPayload; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(Instant resolvedAt) { this.resolvedAt = resolvedAt; }
}
