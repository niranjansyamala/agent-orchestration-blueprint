package com.recruiting.platform.execution.agent;

import com.recruiting.platform.common.domain.IntentType;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.model.RoutingDecision;
import com.recruiting.platform.execution.service.AgentExecutionContext;
import com.recruiting.platform.execution.tools.RecruitingToolsService;
import com.recruiting.platform.execution.toolregistry.ToolSelectionPlan;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CandidateStatusAgentTest {

    @Test
    void updatesCandidateStatusPayload() {
        RecruitingToolsService toolsService = new RecruitingToolsService(null, null) {
            @Override
            public Map<String, Object> updateCandidateStatus(AgentExecutionContext context,
                                                             String tenantId,
                                                             String candidateId,
                                                             String newStatus,
                                                             String updatedBy,
                                                             String reason) {
                return Map.of("status", newStatus);
            }
        };
        CandidateStatusAgent agent = new CandidateStatusAgent(toolsService);

        ExecuteAgentRequest request = new ExecuteAgentRequest(
                "wf1",
                "candidate_status_agent",
                new NormalizedAgentRequest("req1", "default", "sess1", "u1", "update status", "web",
                        Map.of("candidateId", "candidate-1001", "newStatus", "INTERVIEW_SCHEDULED", "statusReason", "panel confirmed")),
                new RoutingDecision(IntentType.UPDATE_CANDIDATE_STATUS, "candidate_status_agent", 0.9,
                        java.util.List.of("db_read", "db_update"), false)
        );

        AgentExecutionContext context = new AgentExecutionContext(
                "wf1",
                new ToolSelectionPlan(
                        "candidate_status_agent",
                        List.of("update_candidate_status"),
                        Map.of("update_candidate_status", 100),
                        Map.of("update_candidate_status", "test")
                )
        );

        assertEquals("INTERVIEW_SCHEDULED", agent.execute(request, context).newStatus());
    }
}
