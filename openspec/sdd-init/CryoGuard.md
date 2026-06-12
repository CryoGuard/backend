# SDD Init — CryoGuard

**Project**: CryoGuard  
**Detected**: 2026-06-11  
**Persistence**: openspec  
**Strict TDD**: true (default — test runner detected)

## Stack

| Component | Technology |
|-----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 4.0.6 |
| Build | Maven + Maven wrapper (`./mvnw`) |
| Database | H2 (dev) / PostgreSQL NeonDB (prod) |
| Auth | JWT (jjwt 0.12.6) + Spring Security |
| API Docs | SpringDoc OpenAPI 2.8.4 |
| Annotations | Lombok |
| Persistence | Spring Data JPA + JPA Auditing |

## Architecture

**Pattern**: DDD with CQRS

**Bounded Contexts** (8):
- `iam` — authentication, user management
- `monitoring` — container tracking, telemetry, events
- `evaluation` — alerts, monitoring rules
- `logistics` — routes, geofences
- `actuators` — device-side control (no frontend yet)
- `operations` — mobile app future
- `notifications` — settings config only
- `audit` — audit logs

**Layer per context**: `presentation` → `application` → `domain` → `infrastructure`

## OpenSpec Structure

```
openspec/
├── config.yaml          # Phase rules + context
├── specs/
│   ├── iam/spec.md
│   ├── monitoring/spec.md
│   ├── evaluation/spec.md
│   ├── logistics/spec.md
│   ├── actuators/spec.md
│   ├── operations/spec.md
│   ├── notifications/spec.md
│   └── audit/spec.md
└── changes/
    ├── state.yaml
    ├── proposal.md
    ├── design-*.md
    └── tasks*.md
```

## Active Change

- **Name**: `refactor-backend-complete`
- **Status**: `apply_in_progress` (stuck — needs verify)
- **Completed contexts**: iam, monitoring, evaluation, logistics
- **Pending contexts**: actuators, operations, notifications, audit
- **Next phase**: `verify`

## Conventions (from openspec/config.yaml)

- Scenarios: Given/When/Then format
- RFC 2119 keywords (MUST, SHALL, SHOULD, MAY)
- Entity relationship per bounded context in design
- Tasks grouped by context, hierarchical numbering
- Rollback plan required for risky changes