# Design: cryoguard-vue-backend-mvp

## Technical Approach

Adapt the 4 existing BCs (`iam`, `monitoring`, `evaluation`, `logistics`) to the Vue 3 contract. Extend entities, restructure resources, add 7 new endpoints, wire cross-BC reads via in-process ACL facades. No new BCs. Strict TDD. Synchronous in-process reads.

**Conventions** (actual project, not the ddd-context-scaffold template): `com.example.cryoguard.{bc}`; `domain/{aggregates,entities,valueobjects,commands,queries,services}`, `application/internal/{commandservices,queryservices}`, `infrastructure/persistence/jpa/repositories`, `presentation/{resources,assemblers,controllers}` for evaluation/monitoring/logistics, `interfaces/{rest,acl}` for iam.

## Architecture Decisions

| # | Decision | Choice |
|---|----------|--------|
| **D1** | Cross-BC | **ACL facades**: consuming BC declares interface in `interfaces/acl/`; providing BC ships `@Component` impl. Precedent: `IamContextFacade`. Events async (useless here); HTTP overkill. |
| **D2** | Alert path `/alerts`→`/alertas` | **Rename + update tests in same commit**. No alias. |
| **D3** | Route multi-container | **New `RouteContainerAssignment(routeId, containerId, assignedAt)`**, drop `Route.containerId`. |
| **D4** | `connected`/`online` | **Derived**: `Duration.between(lastUpdate, now).toMinutes() < 5`. |
| **D5** | IA precision/recs | **Pure functions**: `round(confirmed*100/total)`, 0 if total=0; 3 rules, max 5 results. Marked synthetic. |
| **D6** | `telefono` | `@Size(max=20)`, nullable, no format. |
| **D7** | Dashboard subtexto | `+N esta semana` (users 7d); `N conectadas` (lastUpdate<5m); `N finalizados hoy` (completed today); `N críticas` (CRITICAL+unresolved). |
| **D8** | Tests | JUnit 5 + Mockito. Unit (RED), `@WebMvcTest`, `@SpringBootTest` for cross-BC. Names `should_X_when_Y`. |

## Data Flow

**Dashboard** — `GET /api/v1/dashboard/stats` → `DashboardController` (monitoring) → `DashboardStatsService` → UserQueryService (iam) + ContainerRepository + RouteRepository (logistics) + AlertRepository (evaluation).

**Cross-BC iam→logistics** — `GET /api/v1/users?role=OPERATOR` → assembler injects `LogisticsQueryService` ACL → `LogisticsContextFacade` → `RouteRepository.countByAuthorizedOperatorIdAndStatusIn(...)` → `RouteStatsDto(active, completed)`.

## File Changes (Create | Modify)

**iam**: `User.java`+telefono; `SignUpCommand/UpdateUserCommand/ResetUserPinCommand`+telefono/PIN; `SignUpResource/UpdateUserResource/UserResource`+new fields; `ResetPinResponseResource` (new); `UserResourceFromEntityAssembler` (inject `LogisticsQueryService`, format `ultimaActividad` es-PE); assemblers pass telefono; `LogisticsQueryService` interface + `RouteStatsDto` (new); `UserCommandServiceImpl` implements PIN reset + telefono handling; `UsersController`+`/reset-password` endpoint.

