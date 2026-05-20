# Deployment Assets

This folder contains a production-oriented starting point for deploying the
Fusion HCM recruiting agent platform to OCI OKE.

## Contents

- `helm/fusion-arch`: Helm chart for the four Spring Boot services
- service `Dockerfile`s in each module directory

## Build Images

Run image builds from the repository root so Docker can use the monorepo as the
build context.

```bash
docker build -f orchestration-service/Dockerfile -t fusion-arch/orchestration-service:local .
docker build -f execution-service/Dockerfile -t fusion-arch/execution-service:local .
docker build -f registry-service/Dockerfile -t fusion-arch/registry-service:local .
docker build -f recruiting-mcp-server/Dockerfile -t fusion-arch/recruiting-mcp-server:local .
```

## Helm Install

Create a values override with your environment-specific secrets and endpoints,
then install the chart.

```bash
helm upgrade --install fusion-arch deploy/helm/fusion-arch \
  --namespace fusion-arch-app \
  --create-namespace \
  -f deploy/helm/fusion-arch/values-prod.yaml
```

## Notes

- The chart expects managed PostgreSQL, Redis, Vault-managed secrets, and OCI
  Queue endpoints to be supplied through values or external secret injection.
- `orchestration-service` remains the only intended public entry point.
- Enable `services.orchestration.privateLoadBalancer.enabled=true` when you want
  an internal OCI load balancer in front of the orchestrator for API Gateway.

