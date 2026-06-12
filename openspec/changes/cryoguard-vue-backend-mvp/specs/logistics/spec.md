# Delta for Logistics — cryoguard-vue-backend-mvp

## MODIFIED Requirements

### Requirement: route-management (Previously: route-management)

The system SHALL expose route management endpoints at `GET /api/v1/routes`, `GET /api/v1/routes/{id}`, `POST /api/v1/routes`, `PUT /api/v1/routes/{id}`, `POST /api/v1/routes/{id}/complete`, and `DELETE /api/v1/routes/{id}`. The Route entity MUST be extended with an optional `authorizedOperator` field (ManyToOne to User from iam BC; nullable; defaults null for existing routes). The `RouteStatus` enum MUST be extended with two new values: `INITIATED` (value: "iniciado") and `IN_PROGRESS` (value: "en_ruta"), alongside existing `active`, `completed`, `cancelled`. The existing `active` status is preserved for backward compatibility; existing routes with `active` status are unaffected.

**Field mapping for `RouteResource` response (MODIFIED)**:

| Backend field | Frontend field | Notes |
|--------------|----------------|-------|
| `routeId` | `codigo` | Renamed — e.g. "V-2024-0156" |
| `status` (value string) | `estado` | Remapped: `INITIATED`→"iniciado", `IN_PROGRESS`→"en_ruta", `COMPLETED`→"completado", `CANCELLED`→"cancelado", `active`→"active" (legacy) |
| `authorizedOperator.name` | `operador` | NEW — nullable; null when no operator assigned |
| `progreso` (computed) | `progreso` | NEW — Integer 0–100; derived from completed checkpoints / total checkpoints; `0` when checkpoints is null or 0 |
| `cajasAsignadas` (computed) | `cajasAsignadas` | NEW — count of containers assigned via RouteContainerAssignment |
| `alertCount` (computed) | `alertCount` | NEW — count of active (unresolved) alerts for all containers on this route |
| `assignedBoxes` (computed) | `assignedBoxes` | NEW — List<String> of Container.containerId codes assigned to this route |
| `containerId` | (hidden) | Internal only; not surfaced in frontend response |
| `name` | (hidden) | Internal only |
| `currentLocation` | (hidden) | Internal only |

**BEFORE** (old `RouteResource`):
```json
{ "id": 1, "routeId": "ROUTE-001", "name": "Quito-Hospital", "containerId": 5,
  "status": "active", "origin": "...", "destination": "..." }
```

**AFTER** (new `RouteResource`):
```json
{ "id": 1, "codigo": "V-2024-0156", "estado": "en_ruta",
  "operador": "Juan Pérez", "progreso": 65, "cajasAsignadas": 8,
  "alertCount": 1, "assignedBoxes": ["CG-001", "CG-002"] }
```

#### Scenario: US16 — GET /api/v1/routes returns array with new fields

- GIVEN 2 routes exist: R1 (authorizedOperator=User[name=Juan], 3 checkpoints with 2 completed, 8 containers, 1 active alert) and R2 (authorizedOperator=null, no checkpoints, no containers)
- WHEN client requests `GET /api/v1/routes`
- THEN response SHALL be HTTP 200 with array (NOT paginated) of route objects
- AND each route SHALL include `codigo`, `estado`, `operador`, `progreso`, `cajasAsignadas`, `alertCount`, `assignedBoxes`
- AND R1 SHALL have `codigo="R1.routeId"`, `operador="Juan"`, `progreso` ≈ 67, `cajasAsignadas=8`, `alertCount=1`, `assignedBoxes=[codes...]`
- AND R2 SHALL have `operador=null`, `progreso=0`, `cajasAsignadas=0`, `assignedBoxes=[]`
- **Test**: `should_return_routes_with_all_new_fields`

#### Scenario: US16 — Route with no operator returns operador=null

- GIVEN a route exists with `authorizedOperator=null`
- WHEN client requests `GET /api/v1/routes`
- THEN that route's `operador` SHALL be `null` (no fallback name)
- **Test**: `should_return_null_operador_when_route_has_no_authorized_operator`

#### Scenario: US16 — Route with no waypoints returns progreso=0

- GIVEN a route exists with `checkpoints=null` or `checkpoints=0`
- WHEN client requests `GET /api/v1/routes`
- THEN that route's `progreso` SHALL be `0`
- **Test**: `should_return_progreso_zero_when_no_checkpoints`

#### Scenario: US16 — Route with no containers returns empty assignedBoxes