**monitoring**: `Container.java`+firmware/locked/coolingActive; `ContainerResource`+`ContainerResourceAssembler` (compute connected); `DeviceResource` (new); `ContainersController` updated; `DevicesController` (new, `/devices`); `DashboardController` (new, `/dashboard/stats`+`/ia/*`, inject eval's `IaMetricsService`); `DeviceAggregationService` (new, joins container+telemetry+alerts); `DashboardStatsService` (new, 4 KPIs per D7); `RouteQueryService` interface + `RouteInfoDto` (new, in monitoring); `RouteContextFacade` (new, in monitoring, delegates to logistics); `ContainerRepository`+`countByLastUpdateAfter`.

**evaluation**: `Alert.java`+triggerValue/tripId; **`AlertStatus.java` REPLACE** values (OPEN/ACK/RES→ACTIVA/PENDIENTE/CONFIRMADA — `Alert` uses Booleans, no DB impact); `AlertResource` replaced by `AlertIncidentResource` record; `AlertSummaryResource` (new, dashboard card); `RecommendationResource` (new); `AlertAssembler` remap severity+derive status+format ts; `AlertsController` `@RequestMapping("/api/v1/alertas")`+filters+new endpoints; `IaMetricsService` (new, `computePrecision`/`computeRecommendations`); `ContainerQueryService`+`RouteQueryService` interfaces (new); `ContainerContextFacade`+`RouteContextFacade` (new, safe fallback); `AlertRepository`+counts/timestamp queries.

**logistics**: `Route.java` drop `containerId`, add `authorizedOperatorId` (Long, no JPA) + `@OneToMany` `containerAssignments`; `RouteContainerAssignment.java` (new, `route` `@ManyToOne`, `containerId` Long, `assignedAt`); `RouteStatus.java`+`INITIATED`/`IN_PROGRESS`; `RouteResource` replaced with full Vue shape; `RouteAssembler` inject `IamContextFacade`+`ContainerQueryService`+`AlertQueryService`, compute `progreso`; `RoutesController` updated (accepts `containerIds[]`); `ViajesController` (new, `/viajes`); `RouteRepository` derived queries + `countByAuthorizedOperatorId*`; `LogisticsContextFacade` (new, implements iam's interface); `RouteContextFacade` (new, implements monitoring's interface via `RouteContainerAssignment` lookup).

## Interfaces / Contracts

```java
// iam consumes logistics
public interface LogisticsQueryService { RouteStatsDto getStatsByOperator(Long operatorId); }
public record RouteStatsDto(int activeCount, int completedCount) {}

// logistics implements
@Component class LogisticsContextFacade implements LogisticsQueryService {
    public RouteStatsDto getStatsByOperator(Long id) {
        return new RouteStatsDto(
            repo.countByAuthorizedOperatorIdAndStatusIn(id, List.of(INITIATED, IN_PROGRESS)),
            repo.countByAuthorizedOperatorIdAndStatus(id, completed));
    }
}

// evaluation consumes monitoring + logistics
public interface ContainerQueryService { String getCode(Long id); }
public interface RouteQueryService { String getCode(Long id); }

// monitoring consumes logistics
public interface RouteQueryService { RouteInfoDto getInfoByContainerCode(String code); }
public record RouteInfoDto(String code, String status) {}
```

All DTOs `record`s. Impls return safe defaults (`String.valueOf(id)`, `"Sin viaje"`).

## Entity Relationships

```
iam.User.telefono (String, nullable, @Size 20)
logistics.Route.authorizedOperatorId → iam.User.id (Long, no JPA)
logistics.Route 1—N RouteContainerAssignment (containerId:Long, assignedAt)
logistics.RouteStatus { active, completed, cancelled, INITIATED, IN_PROGRESS }
evaluation.Alert.containerId → monitoring.Container.id; .tripId → logistics.Route.id (nullable)
evaluation.AlertStatus { ACTIVA, PENDIENTE, CONFIRMADA }  (replaces old enum, no DB impact)
evaluation.Alert.acknowledged:Boolean, resolved:Boolean  (derive status)
monitoring.Container.{id, containerId:String, firmware, locked, coolingActive}
```

## Testing Strategy

| Layer | Approach |
|-------|----------|
| Unit (RED) | JUnit 5 + Mockito, `@Mock` repos, `should_X_when_Y` |
| Controller | `@WebMvcTest` + `MockMvc`, JSON path |
| Integration | `@SpringBootTest` H2, one test per cross-BC path |
| Regression | Old `AlertResource`+`/api/v1/alerts` tests: update same commit. `mvn test` must pass. |

## Migration / Rollout

No migration scripts. H2 `ddl-auto=create-drop` regenerates. All new columns nullable. `RouteContainerAssignment` is new. `AlertStatus` enum change has no DB impact (entity uses Booleans). Scope guard: no new BCs, no Flyway, no backfill.

## Open Questions

- **`AlertStatus` enum**: verified `Alert.java` uses `Boolean acknowledged/resolved` (lines 44-60), not enum. Safe to replace values.
- **Old `AlertResource`+`/api/v1/alerts` tests**: sdd-apply updates in same commit as rename.
- **Dashboard `/ia/*` owner**: spec says evaluation; `/stats` is monitoring. **Decision**: single `monitoring/.../DashboardController.java` injects eval's `IaMetricsService` via ACL.
- **`progreso`**: no waypoints model. **Decision**: use `Route.checkpoints` denominator; default 50% when null.
