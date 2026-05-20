package com.recruiting.platform.execution.api;

import com.recruiting.platform.common.model.AgentExecutionResult;
import com.recruiting.platform.common.model.ExecuteAgentRequest;
import com.recruiting.platform.execution.observability.LangSmithTracingService;
import com.recruiting.platform.execution.service.AgentExecutionRuntime;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ExecutionController {

    private final AgentExecutionRuntime runtime;
    private final LangSmithTracingService tracingService;

    public ExecutionController(AgentExecutionRuntime runtime, LangSmithTracingService tracingService) {
        this.runtime = runtime;
        this.tracingService = tracingService;
    }

    @PostMapping("/internal/v1/executions")
    public AgentExecutionResult execute(@Valid @RequestBody ExecuteAgentRequest request) {
        return tracingService.inSpan("execution.execute",
                Map.of("workflow.id", request.workflowId(), "agent.id", request.targetAgentId()),
                () -> runtime.execute(request));
    }
}
