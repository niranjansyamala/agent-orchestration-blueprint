package com.recruiting.platform.router;

import com.recruiting.platform.domain.model.NormalizedAgentRequest;
import com.recruiting.platform.domain.model.RoutingDecision;

public interface RecruitingRouter {
    RoutingDecision route(NormalizedAgentRequest request);
}
