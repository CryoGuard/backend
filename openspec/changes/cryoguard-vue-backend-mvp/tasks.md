# Tasks: cryoguard-vue-backend-mvp — Connect Backend to Vue Frontend (MVP)

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | 1500–2500 |
| 400-line budget risk | High |
| Chained PRs recommended | No |
| Suggested split | Single PR (academic MVP, shipping over slicing) |
| Delivery strategy | single-pr |
| Chain strategy | size:exception |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: size:exception
400-line budget risk: High

---

## Phase 1: IAM BC — User entity, auth, and reset-password

- [x] **T1.1** — BC: iam | Add `telefono` field to `User.java` entity (`@Size(max=20)`, nullable) | Test: `UserEntityTests.shouldHaveTelefonoField` | Files: `domain/entities/User.java`
- [x] **T1.2** — BC: iam | Add `LogisticsQueryService` interface + `RouteStatsDto` record in `interfaces/acl/` | Test: `LogisticsQueryServiceTest.shouldReturnRouteStatsDto` | Files: `interfaces/acl/LogisticsQueryService.java`, `interfaces/acl/RouteStatsDto.java` | Depends: T4.1
- [x] **T1.3** — BC: iam | Modify `SignUpCommand` to include optional `telefono` | Test: `SignUpCommandTests.shouldAcceptTelefono` | Files: `domain/commands/SignUpCommand.java`
- [x] **T1.4** — BC: iam | Modify `UpdateUserCommand` to include optional `telefono` | Test: `UpdateUserCommandTests.shouldAcceptTelefono` | Files: `domain/commands/UpdateUserCommand.java`
- [x] **T1.5** — BC: iam | Add `ResetUserPinCommand` record + `ResetUserPinCommandService` | Test: `ResetUserPinCommandServiceTests.shouldGenerate4DigitPin`, `shouldReturn404WhenUserNotFound` | Files: `domain/commands/ResetUserPinCommand.java`, `application/ResetUserPinCommandService.java`
- [x] **T1.6** — BC: iam | Add `ResetPinResponseResource` record (`newPin: String`) | Test: `ResetPinResponseResourceTests` | Files: `presentation/resources/ResetPinResponseResource.java`
- [x] **T1.7** — BC: iam | Add `POST /users/{id}/reset-password` endpoint in `UsersController` | Test: `UsersControllerTests.should_reset_password_returns_new_pin`, `should_return_404_when_user_not_found` | Files: `presentation/controllers/UsersController.java` | Depends: T1.5, T1.6
- [x] **T1.8** — BC: iam | Create `LogisticsContextFacade` in logistics BC implementing `LogisticsQueryService` | Test: `LogisticsContextFacadeTests.shouldReturnRouteStatsFromRepository` | Files: `logistics/application/LogisticsContextFacade.java` | Depends: T4.1, T4.2
- [x] **T1.9** — BC: iam | Modify `UserResource` to include `telefono`, `pinBloqueado`, `viajesAsignados`, `viajesCompletados`, `ultimaActividad` | Test: `UserResourceTests` | Files: `presentation/resources/UserResource.java`
- [x] **T1.10** — BC: iam | Modify `SignUpResource` to include `telefono` | Test: `SignUpResourceTests` | Files: `presentation/resources/SignUpResource.java`
- [x] **T1.11** — BC: iam | Modify `UpdateUserResource` to include `telefono` | Test: `UpdateUserResourceTests` | Files: `presentation/resources/UpdateUserResource.java`
- [x] **T1.12** — BC: iam | Update `UserResourceFromEntityAssembler` to inject `LogisticsQueryService`, compute `pinBloqueado`, `viajesAsignados`, `viajesCompletados`, format `ultimaActividad` (es-PE `dd/MM/yyyy HH:mm`, "Sin actividad" when null) | Test: `UserResourceFromEntityAssemblerTests.should_compute_all_fields`, `should_return_sin_actividad_when_lastLogin_null` | Files: `interfaces/rest/assemblers/UserResourceFromEntityAssembler.java` | Depends: T1.2
- [x] **T1.13** — BC: iam | Update `UserCommandServiceImpl` to handle `telefono` on sign-up and update | Test: `UserCommandServiceImplTests.should_persist_telefono_on_signup`, `should_update_telefono` | Files: `application/impl/UserCommandServiceImpl.java`
- [x] **T1.14** — BC: iam | Update `AuthController` login response to include computed fields | Test: `IamAuthControllerTest.should_return_user_with_computed_fields_on_successful_login`, `should_return_pinBloqueado_true_when_user_status_is_locked` | Files: `presentation/controllers/IamAuthController.java` (or wherever login lives) | Depends: T1.12

