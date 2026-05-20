package com.recruiting.platform.execution.toolregistry;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class RecruitingToolRegistry {

    private final List<RecruitingToolDefinition> toolDefinitions = List.of(
            new RecruitingToolDefinition(
                    "candidate_tool",
                    "candidate",
                    "Fetches candidate profile and recruiting status context from the MCP server.",
                    ToolSource.MCP,
                    Set.of("candidate_read", "profile_read"),
                    Set.of("candidateId"),
                    Set.of("candidate_screening_agent", "recruiter_copilot_agent"),
                    List.of("candidate", "profile", "screen", "screening", "fit", "status")
            ),
            new RecruitingToolDefinition(
                    "requisition_tool",
                    "requisition",
                    "Fetches requisition details such as job requirements, location, and hiring team context.",
                    ToolSource.MCP,
                    Set.of("requisition_read", "job_read"),
                    Set.of("requisitionId"),
                    Set.of("candidate_screening_agent", "recruiter_copilot_agent", "offer_approval_agent"),
                    List.of("requisition", "job", "opening", "requirements", "hiring")
            ),
            new RecruitingToolDefinition(
                    "job_application_tool",
                    "application",
                    "Fetches application state, stage, and job application level workflow details.",
                    ToolSource.MCP,
                    Set.of("application_read", "workflow_read"),
                    Set.of("jobApplicationId"),
                    Set.of("recruiter_copilot_agent", "offer_approval_agent"),
                    List.of("application", "job application", "stage", "workflow", "submission")
            ),
            new RecruitingToolDefinition(
                    "suggest_interview_slots",
                    "interview",
                    "Calculates suggested interview slots for the candidate and panel.",
                    ToolSource.INTERNAL,
                    Set.of("calendar_read", "schedule_write"),
                    Set.of("candidateId", "panelName"),
                    Set.of("interview_scheduling_agent"),
                    List.of("interview", "schedule", "panel", "slot", "availability")
            ),
            new RecruitingToolDefinition(
                    "update_candidate_status",
                    "candidate",
                    "Persists durable candidate workflow state updates to the relational store.",
                    ToolSource.INTERNAL,
                    Set.of("db_update", "status_write"),
                    Set.of("candidateId"),
                    Set.of("candidate_status_agent", "interview_scheduling_agent", "offer_approval_agent"),
                    List.of("update", "status", "advance", "stage", "workflow")
            )
    );

    public List<RecruitingToolDefinition> allTools() {
        return toolDefinitions;
    }
}
