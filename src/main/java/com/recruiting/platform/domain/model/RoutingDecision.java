package com.recruiting.platform.domain.model;

import com.recruiting.platform.domain.IntentType;

import java.io.Serializable;
import java.util.List;

public record RoutingDecision(
        IntentType intent,
        String targetAgentType,
        double confidence,
        List<String> requiredCapabilities,
        boolean humanReviewRequired
) implements Serializable {
}