- GIVEN a route exists with no containers assigned
- WHEN client requests `GET /api/v1/routes`
- THEN that route's `cajasAsignadas` SHALL be `0` and `assignedBoxes` SHALL be `[]`
- **Test**: `should_return_empty_assigned_boxes_when_no_containers`

#### Scenario: US16 — Route with no active alerts returns alertCount=0

- GIVEN a route exists with containers that have no active alerts
- WHEN client requests `GET /api/v1/routes`
- THEN that route's `alertCount` SHALL be `0`
- **Test**: `should_return_zero_alert_count_when_no_active_alerts`

#### Scenario: US16 — GET /routes?status=IN_PROGRESS filters correctly

- GIVEN 3 routes: R1 (IN_PROGRESS), R2 (INITIATED), R3 (COMPLETED)
- WHEN client requests `GET /api/v1/routes?status=IN_PROGRESS`
- THEN response SHALL contain only R1
- AND R1's `estado` SHALL be "en_ruta"
- **Test**: `should_filter_routes_by_in_progress_status`

#### Scenario: US16 — GET /routes?containerId filters correctly

- GIVEN 2 routes: R1 (containerId=5), R2 (containerId=10)
- WHEN client requests `GET /api/v1/routes?containerId=5`
- THEN response SHALL contain only R1
- **Test**: `should_filter_routes_by_container_id`

#### Scenario: US17 — POST /api/v1/routes accepts optional authorizedOperatorId and containerIds

- GIVEN a user with id=3 exists (operator), and no containers are assigned to any route yet
- WHEN client submits `POST /api/v1/routes` with `{ "name": "Viaje Quito", "containerId": 5, "origin": "Quito", "destination": "Hospital", "authorizedOperatorId": 3, "containerIds": [1, 2, 3] }`
- THEN response SHALL be HTTP 201 with route where `operador` is joined from authorizedOperator.name="..."
- AND `cajasAsignadas` SHALL be `3`
- AND `assignedBoxes` SHALL contain codes for containers 1, 2, 3
- **Test**: `should_create_route_with_authorized_operator_and_container_assignments`

#### Scenario: US17 — POST /api/v1/routes without authorizedOperatorId creates route with operador=null

- GIVEN no user needed
- WHEN client submits `POST /api/v1/routes` with `{ "name": "Viaje Sin Op", "containerId": 5, "origin": "Quito", "destination": "Hospital" }` (no authorizedOperatorId)
- THEN response SHALL be HTTP 201 with route where `operador` is `null`
- **Test**: `should_create_route_with_null_operador_when_no_authorized_operator_id`

#### Scenario: US17 — POST /api/v1/routes with non-existent authorizedOperatorId returns 404

- GIVEN no user exists with id=9999
- WHEN client submits `POST /api/v1/routes` with `{ "name": "Viaje", "containerId": 5, "authorizedOperatorId": 9999 }`
- THEN response SHALL return HTTP 404 Not Found
- **Test**: `should_return_404_when_authorized_operator_id_does_not_exist`

#### Scenario: US18 — POST /api/v1/routes/{id}/complete marks route as COMPLETED

- GIVEN a route with id=5 exists with status IN_PROGRESS
- WHEN client submits `POST /api/v1/routes/5/complete` with `{}`
- THEN response SHALL be HTTP 200 with route where `estado` is "completado"
- AND `endTime` SHALL be set to current timestamp
- **Test**: `should_mark_route_as_completed_on_complete`

#### Scenario: US18 — POST /api/v1/routes/{id}/complete on non-existent route returns 404

- GIVEN no route exists with id=9999
- WHEN client submits `POST /api/v1/routes/9999/complete`
- THEN response SHALL return HTTP 404 Not Found
- **Test**: `should_return_404_when_completing_nonexistent_route`

#### Scenario: US18 — POST /api/v1/routes/{id}/complete already-completed route throws 409

- GIVEN a route with id=5 exists with status COMPLETED
- WHEN client submits `POST /api/v1/routes/5/complete`
- THEN response SHALL return HTTP 409 Conflict
- **Test**: `should_return_409_when_completing_already_completed_route`

#### Scenario: US16 — RouteStatus enum maps to correct Spanish values

- GIVEN routes with statuses INITIATED, IN_PROGRESS, COMPLETED, CANCELLED
- WHEN client requests `GET /api/v1/routes`
- THEN status values in responses SHALL be: INITIATED→"iniciado", IN_PROGRESS→"en_ruta", COMPLETED→"completado", CANCELLED→"cancelado"
- **Test**: `should_map_route_status_to_spanish_values`

---

## ADDED Requirements

### Requirement: trip-tracking (NEW)

