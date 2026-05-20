package com.recruiting.platform.execution.toolregistry;

import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.config.ToolSelectionProperties;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;

@Service
public class ToolSelectionService {

    private final RecruitingToolRegistry toolRegistry;
    private final ToolSelectionProperties properties;

    public ToolSelectionService(RecruitingToolRegistry toolRegistry, ToolSelectionProperties properties) {
        this.toolRegistry = toolRegistry;
        this.properties = properties;
    }

    public ToolSelectionPlan selectFor(ExecuteAgentRequest request) {
        int maxVisibleTools = Math.max(1, properties.getMaxVisibleTools());
        Map<String, Object> metadata = request.request().metadata();
        String normalizedQuery = request.request().query() == null
                ? ""
                : request.request().query().toLowerCase(Locale.ROOT);

        List<ScoredTool> rankedTools = toolRegistry.allTools().stream()
                .filter(tool -> tool.supportedAgents().contains(request.targetAgentId()))
                .map(tool -> scoreTool(tool, request, metadata, normalizedQuery))
                .filter(scoredTool -> scoredTool.score() > 0)
                .sorted(Comparator.comparingInt(ScoredTool::score).reversed()
                        .thenComparing(scoredTool -> scoredTool.definition().toolName()))
                .limit(maxVisibleTools)
                .toList();

        Map<String, Integer> scores = new LinkedHashMap<>();
        Map<String, String> rationales = new LinkedHashMap<>();
        for (ScoredTool rankedTool : rankedTools) {
            scores.put(rankedTool.definition().toolName(), rankedTool.score());
            rationales.put(rankedTool.definition().toolName(), rankedTool.rationale());
        }

        return new ToolSelectionPlan(
                request.targetAgentId(),
                rankedTools.stream().map(scoredTool -> scoredTool.definition().toolName()).toList(),
                scores,
                rationales
        );
    }

    private ScoredTool scoreTool(RecruitingToolDefinition tool,
                                 ExecuteAgentRequest request,
                                 Map<String, Object> metadata,
                                 String normalizedQuery) {
        int score = 0;
        StringJoiner rationale = new StringJoiner("; ");

        if (tool.supportedAgents().contains(request.targetAgentId())) {
            score += 50;
            rationale.add("agent affinity");
        }

        if (request.routingDecision() != null && request.routingDecision().requiredCapabilities() != null) {
            long matchedCapabilities = request.routingDecision().requiredCapabilities().stream()
                    .filter(tool.capabilities()::contains)
                    .count();
            if (matchedCapabilities > 0) {
                score += (int) matchedCapabilities * 10;
                rationale.add("capability match");
            }
        }

        long metadataMatches = tool.requiredMetadataKeys().stream()
                .filter(metadata::containsKey)
                .count();
        if (metadataMatches > 0) {
            score += (int) metadataMatches * 6;
            rationale.add("metadata present");
        }

        long keywordMatches = tool.keywords().stream()
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .filter(normalizedQuery::contains)
                .count();
        if (keywordMatches > 0) {
            score += (int) keywordMatches * 4;
            rationale.add("query keyword match");
        }

        if (request.routingDecision() != null
                && request.routingDecision().intent() != null
                && request.routingDecision().intent().name().toLowerCase(Locale.ROOT).contains(tool.domain())) {
            score += 8;
            rationale.add("intent-domain alignment");
        }

        return new ScoredTool(tool, score, rationale.length() == 0 ? "not selected" : rationale.toString());
    }

    private record ScoredTool(RecruitingToolDefinition definition, int score, String rationale) {
    }
}
