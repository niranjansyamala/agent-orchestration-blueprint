# Recruiting Multi-Agent Platform

Production-shaped Java platform for recruiting and hiring workflows using:

- Spring Boot 3 / Java 21
- LangChain4j for model-driven intent routing
- LangGraph4j for orchestration workflows
- LangSmith tracing hooks
- Redis for short-term session memory
- PostgreSQL for local workflow and registry persistence
- JWT-secured service-to-service calls
- MCP server for recruiting tools

## Architecture

This repository is a monorepo, but it produces independently deployable services:

- `orchestration-service`
  Public API, intent analysis, workflow orchestration, pause/resume, audit trail, Redis session memory.
- `execution-service`
  Executes routed agent work, calls tools, persists user record state, integrates with the MCP server.
- `registry-service`
  Maintains agent registry and resolves `target_agent` for a given intent and tenant context.
- `recruiting-mcp-server`
  Exposes recruiting tools over MCP:
  `candidate_tool`, `requisition_tool`, `job_application_tool`.
- `platform-common`
  Shared DTOs, enums, and model contracts.

## Control / Execution / Memory Planes

- Control plane:
  `orchestration-service` receives user requests, summarizes session context, calls the router, resolves the best agent through the registry, and advances the workflow graph.
- Execution plane:
  `execution-service` runs the target agent, invokes recruiting tools through MCP, and performs downstream REST/DB/tool work.
- Memory plane:
  Redis stores short-term session state, while relational persistence stores workflow steps, audit history, HITL status, and user record state.

## Service Discovery and `target_agent`

`target_agent` metadata is maintained in `registry-service`, not hardcoded inside the orchestrator.

Flow:

1. `orchestration-service` determines intent from the user query.
2. It calls `POST /internal/v1/agent-registry/resolve`.
3. `registry-service` returns the active agent id and dispatch metadata.
4. `orchestration-service` calls `execution-service` with that resolved agent.

This is effectively an application-level service discovery layer for agents. In production, the service URLs themselves should be supplied by Kubernetes Service DNS, service mesh, or platform discovery, while agent resolution remains a business-level registry concern.

## Internal Security

Internal service-to-service traffic uses JWT bearer tokens.

Why JWT instead of opaque internal tokens:

- Stateless verification scales better across independently deployed services.
- No central token introspection dependency on the hot path.
- Easier fit for multi-service and multi-cluster deployments.

Current implementation:

- Shared-secret HS256 JWTs for local simplicity
- Issuer: `recruiting-platform`
- Audience-bound validation per service

Production recommendation:

- Move to asymmetric signing using RSA or EC keys
- Rotate keys through a secret manager
- Publish keys through JWKS if cross-cluster verification is needed

## MCP Integration

The MCP server runs as its own deployable service and is protected by JWT resource-server security.

Exposed tools:

- `candidate_tool`
- `requisition_tool`
- `job_application_tool`

Integration path:

1. `execution-service` signs a JWT for audience `recruiting-mcp-server`
2. It connects to the MCP endpoint
3. The requested recruiting tool is executed
4. The tool result is returned to the agent runtime

This keeps recruiting-domain tools isolated from orchestration logic and allows the MCP tier to scale independently based on tool throughput.

## MCP Tool Narrowing

The execution layer does not expose the full MCP catalog to every agent.

Instead it applies a narrowing flow:

1. Resolve the `target_agent` from the orchestration and registry flow
2. Load tool metadata from the execution-side tool registry
3. Filter tools to the agent's allowed domain
4. Score tools using routing capabilities, metadata presence, and query keywords
5. Pass only the top-K tools into the active execution context
6. Reject any tool call outside that shortlist

This means a future MCP server can expose 100+ tools while a candidate-screening execution still sees only a small set such as:

- `candidate_tool`
- `requisition_tool`

and an interview-scheduling execution sees only:

- `suggest_interview_slots`
- `update_candidate_status`

Key implementation points:

- Tool metadata registry:
  [RecruitingToolRegistry.java](/Users/nsyamala/Fusion%20HCM%20Arch/execution-service/src/main/java/com/recruiting/platform/execution/toolregistry/RecruitingToolRegistry.java)
- Top-K selection:
  [ToolSelectionService.java](/Users/nsyamala/Fusion%20HCM%20Arch/execution-service/src/main/java/com/recruiting/platform/execution/toolregistry/ToolSelectionService.java)
- Per-request execution context:
  [AgentExecutionContext.java](/Users/nsyamala/Fusion%20HCM%20Arch/execution-service/src/main/java/com/recruiting/platform/execution/service/AgentExecutionContext.java)
- Guarded tool calls:
  [RecruitingToolsService.java](/Users/nsyamala/Fusion%20HCM%20Arch/execution-service/src/main/java/com/recruiting/platform/execution/tools/RecruitingToolsService.java)

## Ports

- `orchestration-service`: `8080`
- `execution-service`: `8081`
- `registry-service`: `8082`
- `recruiting-mcp-server`: `8083`
- Redis: `6379`

## Build

```bash
mvn test
mvn package -DskipTests
```

## Run Locally

Start PostgreSQL on port `5431` and create the local service databases:

```bash
createdb -h localhost -p 5431 registry
createdb -h localhost -p 5431 execution
createdb -h localhost -p 5431 orchestration
```

Start Redis as well.

Then run the services in separate terminals:

```bash
mvn -pl registry-service spring-boot:run
```

```bash
mvn -pl recruiting-mcp-server spring-boot:run
```

```bash
mvn -pl execution-service spring-boot:run
```

```bash
mvn -pl orchestration-service spring-boot:run
```

Optional environment variables:

```bash
export OPENAI_API_KEY=your-key
export INTERNAL_JWT_SECRET=changeit-changeit-changeit-changeit
export INTERNAL_JWT_ISSUER=recruiting-platform
export REGISTRY_BASE_URL=http://localhost:8082
export EXECUTION_BASE_URL=http://localhost:8081
export RECRUITING_MCP_BASE_URL=http://localhost:8083
```

If `OPENAI_API_KEY` is not set, routing falls back to heuristics where supported by the router implementation.

## Sample Request

```bash
curl -X POST http://localhost:8080/api/v1/agent-requests \
  -H 'Content-Type: application/json' \
  -H 'X-Tenant-Id: tenant-a' \
  -H 'X-Session-Id: session-1001' \
  -H 'X-User-Id: recruiter-42' \
  -d '{
    "query": "Show me the current status for candidate CAND-1001",
    "candidateId": "CAND-1001",
    "requisitionId": "REQ-2201",
    "jobApplicationId": "APP-7781"
  }'
```

## Why Independent Scaling Works In A Monorepo

A monorepo does not mean a monolith.

Each module builds a separate jar:

- `orchestration-service/target/orchestration-service-0.0.1-SNAPSHOT.jar`
- `execution-service/target/execution-service-0.0.1-SNAPSHOT.jar`
- `registry-service/target/registry-service-0.0.1-SNAPSHOT.jar`
- `recruiting-mcp-server/target/recruiting-mcp-server-0.0.1-SNAPSHOT.jar`

Recommended production scaling model:

- Scale `orchestration-service` on request concurrency and workflow latency
- Scale `execution-service` on tool load, model calls, and job throughput
- Scale `recruiting-mcp-server` on tool invocation volume
- Keep `registry-service` smaller, but highly available

Typical production deployment:

- One container image per module
- Separate Kubernetes Deployment and Service per module
- Independent HPAs
- Redis as managed cache
- Managed PostgreSQL
- External secret management for JWT keys and model credentials

## Current Local-First Tradeoffs

For faster iteration, the project currently uses:

- Local PostgreSQL instead of managed PostgreSQL
- HS256 shared secret JWTs instead of asymmetric keys
- In-memory/local sample recruiting tool data

These choices are intentional for developer speed and can be replaced without changing the high-level service boundaries.
# agent-orchestration-blueprint
