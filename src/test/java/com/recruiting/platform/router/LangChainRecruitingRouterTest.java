package com.recruiting.platform.router;

import com.recruiting.platform.ai.AiModelFactory;
import com.recruiting.platform.config.AiPlatformProperties;
import com.recruiting.platform.domain.IntentType;
import com.recruiting.platform.domain.model.NormalizedAgentRequest;
import com.recruiting.platform.memory.RedisStateStore;
import com.recruiting.platform.support.JsonSupport;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LangChainRecruitingRouterTest {

    @Test
    void routesOfferApprovalQueriesToHumanReviewIntent() {
        AiPlatformProperties properties = new AiPlatformProperties();
        AiModelFactory factory = new AiModelFactory(properties);
        JsonSupport jsonSupport = new JsonSupport(new com.fasterxml.jackson.databind.ObjectMapper());
        RedisStateStore redisStateStore = new RedisStateStore((StringRedisTemplate) null, jsonSupport);
        LangChainRecruitingRouter router = new LangChainRecruitingRouter(factory, redisStateStore);

        NormalizedAgentRequest request = new NormalizedAgentRequest(
                "req_1",
                "default",
                "sess_1",
                "u1",
                "Please get approval for this offer package",
                "web",
                Map.of()
        );

        assertEquals(IntentType.REQUEST_OFFER_APPROVAL, router.route(request).intent());
    }
}
