package com.recruiting.platform.orchestration.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "workflow_step_execution")
public class WorkflowStepExecutionEntity {

    @Id
    @Column(name = "step_execution_id", nullable = false, length = 100)
    private String stepExecutionId;
    @Column(name = "workflow_id", nullable = false, length = 100)
    private String workflowId;
    @Column(name = "step_name", nullable = false, length = 100)
    private String stepName;
    @Column(name = "step_type", nullable = false, length = 50)
    private String stepType;
    @Column(name = "status", nullable = false, length = 30)
    private String status;
    @Column(name = "attempt_no", nullable = false)
    private Integer attemptNo;
    @Column(name = "input_payload", length = 4000)
    private String inputPayload;
    @Column(name = "output_payload", length = 4000)
    private String outputPayload;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "error_code", length = 100)
    private String errorCode;
    @Column(name = "error_message", length = 4000)
    private String errorMessage;

    public String getStepExecutionId() { return stepExecutionId; }
    public void setStepExecutionId(String stepExecutionId) { this.stepExecutionId = stepExecutionId; }
    public String getWorkflowId() { return workflowId; }
    public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
    public String getStepName() { return stepName; }
    public void setStepName(String stepName) { this.stepName = stepName; }
    public String getStepType() { return stepType; }
    public void setStepType(String stepType) { this.stepType = stepType; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getAttemptNo() { return attemptNo; }
    public void setAttemptNo(Integer attemptNo) { this.attemptNo = attemptNo; }
    public String getInputPayload() { return inputPayload; }
    public void setInputPayload(String inputPayload) { this.inputPayload = inputPayload; }
    public String getOutputPayload() { return outputPayload; }
    public void setOutputPayload(String outputPayload) { this.outputPayload = outputPayload; }
    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }
    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}
