package com.recruiting.platform.execution.toolregistry;

import com.recruiting.platform.common.domain.IntentType;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.model.RoutingDecision;
import com.recruiting.platform.execution.config.ToolSelectionProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToolSelectionServiceTest {

    @Test
    void narrowsCandidateScreeningToRelevantTools() {
        ToolSelectionProperties properties = new ToolSelectionProperties();
        properties.setMaxVisibleTools(3);
        ToolSelectionService service = new ToolSelectionService(new RecruitingToolRegistry(), properties);

        ExecuteAgentRequest request = new ExecuteAgentRequest(
                "wf-1",
                "candidate_screening_agent",
                new NormalizedAgentRequest(
                        "req-1",
                        "tenant-a",
                        "session-1",
                        "recruiter-1",
                        "Screen candidate C101 for requisition REQ9 and assess fit",
                        "web",
                        Map.of("candidateId", "C101", "requisitionId", "REQ9")
                ),
                new RoutingDecision(IntentType.SCREEN_CANDIDATE, "candidate_screening_agent", 0.98,
                        List.of("candidate_read", "requisition_read"), false)
        );

        ToolSelectionPlan plan = service.selectFor(request);

        assertEquals(List.of("candidate_tool", "requisition_tool"), plan.selectedTools());
        assertTrue(plan.toolScores().get("candidate_tool") > 0);
        assertTrue(plan.toolScores().get("requisition_tool") > 0);
    }

    @Test
    void keepsSchedulingToolsetSmallAndDomainScoped() {
        ToolSelectionProperties properties = new ToolSelectionProperties();
        properties.setMaxVisibleTools(2);
        ToolSelectionService service = new ToolSelectionService(new RecruitingToolRegistry(), properties);

        ExecuteAgentRequest request = new ExecuteAgentRequest(
                "wf-2",
                "interview_scheduling_agent",
                new NormalizedAgentRequest(
                        "req-2",
                        "tenant-a",
                        "session-2",
                        "recruiter-2",
                        "Schedule interview panel slots for candidate C201",
                        "web",
                        Map.of("candidateId", "C201", "panelName", "Panel A")
                ),
                new RoutingDecision(IntentType.SCHEDULE_INTERVIEW, "interview_scheduling_agent", 0.95,
                        List.of("schedule_write"), false)
        );

        ToolSelectionPlan plan = service.selectFor(request);

        assertEquals(List.of("suggest_interview_slots", "update_candidate_status"), plan.selectedTools());
    }
}
