# AcctAtlas API Gateway

Edge gateway for AccountabilityAtlas. Routes external requests to internal services, handles JWT validation, CORS, and rate limiting.

## Prerequisites

- **Docker Desktop** (for Redis)
- **Git**

JDK 21 is managed automatically by the Gradle wrapper via [Foojay Toolchain](https://github.com/gradle/foojay-toolchain) -- no manual JDK installation required.

## Clone and Build

```bash
git clone <repo-url>
cd AcctAtlas-api-gateway
```

Build the project (downloads JDK 21 automatically on first run):

```bash
# Linux/macOS
./gradlew build

# Windows
gradlew.bat build
```

## Local Development

### Start dependencies

```bash
docker-compose up -d
```

This starts Redis 7 for rate limiting.

### Run the service

```bash
# Linux/macOS
./gradlew bootRun

# Windows
gradlew.bat bootRun
```

The gateway starts on **http://localhost:8080**.

### Run tests

```bash
./gradlew test
```

### Code formatting

Formatting is enforced by [Spotless](https://github.com/diffplug/spotless) using Google Java Format.

```bash
# Check formatting
./gradlew spotlessCheck

# Auto-fix formatting
./gradlew spotlessApply
```

### Full quality check

Runs Spotless, Error Prone, tests, and JaCoCo coverage verification (80% minimum):

```bash
./gradlew check
```

## Docker Image

Build a Docker image locally using [Jib](https://github.com/GoogleContainerTools/jib) (no Dockerfile needed):

```bash
./gradlew jibDockerBuild
```

Build and start the full stack (gateway + Redis) in Docker:

```bash
./gradlew composeUp
```

Stop docker-compose services:

```bash
./gradlew composeDown
```

## Project Structure

```
src/main/java/com/accountabilityatlas/gateway/
  config/        Spring configuration (JWT, CORS)
  filter/        Gateway filters (JWT authentication)

src/main/resources/
  application.yml          Shared config
  application-local.yml    Local dev overrides

src/test/java/.../
  filter/        Filter unit tests
  integration/   Integration tests (TestContainers)
```

## Key Gradle Tasks

| Task | Description |
|------|-------------|
| `bootRun` | Run the service locally (uses `local` profile) |
| `test` | Run all tests |
| `check` | Full quality gate (format + analysis + tests + coverage) |
| `spotlessApply` | Auto-fix code formatting |
| `jibDockerBuild` | Build Docker image |
| `composeUp` | Build image + docker-compose up |
| `composeDown` | Stop docker-compose services |

## Routing

The gateway routes requests with `/api/v1` prefix to backend services:

| Path Pattern | Target Service |
|--------------|----------------|
| `/api/v1/auth/**` | user-service:8081 |
| `/api/v1/users/**` | user-service:8081 |
| `/api/v1/videos/**` | video-service:8082 |
| `/api/v1/locations/**` | location-service:8083 |
| `/api/v1/moderation/**` | moderation-service:8085 |