The system SHALL expose `GET /api/v1/viajes` as a dashboard-optimized query that returns active trips filtered by status and optionally limited. This endpoint is distinct from `GET /api/v1/routes` in that it filters for active states (INITIATED, IN_PROGRESS) and returns a compact trip card payload optimized for the dashboard.

**ViajesResource response shape**:

| Field | Type | Source |
|-------|------|--------|
| `codigo` | String | Route.routeId |
| `operador` | String\|null | Route.authorizedOperator.name; null when no operator |
| `estado` | String | Route.status value mapped: INITIATED→"iniciado", IN_PROGRESS→"en_ruta" |
| `progreso` | Integer 0–100 | Derived: completed checkpoints / total checkpoints; 0 when no checkpoints |
| `cajasAsignadas` | Integer | Count of containers assigned via RouteContainerAssignment |

**Query parameters**:
- `estado` (optional): filters routes by status. Value `"activo"` maps to `status IN (INITIATED, IN_PROGRESS)`. Other values are passed directly to repository.
- `limit` (optional): caps the result array. When provided and positive, return at most `limit` items. When absent or null, return all matching routes.

#### Scenario: US07 — GET /api/v1/viajes?estado=activo&limit=3 returns active trips for dashboard

- GIVEN 5 active routes exist: R1 (operator=Juan, 8 containers, 65% progress), R2 (operator=María, 12 containers, 40% progress), R3 (operator=Carlos, 6 containers, 10% progress), R4 (operator=null, 2 containers, 30% progress), R5 (completed, not active)
- WHEN client requests `GET /api/v1/viajes?estado=activo&limit=3`
- THEN response SHALL be HTTP 200 with array of at most 3 trip objects
- AND each trip SHALL include `codigo`, `operador`, `estado`, `progreso`, `cajasAsignadas`
- AND R1 SHALL have `operador="Juan"`, `estado="en_ruta"`, `progreso=65`, `cajasAsignadas=8`
- AND R4 SHALL have `operador=null`
- AND R5 SHALL NOT appear in results
- **Test**: `should_return_active_trips_with_limit`

#### Scenario: US07 — GET /api/v1/viajes?estado=activo returns all active trips when no limit

- GIVEN 2 active routes exist
- WHEN client requests `GET /api/v1/viajes?estado=activo` (no limit param)
- THEN response SHALL be HTTP 200 with array containing both routes
- **Test**: `should_return_all_active_trips_without_limit`

#### Scenario: US07 — GET /api/v1/viajes when no active routes returns empty array

- GIVEN no routes with status INITIATED or IN_PROGRESS exist
- WHEN client requests `GET /api/v1/viajes?estado=activo`
- THEN response SHALL be HTTP 200 with body `[]`
- **Test**: `should_return_empty_array_when_no_active_routes`

#### Scenario: US07 — GET /api/v1/viajes with operador name join

- GIVEN an active route exists with authorizedOperator.name="Pedro Martínez"
- WHEN client requests `GET /api/v1/viajes?estado=activo`
- THEN that trip's `operador` SHALL be "Pedro Martínez"
- **Test**: `should_join_operator_name_in_viajes_response`

---

### Requirement: cross-bc-route-code-lookup (NEW — CRITICAL for evaluation BC)

The logistics BC SHALL expose `RouteQueryService.getCode(routeId: Long): String` as a cross-BC query method that resolves a Route database ID (Long) to its `routeId` field (String, e.g. "V-2024-0156"). This method is REQUIRED by the evaluation BC to populate the `tripCode` field in alert resources. If the route is not found, this method SHALL return `null`.

**Interface**: `RouteQueryService.getCode(routeId: Long): String`

#### Scenario: getCode returns routeId for valid route id

- GIVEN a route exists with id=5 and routeId="V-2024-0156"
- WHEN `RouteQueryService.getCode(5)` is invoked
- THEN result SHALL be `"V-2024-0156"`
- **Test**: `should_return_route_code_for_valid_id`

#### Scenario: getCode returns null for non-existent route id

