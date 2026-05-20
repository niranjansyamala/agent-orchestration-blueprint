package com.recruiting.platform.mcp.config;

import com.recruiting.platform.mcp.catalog.RecruitingCatalogService;
import io.modelcontextprotocol.json.jackson3.JacksonMcpJsonMapper;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.transport.HttpServletStatelessServerTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.servlet.Servlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Configuration
public class McpServerConfig {

    @Bean
    public JacksonMcpJsonMapper jacksonMcpJsonMapper() {
        return new JacksonMcpJsonMapper(JsonMapper.builder().build());
    }

    @Bean
    public HttpServletStatelessServerTransport mcpTransport(JacksonMcpJsonMapper mapper) {
        return HttpServletStatelessServerTransport.builder()
                .jsonMapper(mapper)
                .messageEndpoint("/mcp")
                .build();
    }

    @Bean
    public ServletRegistrationBean<Servlet> mcpServlet(HttpServletStatelessServerTransport transport) {
        return new ServletRegistrationBean<>(transport, "/mcp");
    }

    @Bean
    public Object mcpServer(HttpServletStatelessServerTransport transport,
                            JacksonMcpJsonMapper mapper,
                            RecruitingCatalogService catalogService) {
        return McpServer.sync(transport)
                .serverInfo("recruiting-mcp-server", "1.0.0")
                .instructions("Recruiting MCP server exposing candidate, requisition, and job application tools.")
                .requestTimeout(Duration.ofSeconds(10))
                .tools(
                        toolSpec("candidate_tool",
                                "Lookup a candidate profile by candidateId",
                                Map.of("candidateId", Map.of("type", "string", "description", "Candidate identifier")),
                                List.of("candidateId"),
                                (request) -> catalogService.candidate(String.valueOf(request.arguments().get("candidateId")))),
                        toolSpec("requisition_tool",
                                "Lookup a requisition summary by requisitionId",
                                Map.of("requisitionId", Map.of("type", "string", "description", "Requisition identifier")),
                                List.of("requisitionId"),
                                (request) -> catalogService.requisition(String.valueOf(request.arguments().get("requisitionId")))),
                        toolSpec("job_application_tool",
                                "Lookup a job application summary by applicationId",
                                Map.of("applicationId", Map.of("type", "string", "description", "Job application identifier")),
                                List.of("applicationId"),
                                (request) -> catalogService.jobApplication(String.valueOf(request.arguments().get("applicationId"))))
                )
                .build();
    }

    private io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification toolSpec(
            String name,
            String description,
            Map<String, Object> properties,
            List<String> required,
            java.util.function.Function<McpSchema.CallToolRequest, Map<String, Object>> handler) {

        McpSchema.Tool tool = McpSchema.Tool.builder()
                .name(name)
                .description(description)
                .inputSchema(new McpSchema.JsonSchema("object", properties, required, false, Map.of(), Map.of()))
                .build();

        return io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification.builder()
                .tool(tool)
                .callHandler((context, request) -> {
                    Map<String, Object> payload = handler.apply(request);
                    return McpSchema.CallToolResult.builder()
                            .content(List.of(new McpSchema.TextContent(String.valueOf(payload.get("summary")))))
                            .structuredContent(Map.of("text", String.valueOf(payload.get("summary")), "payload", payload))
                            .isError(false)
                            .build();
                })
                .build();
    }
}
