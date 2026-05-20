package com.recruiting.platform.router;

import com.recruiting.platform.domain.model.RoutingDecision;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

public interface RecruitingRouterAssistant {

    @SystemMessage("""
            You are an intent router for a recruiting platform.
            Return only a structured routing decision.
            Use these intents only:
            SCREEN_CANDIDATE, SCHEDULE_INTERVIEW, UPDATE_CANDIDATE_STATUS,
            GENERATE_RECRUITER_BRIEF, REQUEST_OFFER_APPROVAL, GENERAL_RECRUITING_SUPPORT.
            Offer approval, compensation changes, or final hiring decisions must set humanReviewRequired to true.
            Required capabilities must be concise platform capabilities.
            """)
    @UserMessage("""
            Session summary: {{sessionSummary}}
            User query: {{query}}
            """)
    RoutingDecision route(@V("query") String query, @V("sessionSummary") String sessionSummary);
}