- GIVEN no route exists with id=9999
- WHEN `RouteQueryService.getCode(9999)` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_code_for_nonexistent_route_id`

---

### Requirement: cross-bc-route-stats-by-operator (NEW — CRITICAL for iam BC)

The logistics BC SHALL expose `RouteQueryService.getStatsByOperator(operatorId: Long): RouteStatsDto` as a cross-BC query method that returns trip statistics for a given operator. The `RouteStatsDto` SHALL contain `activeCount: Int` (count of routes where `authorizedOperator.id = operatorId` AND `status IN (INITIATED, IN_PROGRESS)`) and `completedCount: Int` (count of routes where `authorizedOperator.id = operatorId` AND `status = COMPLETED`). This method is REQUIRED by the iam BC to populate `viajesAsignados` and `viajesCompletados` computed fields in UserResource responses. If the operator has no routes, this method SHALL return `RouteStatsDto(0, 0)`.

**Interface**: `RouteQueryService.getStatsByOperator(operatorId: Long): RouteStatsDto`
**Where**: `RouteStatsDto` is a record with `activeCount: Int` and `completedCount: Int`

#### Scenario: getStatsByOperator returns correct counts

- GIVEN operator id=3 has 2 routes with status IN_PROGRESS, 1 route with status INITIATED, and 5 routes with status COMPLETED
- WHEN `RouteQueryService.getStatsByOperator(3)` is invoked
- THEN result SHALL be `RouteStatsDto(activeCount=3, completedCount=5)`
- **Test**: `should_return_correct_active_and_completed_counts_for_operator`

#### Scenario: getStatsByOperator returns zeros when operator has no routes

- GIVEN operator id=99 exists but has no assigned routes
- WHEN `RouteQueryService.getStatsByOperator(99)` is invoked
- THEN result SHALL be `RouteStatsDto(activeCount=0, completedCount=0)`
- **Test**: `should_return_zero_counts_when_operator_has_no_routes`

#### Scenario: getStatsByOperator excludes cancelled routes from active count

- GIVEN operator id=3 has 1 route IN_PROGRESS, 1 route CANCELLED, 1 route COMPLETED
- WHEN `RouteQueryService.getStatsByOperator(3)` is invoked
- THEN result SHALL be `RouteStatsDto(activeCount=1, completedCount=1)` (cancelled excluded from both)
- **Test**: `should_exclude_cancelled_routes_from_counts`

---

### Requirement: cross-bc-route-info-by-container-code (NEW — CRITICAL for monitoring BC)

The logistics BC SHALL expose `RouteQueryService.getInfoByContainerCode(containerCode: String): RouteInfoDto | null` as a cross-BC query method that resolves a container code (String, e.g. "CG-001") to its active route information. This method is REQUIRED by the monitoring BC to populate the `tripCode`, `latitude`, and `longitude` fields in the GET /devices response (DeviceResource). The `RouteInfoDto` SHALL contain `tripCode: String`, `latitude: Double`, `longitude: Double`.

**Logic**: Join RouteContainerAssignment (or Route.containerId) to Container.id where Container.containerId = containerCode, find the route with `status IN (INITIATED, IN_PROGRESS)` (active route), return its routeId as tripCode and currentLocation lat/lng. If no active route exists for the container, return `null`. If the route has no currentLocation, latitude/longitude SHALL be `null`.

**Interface**: `RouteQueryService.getInfoByContainerCode(containerCode: String): RouteInfoDto | null`
**Where**: `RouteInfoDto` is a record with `tripCode: String`, `latitude: Double | null`, `longitude: Double | null`

#### Scenario: getInfoByContainerCode returns route info for container in active route

- GIVEN container "CG-005" (id=5) is assigned to an active route with routeId="V-2024-0156" and currentLocation lat=-0.1807, lng=-78.4678
- WHEN `RouteQueryService.getInfoByContainerCode("CG-005")` is invoked
- THEN result SHALL be `RouteInfoDto(tripCode="V-2024-0156", latitude=-0.1807, longitude=-78.4678)`
- **Test**: `should_return_route_info_for_container_in_active_route`

#### Scenario: getInfoByContainerCode returns null when container not in any active route

- GIVEN container "CG-006" exists but is not assigned to any active route
- WHEN `RouteQueryService.getInfoByContainerCode("CG-006")` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_when_container_not_in_active_route`

#### Scenario: getInfoByContainerCode returns null when container code not found

