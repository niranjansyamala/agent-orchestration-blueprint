package com.recruiting.platform.execution.mcp;

import com.recruiting.platform.execution.config.McpServerProperties;
import com.recruiting.platform.execution.security.InternalJwtTokenProvider;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class RecruitingMcpClient {

    private final McpServerProperties properties;
    private final InternalJwtTokenProvider tokenProvider;

    public RecruitingMcpClient(McpServerProperties properties, InternalJwtTokenProvider tokenProvider) {
        this.properties = properties;
        this.tokenProvider = tokenProvider;
    }

    public String candidateProfile(String candidateId) {
        return callTextTool("candidate_tool", Map.of("candidateId", candidateId));
    }

    public String requisitionSummary(String requisitionId) {
        return callTextTool("requisition_tool", Map.of("requisitionId", requisitionId));
    }

    public String jobApplicationSummary(String applicationId) {
        return callTextTool("job_application_tool", Map.of("applicationId", applicationId));
    }

    private String callTextTool(String toolName, Map<String, Object> args) {
        try (McpSyncClient client = buildClient()) {
            McpSchema.CallToolResult result = client.callTool(new McpSchema.CallToolRequest(toolName, args));
            if (result.structuredContent() instanceof Map<?, ?> map && map.get("text") != null) {
                return String.valueOf(map.get("text"));
            }
            if (!result.content().isEmpty() && result.content().getFirst() instanceof McpSchema.TextContent textContent) {
                return textContent.text();
            }
            return String.valueOf(result.structuredContent());
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to call MCP tool " + toolName, ex);
        }
    }

    private McpSyncClient buildClient() {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(properties.getBaseUrl())
                .endpoint(properties.getEndpoint())
                .customizeRequest(builder -> builder.header("Authorization",
                        "Bearer " + tokenProvider.tokenForAudience(properties.getAudience())))
                .build();
        McpSyncClient client = McpClient.sync(transport)
                .clientInfo(new McpSchema.Implementation("execution-service", "1.0.0"))
                .build();
        client.initialize();
        return client;
    }
}