---

## Phase 2: Evaluation BC — Alert payload restructure, new endpoints

- [x] **T2.1** — BC: evaluation | Add `triggerValue: String` and `tripId: Long` fields to `Alert.java` entity | Test: `AlertEntityTests.shouldHaveTriggerValueAndTripId` | Files: `domain/entities/Alert.java`
- [x] **T2.2** — BC: evaluation | Replace `AlertStatus.java` enum values: OPEN→ACTIVA, ACKNOWLEDGED→PENDIENTE, RESOLVED→CONFIRMADA (Alert uses Booleans, no DB impact) | Test: `AlertStatusTests` | Files: `domain/valueobjects/AlertStatus.java`
- [x] **T2.3** — BC: evaluation | Create `AlertIncidentResource` record with fields: `id`, `severity`, `status`, `message`, `boxId`, `tripCode`, `value`, `timestamp` | Test: `AlertIncidentResourceTests` | Files: `presentation/resources/AlertIncidentResource.java`
- [x] **T2.4** — BC: evaluation | Create `AlertSummaryResource` record for dashboard card: `id`, `tipo`, `caja`, `tiempo`, `severidad` | Test: `AlertSummaryResourceTests` | Files: `presentation/resources/AlertSummaryResource.java`
- [x] **T2.5** — BC: evaluation | Create `RecommendationResource` record: `titulo`, `descripcion`, `prioridad`, `confianza` | Test: `RecommendationResourceTests` | Files: `presentation/resources/RecommendationResource.java`
- [x] **T2.6** — BC: evaluation | Add `ContainerQueryService` interface in `interfaces/acl/` (for cross-BC boxId lookup) | Test: `ContainerQueryServiceTest` | Files: `interfaces/acl/ContainerQueryService.java` | Depends: T3.1
- [x] **T2.7** — BC: evaluation | Add `RouteQueryService` interface in `interfaces/acl/` (for cross-BC tripCode lookup) | Test: `RouteQueryServiceTest` | Files: `interfaces/acl/RouteQueryService.java` | Depends: T4.1
- [x] **T2.8** — BC: evaluation | Create `ContainerContextFacade` implementing `ContainerQueryService` (safe fallback: return `String.valueOf(id)`) | Test: `ContainerContextFacadeTests.shouldReturnCodeOrFallback` | Files: `interfaces/acl/ContainerContextFacade.java` | Depends: T2.6, T3.1
- [x] **T2.9** — BC: evaluation | Create `RouteContextFacade` implementing `RouteQueryService` (safe fallback: "Sin viaje" when null, raw Long as string) | Test: `RouteContextFacadeTests.shouldReturnCodeOrSinViajeOrFallback` | Files: `interfaces/acl/RouteContextFacade.java` | Depends: T2.7, T4.1
- [x] **T2.10** — BC: evaluation | Update `AlertAssembler` to: remap severity (CRITICAL→"critica", WARNING/INFO→"advertencia"), derive status from acknowledged+resolved booleans, format timestamp (es-PE `dd/MM/yyyy HH:mm`), resolve `boxId` and `tripCode` via ACLs | Test: `AlertAssemblerTests.should_remap_severity_to_critica`, `should_derive_status_activa_when_not_acknowledged`, `should_format_timestamp_es_pe` | Files: `presentation/assemblers/AlertAssembler.java` | Depends: T2.8, T2.9
- [x] **T2.11** — BC: evaluation | Rename `AlertsController` mapping from `/api/v1/alerts` to `/api/v1/alertas`; add `limit` and `sort` query params; update all existing tests to new path | Test: `AlertsControllerTests.should_map_to_alertas`, `should_return_limited_recent_alerts_for_dashboard`, `should_filter_by_status`, `should_filter_by_severity` | Files: `presentation/controllers/AlertsController.java` | Depends: T2.3, T2.4, T2.10
- [x] **T2.12** — BC: evaluation | Add `PUT /api/v1/alertas/{id}/acknowledge` → sets acknowledged=true, status=PENDIENTE | Test: `should_change_status_to_pendiente_on_acknowledge` | Files: `AlertsController.java` | Depends: T2.11
- [x] **T2.13** — BC: evaluation | Add `PUT /api/v1/alertas/{id}/resolve` → sets resolved=true, status=CONFIRMADA | Test: `should_change_status_to_confirmada_on_resolve` | Files: `AlertsController.java` | Depends: T2.12
- [x] **T2.14** — BC: evaluation | Add `PUT /api/v1/alertas/{id}/escalate` → returns 409 if already CRITICAL | Test: `should_return_409_when_escalating_already_critical_alert` | Files: `AlertsController.java` | Depends: T2.13
- [x] **T2.15** — BC: evaluation | Add `GET /api/v1/dashboard/ia/precision` returning integer % (confirmed/total * 100, 0 if none) | Test: `IaMetricsServiceTests.should_return_80_percent_precision_when_8_of_10_confirmed`, `should_return_0_when_no_alerts_exist`, `should_return_100_when_all_alerts_confirmed` | Files: `application/IaMetricsService.java` | Depends: T2.3
- [x] **T2.16** — BC: evaluation | Add `GET /api/v1/dashboard/ia/recomendaciones` — rule-based recommendations (3 rules, max 5 results, marked synthetic in code) | Test: `IaMetricsServiceTests.should_return_maintenance_recommendation_for_container_with_4_alerts`, `should_return_night_operations_recommendation_when_8_night_alerts`, `should_return_empty_array_when_no_patterns_match`, `should_limit_recommendations_to_maximum_5` | Files: `IaMetricsService.java` | Depends: T2.5, T2.15

