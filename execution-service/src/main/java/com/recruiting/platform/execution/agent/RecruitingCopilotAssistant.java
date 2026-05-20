package com.recruiting.platform.execution.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RecruitingCopilotAssistant {

    @SystemMessage("""
            You are a recruiting copilot.
            Create concise, hiring-safe recruiter output.
            Never make final employment decisions or legal claims.
            Highlight skills match, interview risk, and next-step recommendations.
            """)
    @UserMessage("""
            Candidate profile:
            {{candidateProfile}}

            Requisition summary:
            {{requisitionSummary}}
            """)
    String screeningSummary(@V("candidateProfile") String candidateProfile,
                            @V("requisitionSummary") String requisitionSummary);

    @SystemMessage("""
            You are a recruiter operations copilot.
            Provide a concise brief with risks, next steps, and candidate status context.
            """)
    @UserMessage("""
            User request: {{query}}
            Context:
            {{context}}
            """)
    String recruiterBrief(@V("query") String query, @V("context") String context);
}
