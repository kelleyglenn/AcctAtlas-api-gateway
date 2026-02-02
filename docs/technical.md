# API Gateway - Technical Documentation

## Service Overview

The API Gateway serves as the single entry point for all client traffic to AccountabilityAtlas backend services. It handles cross-cutting concerns including authentication, routing, rate limiting, and request logging.

## Responsibilities

- Request routing to backend services based on path
- JWT token validation and user context injection
- Rate limiting by user tier and endpoint
- Request/response logging for observability
- CORS configuration for web clients
- Health check aggregation for all services

## Technology Stack

| Component | Technology |
|-----------|------------|
| Framework | Spring Cloud Gateway |
| Language | Java 21 |
| Build | Gradle |
| Runtime | JDK 21 (Corretto) |

## Dependencies

- **Redis**: Rate limiting counters, session validation
- **All Backend Services**: Routing targets

## Documentation Index

| Document | Status | Description |
|----------|--------|-------------|
| [api-specification.yaml](api-specification.yaml) | Complete | OpenAPI 3.1 specification |
| [routing-configuration.md](routing-configuration.md) | Planned | Route definitions and configuration |
| [rate-limiting.md](rate-limiting.md) | Planned | Rate limiting rules and implementation |
| [security-filters.md](security-filters.md) | Planned | Authentication and security filter chain |

## Key Design Decisions

### ADR-GW-001: Spring Cloud Gateway over Kong/nginx

**Decision**: Use Spring Cloud Gateway (reactive)

**Rationale**:
- Native Spring integration
- Programmatic route configuration
- Same tech stack as backend services
- Built-in circuit breaker support via Resilience4j

## Service Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| /health | GET | Gateway health check |
| /health/services | GET | Aggregated service health |

All other requests are routed to backend services based on path prefix.

## Configuration

```yaml
# Key configuration properties
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/v1/auth/**,/api/v1/users/**
        - id: video-service
          uri: lb://video-service
          predicates:
            - Path=/api/v1/videos/**
        # ... additional routes
```

## Local Development

```bash
# Start dependencies
docker-compose up -d redis

# Run gateway
./gradlew bootRun

# Gateway available at http://localhost:8080
```
