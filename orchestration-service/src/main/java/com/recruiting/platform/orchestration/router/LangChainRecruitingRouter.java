package com.recruiting.platform.orchestration.router;

import com.recruiting.platform.common.domain.IntentType;
import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.model.RoutingDecision;
import com.recruiting.platform.orchestration.ai.AiModelFactory;
import com.recruiting.platform.orchestration.memory.RedisStateStore;
import dev.langchain4j.service.AiServices;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LangChainRecruitingRouter implements RecruitingRouter {

    private final AiModelFactory aiModelFactory;
    private final RedisStateStore redisStateStore;

    public LangChainRecruitingRouter(AiModelFactory aiModelFactory, RedisStateStore redisStateStore) {
        this.aiModelFactory = aiModelFactory;
        this.redisStateStore = redisStateStore;
    }

    @Override
    public RoutingDecision route(NormalizedAgentRequest request) {
        Map<String, Object> sessionState = redisStateStore.getSession(request.tenantId(), request.sessionId());
        String sessionSummary = sessionState.isEmpty() ? "No prior context" : sessionState.toString();

        return aiModelFactory.chatModel()
                .map(model -> {
                    RecruitingRouterAssistant assistant = AiServices.builder(RecruitingRouterAssistant.class)
                            .chatModel(model)
                            .build();
                    try {
                        return assistant.route(request.query(), sessionSummary);
                    } catch (RuntimeException ex) {
                        return heuristicRoute(request.query());
                    }
                })
                .orElseGet(() -> heuristicRoute(request.query()));
    }

    private RoutingDecision heuristicRoute(String query) {
        String normalized = query.toLowerCase(Locale.ROOT);
        if ((normalized.contains("offer") || normalized.contains("compensation"))
                && (normalized.contains("approve") || normalized.contains("approval"))) {
            return new RoutingDecision(IntentType.REQUEST_OFFER_APPROVAL, "offer_approval_agent", 0.92,
                    List.of("approval_workflow", "status_update"), true);
        }
        if (normalized.contains("schedule") || normalized.contains("interview")) {
            return new RoutingDecision(IntentType.SCHEDULE_INTERVIEW, "interview_scheduling_agent", 0.88,
                    List.of("calendar_lookup", "status_update"), false);
        }
        if (normalized.contains("status") || normalized.contains("move candidate") || normalized.contains("advance candidate")) {
            return new RoutingDecision(IntentType.UPDATE_CANDIDATE_STATUS, "candidate_status_agent", 0.87,
                    List.of("db_read", "db_update"), false);
        }
        if (normalized.contains("screen") || normalized.contains("resume") || normalized.contains("fit")) {
            return new RoutingDecision(IntentType.SCREEN_CANDIDATE, "candidate_screening_agent", 0.89,
                    List.of("candidate_lookup", "job_lookup", "fit_summary"), false);
        }
        if (normalized.contains("brief") || normalized.contains("summary")) {
            return new RoutingDecision(IntentType.GENERATE_RECRUITER_BRIEF, "recruiter_copilot_agent", 0.85,
                    List.of("candidate_lookup", "job_lookup", "brief_generation"), false);
        }
        return new RoutingDecision(IntentType.GENERAL_RECRUITING_SUPPORT, "recruiter_copilot_agent", 0.70,
                List.of("brief_generation"), false);
    }
}
