package com.recruiting.platform.orchestration.router;

import com.recruiting.platform.common.model.NormalizedAgentRequest;
import com.recruiting.platform.common.model.RoutingDecision;

public interface RecruitingRouter {
    RoutingDecision route(NormalizedAgentRequest request);
}
