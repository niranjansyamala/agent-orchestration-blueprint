package com.recruiting.platform.orchestration.memory;

import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.support.JsonSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Component
public class RedisStateStore {

    private static final Logger log = LoggerFactory.getLogger(RedisStateStore.class);
    private static final Duration SESSION_TTL = Duration.ofHours(24);
    private static final Duration WORKFLOW_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final JsonSupport jsonSupport;

    public RedisStateStore(StringRedisTemplate redisTemplate, JsonSupport jsonSupport) {
        this.redisTemplate = redisTemplate;
        this.jsonSupport = jsonSupport;
    }

    public void saveSession(NormalizedAgentRequest request, String workflowId, String lastIntent) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("tenant_id", request.tenantId());
        payload.put("session_id", request.sessionId());
        payload.put("active_workflow_id", workflowId);
        payload.put("last_intent", lastIntent);
        payload.put("recent_query", request.query());
        writeValue("session:" + request.tenantId() + ":" + request.sessionId(), payload, SESSION_TTL);
    }

    public Map<String, Object> getSession(String tenantId, String sessionId) {
        return readValue("session:" + tenantId + ":" + sessionId);
    }

    public void saveWorkflowState(String workflowId, Map<String, Object> state) {
        writeValue("workflow:" + workflowId, state, WORKFLOW_TTL);
    }

    private void writeValue(String key, Map<String, Object> value, Duration ttl) {
        if (redisTemplate == null) {
            return;
        }
        try {
            redisTemplate.opsForValue().set(key, jsonSupport.write(value), ttl);
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable while writing key {}", key);
        }
    }

    private Map<String, Object> readValue(String key) {
        if (redisTemplate == null) {
            return Map.of();
        }
        try {
            return jsonSupport.readMap(redisTemplate.opsForValue().get(key));
        } catch (DataAccessException ex) {
            log.warn("Redis unavailable while reading key {}", key);
            return Map.of();
        }
    }
}
