package com.recruiting.platform.execution.tools;

import com.recruiting.platform.execution.persistence.UserRecordStatusEntity;
import com.recruiting.platform.execution.persistence.UserRecordStatusRepository;
import com.recruiting.platform.execution.mcp.RecruitingMcpClient;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import com.recruiting.platform.execution.toolregistry.ToolSelectionPlan;
import dev.langchain4j.agent.tool.Tool;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Component
public class RecruitingToolsService {

    private final UserRecordStatusRepository userRecordStatusRepository;
    private final RecruitingMcpClient mcpClient;

    public RecruitingToolsService(UserRecordStatusRepository userRecordStatusRepository,
                                  RecruitingMcpClient mcpClient) {
        this.userRecordStatusRepository = userRecordStatusRepository;
        this.mcpClient = mcpClient;
    }

    @Tool("Fetch a recruiting candidate profile")
    public String candidateProfile(AgentExecutionContext context, String candidateId) {
        assertToolAllowed(context, "candidate_tool");
        return mcpClient.candidateProfile(candidateId);
    }

    @Tool("Fetch a job requisition summary")
    public String requisitionSummary(AgentExecutionContext context, String requisitionId) {
        assertToolAllowed(context, "requisition_tool");
        return mcpClient.requisitionSummary(requisitionId);
    }

    @Tool("Fetch a job application summary")
    public String jobApplicationSummary(AgentExecutionContext context, String applicationId) {
        assertToolAllowed(context, "job_application_tool");
        return mcpClient.jobApplicationSummary(applicationId);
    }

    @Tool("Suggest interview time slots")
    public List<String> suggestInterviewSlots(AgentExecutionContext context, String candidateId, String panelName) {
        assertToolAllowed(context, "suggest_interview_slots");
        return List.of(
                "2026-04-30T10:00:00Z with " + panelName,
                "2026-04-30T13:00:00Z with " + panelName,
                "2026-05-01T09:00:00Z with " + panelName
        );
    }

    @Tool("Update the durable candidate workflow status")
    public Map<String, Object> updateCandidateStatus(AgentExecutionContext context,
                                                     String tenantId,
                                                     String candidateId,
                                                     String newStatus,
                                                     String updatedBy,
                                                     String reason) {
        assertToolAllowed(context, "update_candidate_status");
        UserRecordStatusEntity entity = userRecordStatusRepository.findById(candidateId)
                .orElseGet(UserRecordStatusEntity::new);
        entity.setRecordId(candidateId);
        entity.setTenantId(tenantId);
        entity.setStatusCode(newStatus);
        entity.setStatusReason(reason);
        entity.setUpdatedBy(updatedBy);
        entity.setUpdatedAt(Instant.now());
        userRecordStatusRepository.save(entity);
        return Map.of(
                "recordId", candidateId,
                "status", newStatus,
                "reason", reason
        );
    }

    private void assertToolAllowed(AgentExecutionContext context, String toolName) {
        ToolSelectionPlan plan = context.toolSelectionPlan();
        if (!plan.allows(toolName)) {
            throw new IllegalStateException("Tool " + toolName + " is not enabled for agent "
                    + plan.targetAgentId() + ". Selected tools: " + plan.selectedTools());
        }
    }
}
