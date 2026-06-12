# CryoGuard Backend — RUNBOOK

Quick reference for running and testing the CryoGuard backend.

## Quick Start (Docker)

```bash
# Navigate to backend directory
cd C:\Users\meteg\IdeaProjects\CryoGuard

# Build and start backend in Docker
docker-compose up --build

# Verify backend is up
curl http://localhost:8080/actuator/health
# Should return {"status":"UP"}
```

## Run Frontend Locally (with pnpm)

```bash
# Navigate to frontend directory
cd C:\Users\meteg\IdeaProjects\cryoguard-webapp

# Install dependencies (if needed)
pnpm install

# Start development server
pnpm dev
# Opens at http://localhost:5173
```

## Development Mode (without Docker)

```bash
cd C:\Users\meteg\IdeaProjects\CryoGuard

# Run with Maven wrapper
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Or using Java directly
java -jar target/CryoGuard-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

## Test Login

Use the default admin credentials (create via sign-up):

```bash
# Sign up (creates new user)
curl -X POST http://localhost:8080/api/v1/auth/sign-up \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@cryoguard.com","password":"admin123","role":"ADMINISTRATOR","telefono":"+51900000000"}'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@cryoguard.com","password":"admin123"}'
```

## Test Endpoints

```bash
# Use the token from login response
TOKEN="<your-jwt-token>"

# Get dashboard stats
curl http://localhost:8080/api/v1/dashboard/stats \
  -H "Authorization: Bearer $TOKEN"

# Get operators
curl "http://localhost:8080/api/v1/users?role=OPERATOR" \
  -H "Authorization: Bearer $TOKEN"

# Get alerts
curl http://localhost:8080/api/v1/alertas \
  -H "Authorization: Bearer $TOKEN"

# Get containers
curl http://localhost:8080/api/v1/containers \
  -H "Authorization: Bearer $TOKEN"

# Get devices
curl http://localhost:8080/api/v1/devices \
  -H "Authorization: Bearer $TOKEN"

# Get active trips
curl "http://localhost:8080/api/v1/viajes?estado=activo&limit=3" \
  -H "Authorization: Bearer $TOKEN"

# Get routes
curl http://localhost:8080/api/v1/routes \
  -H "Authorization: Bearer $TOKEN"

# Get IA precision
curl http://localhost:8080/api/v1/dashboard/ia/precision \
  -H "Authorization: Bearer $TOKEN"

# Get IA recommendations
curl http://localhost:8080/api/v1/dashboard/ia/recomendaciones \
  -H "Authorization: Bearer $TOKEN"
```

## Running Tests

```bash
cd C:\Users\meteg\IdeaProjects\CryoGuard

# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=LoginFlowIntegrationTest

# Run integration tests only
./mvnw test -Dtest="LoginFlowIntegrationTest,DashboardStatsIntegrationTest,SmokeTest"

# Run with coverage
./mvnw test jacoco:report
```

## CORS Configuration

The backend is configured to allow requests from:
- `http://localhost:5173` (Vue default dev port)
- `http://localhost:3000` (Alternative dev port)
- `http://127.0.0.1:5173` (IPv4 localhost variant)

If you need to add more origins, edit:
`src/main/java/com/example/cryoguard/iam/infrastructure/authorization/sfs/configuration/WebSecurityConfiguration.java`

## Environment Variables

Copy `.env.example` to `.env` and configure:

```bash
SPRING_PROFILES_ACTIVE=dev
SERVER_PORT=8080
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION_MS=86400000
```

## H2 Console (Development)

When running with `SPRING_H2_CONSOLE_ENABLED=true`:
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:cryoguard` (or file path in Docker)
- Username: `sa`
- Password: (empty)

## Stopping Docker

```bash
# Stop containers (keep volumes)
docker-compose down

# Stop and remove volumes (delete data)
docker-compose down -v

# Remove images
docker-compose down --rmi all
```