---

## Phase 3: Monitoring BC — Container payload fix, device monitoring, dashboard stats

- [x] **T3.1** — BC: monitoring | Add `firmwareVersion: String`, `locked: Boolean`, `coolingActive: Boolean` fields to `Container.java` | Test: `ContainerEntityTests` | Files: `domain/aggregates/Container.java`
- [x] **T3.2** — BC: monitoring | Create `ContainerContextFacade` implementing `ContainerQueryService.getCode(id)` (lookup Container.code by id, fallback `String.valueOf(id)`) | Test: `ContainerContextFacadeTests` | Files: `interfaces/acl/ContainerContextFacade.java` | Depends: T3.1
- [x] **T3.3** — BC: monitoring | Create `RouteQueryService` interface + `RouteInfoDto` record in `interfaces/acl/` (get code + status by container code) | Test: `MonitoringRouteQueryServiceTests` | Files: `interfaces/acl/RouteQueryService.java`, `interfaces/acl/RouteInfoDto.java` | Depends: T4.1
- [x] **T3.4** — BC: monitoring | Create `RouteContextFacade` in monitoring implementing `RouteQueryService` (delegates to logistics, safe fallback) | Test: `MonitoringRouteContextFacadeTests` | Files: `interfaces/acl/RouteContextFacade.java` | Depends: T4.3
- [x] **T3.5** — BC: monitoring | Update `ContainerResource` to Vue shape: `id`, `nombre`, `estado`, `temperature`, `humidity`, `batteryLevel`, `coolingActive`, `firmware`, `locked`, `connected`, `location`, `productType`, `ultimaSync` | Test: `ContainerResourceTests.shouldHaveAllVueFields` | Files: `presentation/resources/ContainerResource.java`
- [x] **T3.6** — BC: monitoring | Update `ContainerResourceAssembler` to compute `connected` (lastUpdate < 5 min ago) and map fields to Vue naming | Test: `ContainerResourceAssemblerTests.should_compute_connected` | Files: `presentation/assemblers/ContainerResourceAssembler.java`
- [x] **T3.7** — BC: monitoring | Create `DeviceResource` record for `/devices` endpoint: `id`, `tripCode`, `temperature`, `humidity`, `battery`, `cooling`, `location`, `online`, `locked`, `status`, `latitude`, `longitude`, `activeAlerts`, `lastSync`, `temperatureWarning` | Test: `DeviceResourceTests` | Files: `presentation/resources/DeviceResource.java`
- [x] **T3.8** — BC: monitoring | Create `DeviceAggregationService` — joins container + latest telemetry + alert count per container, computes `online`, `status`, `temperatureWarning` | Test: `DeviceAggregationServiceTests.should_compute_device_status`, `should_return_offline_when_no_recent_telemetry` | Files: `application/DeviceAggregationService.java` | Depends: T3.1, T3.4
- [x] **T3.9** — BC: monitoring | Create `DevicesController` with `GET /api/v1/devices` | Test: `DevicesControllerTests.should_return_devices_list` | Files: `presentation/DevicesController.java` | Depends: T3.7, T3.8
- [x] **T3.10** — BC: monitoring | Add `countByLastUpdateAfter` method to `ContainerRepository` | Test: `ContainerRepositoryTests` | Files: `infrastructure/persistence/ContainerRepository.java`
- [x] **T3.11** — BC: monitoring | Create `DashboardStatsService` — aggregates 4 KPIs: `operadoresActivos` (count users created 7d), `cajasIoT` (total + conectadas count), `viajesActivos` (count active routes + finalizados today), `alertasActivas` (count unresolved + count critical) | Test: `DashboardStatsServiceTests.should_return_all_4_kpis_with_subtexts` | Files: `application/DashboardStatsService.java`
- [x] **T3.12** — BC: monitoring | Create `DashboardController` with `GET /api/v1/dashboard/stats` | Test: `DashboardControllerTests.should_return_stats_with_4_blocks` | Files: `presentation/DashboardController.java` | Depends: T3.11
- [x] **T3.13** — BC: monitoring | Create `DashboardIaController` with `GET /api/v1/dashboard/ia/precision` and `GET /api/v1/dashboard/ia/recomendaciones` (inject eval's `IaMetricsService`) | Test: `DashboardIaControllerTests` | Files: `presentation/DashboardIaController.java` | Depends: T2.15, T2.16

---

## Phase 4: Logistics BC — Route payload fix, multi-container, active trips

- [x] **T4.1** — BC: logistics | Extend `RouteStatus.java` enum: add `INITIATED`, `IN_PROGRESS` values (keep existing `active`, `completed`, `cancelled`) | Test: `RouteStatusTests` | Files: `domain/valueobjects/RouteStatus.java`
- [x] **T4.2** — BC: logistics | Create `RouteContainerAssignment.java` entity (`route` @ManyToOne, `containerId: Long`, `assignedAt`) | Test: `RouteContainerAssignmentTests` | Files: `domain/entities/RouteContainerAssignment.java`
- [x] **T4.3** — BC: logistics | Modify `Route.java` aggregate: drop `containerId` field, add `authorizedOperatorId: Long` (no JPA FK needed), add `@OneToMany containerAssignments` | Test: `RouteEntityTests.shouldHaveAuthorizedOperatorIdAndContainerAssignments` | Files: `domain/aggregates/Route.java` | Depends: T4.1, T4.2
- [x] **T4.4** — BC: logistics | Create `RouteResource` (replacement) with Vue shape: `codigo`, `operador`, `estado`, `progreso`, `cajasAsignadas`, `alertCount`, `assignedBoxes` | Test: `RouteResourceTests` | Files: `presentation/resources/RouteResource.java`
- [x] **T4.5** — BC: logistics | Update `RouteAssembler` to: inject `IamContextFacade` + `ContainerQueryService` + `AlertQueryService`; compute `operador` (from authorizedOperator.name), `progreso` (checkpoints), `cajasAsignadas`, `alertCount`, `assignedBoxes` | Test: `RouteAssemblerTests.should_compute_operador`, `should_compute_progreso`, `should_compute_assigned_boxes` | Files: `presentation/assemblers/RouteAssembler.java` | Depends: T4.3
- [x] **T4.6** — BC: logistics | Add `countByAuthorizedOperatorIdAndStatusIn` and `countByAuthorizedOperatorIdAndStatus` to `RouteRepository` | Test: `RouteRepositoryTests` | Files: `infrastructure/persistence/RouteRepository.java`
- [x] **T4.7** — BC: logistics | Create `ViajesController` with `GET /api/v1/viajes?estado=&limit=` (active-filtered trip query with operator join) | Test: `ViajesControllerTests.should_return_active_trips_with_limit` | Files: `presentation/ViajesController.java` | Depends: T4.4, T4.5
- [x] **T4.8** — BC: logistics | Update `RoutesController` POST to accept `containerIds: Long[]` body, create `RouteContainerAssignment` entries | Test: `RoutesControllerTests.should_create_route_with_container_assignments` | Files: `presentation/controllers/RoutesController.java` | Depends: T4.2, T4.3
- [x] **T4.9** — BC: logistics | Create `LogisticsContextFacade` implementing `LogisticsQueryService` (for iam BC cross-BC call) | Test: `LogisticsContextFacadeTests.shouldReturnActiveAndCompletedCounts` | Files: `application/LogisticsContextFacade.java` | Depends: T4.1, T4.6
- [x] **T4.10** — BC: logistics | Create `RouteContextFacade` in logistics implementing monitoring's `RouteQueryService` (via `RouteContainerAssignment` lookup) | Test: `LogisticsRouteContextFacadeTests` | Files: `application/RouteContextFacade.java` | Depends: T4.2, T4.3, T3.3

---

## Phase 5: Cross-BC Integration, Tests, and Smoke Testing

- [x] **T5.1** — Cross-BC | Run `mvn test` — all existing tests must pass; update any broken tests due to payload changes (Alert path rename, UserResource new fields, ContainerResource new fields, RouteResource new fields) in the same commit | Test: full suite pass | Files: all affected test files
- [x] **T5.2** — Cross-BC | Write `@SpringBootTest` integration test for full login → JWT → GET /users flow with computed fields | Test: `LoginFlowIntegrationTests` | Files: `src/test/java/.../LoginFlowIntegrationTests.java`
- [x] **T5.3** — Cross-BC | Write integration test for `GET /api/v1/dashboard/stats` aggregating all 4 BCs | Test: `DashboardStatsIntegrationTests` | Files: `src/test/java/.../DashboardStatsIntegrationTests.java`
- [x] **T5.4** — Cross-BC | Smoke test: `curl` or `@SpringBootTest` hit all new endpoints, verify response shapes match Vue frontend contract (US01–US20 coverage) | Test: `SmokeTest` | Depends: T1.14, T2.11, T3.9, T3.12, T3.13, T4.7

---

## Phase 6: Docker, CORS, and Deployment Configuration

- [x] **T6.1** — Infra | Create `Dockerfile` (multi-stage: maven build, eclipse-temurin:21-jre runtime) | Files: `Dockerfile`
- [x] **T6.2** — Infra | Create `docker-compose.yml` (backend service on port 8080, H2 internal volume) | Files: `docker-compose.yml`
- [x] **T6.3** — Infra | Configure Spring Security CORS: allow `http://localhost:5173` (Vue dev) and any `localhost` variant | Test: `CorsConfigTests` | Files: `infrastructure/config/SecurityConfig.java` or wherever CORS is configured
- [x] **T6.4** — Infra | Add `.dockerignore` (exclude target/, .m2/, node_modules/ from build context) | Files: `.dockerignore`
- [x] **T6.5** — Infra | Document backend env vars in `.env.example` ( SPRING_PROFILES_ACTIVE, SERVER_PORT, etc.) | Files: `.env.example`

---

## Phase 7: Frontend Connection and Run Documentation

- [x] **T7.1** — Frontend | Update Vue frontend `src/lib/api.ts` (or env config) to point to `http://localhost:8080/api/v1` | Files: `cryoguard-webapp/src/lib/api.ts` or `.env`
- [x] **T7.2** — Frontend | Verify CORS works from Vue dev server (`http://localhost:5173`) → backend `http://localhost:8080` | Test: browser network tab or `@SpringBootTest` with Vue origin | Depends: T6.3
- [x] **T7.3** — Frontend | End-to-end smoke: sign in via Vue → JWT in localStorage → navigate to dashboard → real data loads from `/api/v1/dashboard/stats` | Test: manual browser test or Playwright e2e | Depends: T5.4, T7.1
- [x] **T7.4** — Docs | Document run instructions in `RUNBOOK.md` or `README.md`: `mvn spring-boot:run`, `docker-compose up`, Vue connection, test credentials | Files: `RUNBOOK.md` or `README.md`

---

## Dependency Order Summary

```
T4.1 (RouteStatus INITIATED/IN_PROGRESS)
  └─ T4.2 (RouteContainerAssignment)
      └─ T4.3 (Route authorizedOperatorId + containerAssignments)
          ├─ T1.2 (LogisticsQueryService interface in iam)
          ├─ T2.7 (RouteQueryService interface in evaluation)
          ├─ T3.3 (RouteQueryService in monitoring)
          ├─ T4.4 (RouteResource Vue shape)
          ├─ T4.5 (RouteAssembler update)
          ├─ T4.6 (RouteRepository counts)
          ├─ T4.8 (RoutesController POST with containerIds)
          ├─ T4.9 (LogisticsContextFacade for iam)
          └─ T4.10 (RouteContextFacade for monitoring)
              └─ T1.8 (LogisticsContextFacade implementation in logistics)
                  └─ T1.12 (UserResourceFromEntityAssembler uses LogisticsQueryService)
                      └─ T1.14 (AuthController login with computed fields)
                          └─ T5.2 (Login flow integration test)
```

## Acceptance Criteria Coverage

| US | Task | Validation |
|----|------|------------|
| US01 | T1.14 | POST /api/v1/auth/login returns JWT + user with computed fields |
| US02 | T1.3, T1.13 | POST /api/v1/auth/sign-up accepts telefono |
| US03 | T3.12 | GET /api/v1/dashboard/stats returns 4 KPI blocks |
| US04 | T3.13 | GET /api/v1/dashboard/ia/precision returns integer |
| US05 | T3.13 | GET /api/v1/dashboard/ia/recomendaciones returns array |
| US06 | T2.11 | GET /api/v1/alertas?limit=3&sort=reciente returns 3 alerts |
| US07 | T4.7 | GET /api/v1/viajes?estado=activo&limit=3 returns active trips |
| US08 | T1.12 | GET /api/v1/users?role=OPERATOR returns computed fields |
| US09 | T1.9, T1.13 | POST /api/v1/users creates operator with telefono |
| US10 | T1.11, T1.13 | PUT /api/v1/users/{id} updates telefono |
| US11 | T1.11 | PUT /api/v1/users/{id} status change preserves computed fields |
| US12 | T1.7 | POST /api/v1/users/{id}/reset-password returns 4-digit PIN |
| US13 | T3.5 | GET /api/v1/containers returns coolingActive, firmware, locked |
| US14 | T3.5 | POST /api/v1/containers creates container |
| US15 | T3.5 | PUT /api/v1/containers/{id} updates container |
| US16 | T4.5 | GET /api/v1/routes returns operatorName, alertCount, assignedBoxes |
| US17 | T4.8 | POST /api/v1/routes creates route with container assignments |
| US18 | T4.8 | POST /api/v1/routes/{id}/complete marks route complete |
| US19 | T2.11 | GET /api/v1/alertas returns paginated alerts with boxId, tripCode, value, status |
| US20 | T3.9 | GET /api/v1/devices returns real-time device list |