- GIVEN no container exists with containerId="NONEXISTENT"
- WHEN `RouteQueryService.getInfoByContainerCode("NONEXISTENT")` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_when_container_code_not_found`

#### Scenario: getInfoByContainerCode returns null coords when route has no currentLocation

- GIVEN container "CG-007" is assigned to an active route with routeId="V-2024-0157" but currentLocation is null
- WHEN `RouteQueryService.getInfoByContainerCode("CG-007")` is invoked
- THEN result SHALL be `RouteInfoDto(tripCode="V-2024-0157", latitude=null, longitude=null)`
- **Test**: `should_return_null_coords_when_route_has_no_current_location`

---

## Cross-Cutting Notes

| Aspect | Detail |
|--------|--------|
| **RouteStatus enum changes** | Add `INITIATED("iniciado")` and `IN_PROGRESS("en_ruta")`; existing `active`, `completed`, `cancelled` preserved; JSON serialization uses `getValue()` |
| **authorizedOperator field** | `@ManyToOne(fetch = FetchType.LAZY)` to `com.example.cryoguard.iam.domain.entities.User`; nullable; no cascade |
| **progreso calculation** | `completedCheckpoints / totalCheckpoints * 100`, rounded to nearest integer; `0` when total is null or 0 |
| **cajasAsignadas calculation** | Count of distinct Container records assigned via RouteContainerAssignment (or Route.containerId if no assignment table) |
| **alertCount calculation** | Count of Alert entities where Alert.containerId IN (container IDs assigned to route) AND Alert.resolved = false |
| **Cross-BC: evaluation BC** | `getCode(routeId)` consumed by evaluation BC for Alert.tripCode |
| **Cross-BC: iam BC** | `getStatsByOperator(operatorId)` consumed by iam BC for UserResource.viajesAsignados/viajesCompletados |
| **Cross-BC: monitoring BC** | `getInfoByContainerCode(containerCode)` consumed by monitoring BC for DeviceResource.tripCode/lat/lng |
| **Container assignment** | New entity `RouteContainerAssignment` (routeId FK, containerId FK) or reuse existing Route.containerId as single-container; spec supports either model |
| **Existing routes** | Routes created before this change have `authorizedOperator=null`; `operador` in responses SHALL be `null` for these |
| **RouteId format** | Existing `routeId` field (e.g. "ROUTE-001") is renamed to `codigo` in responses; generation logic unchanged |
| **RouteResource field renames** | `routeId`→`codigo`; `status`→`estado` (value remapped) |

---

## Test Coverage Summary

| Scenario | Test Name |
|----------|-----------|
| GET /routes with all new fields | `should_return_routes_with_all_new_fields` |
| Route with no operator → operador=null | `should_return_null_operador_when_route_has_no_authorized_operator` |
| Route with no checkpoints → progreso=0 | `should_return_progreso_zero_when_no_checkpoints` |
| Route with no containers → empty assignedBoxes | `should_return_empty_assigned_boxes_when_no_containers` |
| Route with no alerts → alertCount=0 | `should_return_zero_alert_count_when_no_active_alerts` |
| GET /routes?status=IN_PROGRESS filter | `should_filter_routes_by_in_progress_status` |
| GET /routes?containerId filter | `should_filter_routes_by_container_id` |
| POST /routes with authorizedOperatorId + containerIds | `should_create_route_with_authorized_operator_and_container_assignments` |
| POST /routes without authorizedOperatorId → operador=null | `should_create_route_with_null_operador_when_no_authorized_operator_id` |
| POST /routes with non-existent authorizedOperatorId → 404 | `should_return_404_when_authorized_operator_id_does_not_exist` |
| POST /routes/{id}/complete marks COMPLETED | `should_mark_route_as_completed_on_complete` |
| POST /routes/{id}/complete non-existent → 404 | `should_return_404_when_completing_nonexistent_route` |
| POST /routes/{id}/complete already-completed → 409 | `should_return_409_when_completing_already_completed_route` |
| RouteStatus enum → Spanish values | `should_map_route_status_to_spanish_values` |
| GET /viajes?estado=activo&limit=3 | `should_return_active_trips_with_limit` |
| GET /viajes?estado=activo (no limit) | `should_return_all_active_trips_without_limit` |
| GET /viajes no active routes → empty array | `should_return_empty_array_when_no_active_routes` |
| GET /viajes joins operator name | `should_join_operator_name_in_viajes_response` |
| getCode valid id | `should_return_route_code_for_valid_id` |
| getCode non-existent → null | `should_return_null_code_for_nonexistent_route_id` |
| getStatsByOperator correct counts | `should_return_correct_active_and_completed_counts_for_operator` |
| getStatsByOperator zero counts | `should_return_zero_counts_when_operator_has_no_routes` |
| getStatsByOperator excludes cancelled | `should_exclude_cancelled_routes_from_counts` |
| getInfoByContainerCode active route | `should_return_route_info_for_container_in_active_route` |
| getInfoByContainerCode no active route → null | `should_return_null_when_container_not_in_active_route` |
| getInfoByContainerCode unknown code → null | `should_return_null_when_container_code_not_found` |
| getInfoByContainerCode no currentLocation → null coords | `should_return_null_coords_when_route_has_no_current_location` |