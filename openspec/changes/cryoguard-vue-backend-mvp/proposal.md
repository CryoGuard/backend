# Proposal: cryoguard-vue-backend-mvp — Connect Backend to Vue Frontend (MVP)

## Status
`draft`

## Intent

Make the CryoGuard backend functional and connected to the existing Vue 3 frontend by building all missing endpoints, fixing payload mismatches, and enabling real data flow for the three currently-mocked areas (Dashboard, Monitoreo tiempo real, Alertas) while keeping all CRUD wiring functional for operadores, cajas, and viajes.

This is an **MVP scope** — the goal is end-to-end connectivity, not architectural completeness. Backend shape SHALL adapt to the exact Vue frontend contract (backend→frontend coupling is acceptable for this iteration).

---

## Scope

### In Scope (User-Confirmed)

| # | Deliverable | Source |
|---|-------------|--------|
| 1 | Dashboard stats aggregation endpoint (`GET /api/v1/dashboard/stats`) | Explore: US-D01 mock |
| 2 | IA precision endpoint (`GET /api/v1/dashboard/ia/precision`) | Explore: US-D02 mock |
| 3 | IA recommendations endpoint (`GET /api/v1/dashboard/ia/recomendaciones`) | Explore: US-D03 mock |
| 4 | Recent alerts limited endpoint (`GET /api/v1/alertas?limit=3&sort=reciente`) | Explore: US-D04 mock |
| 5 | Active trips for dashboard (`GET /api/v1/viajes?estado=activo&limit=3`) | Explore: US-D05 mock |
| 6 | Real-time device monitoring endpoint (`GET /api/v1/devices`) | Explore: US-D06 mock |
| 7 | User PIN reset endpoint (`POST /api/v1/users/{userId}/reset-password`) | Explore: missing |
| 8 | Alert payload fix — add `boxId`, `tripCode`, `value`; fix severity enum | Explore: mismatch #1 |
| 9 | User payload fix — add `telefono`, `pinBloqueado`, `viajesAsignados`, `viajesCompletados` | Explore: mismatch #2 |
| 10 | Container payload fix — add `coolingActive`, `firmware`, `locked` | Explore: mismatch #3 |
| 11 | Route payload fix — add `operatorName`, `alertCount`, `assignedBoxes` list | Explore: mismatch #4 |
| 12 | telefono field in sign-up (POST /api/v1/auth/sign-up) | OperadoresPage.vue:134 |
| 13 | telefono field in user GET/PUT responses | OperadoresPage.vue:150 |

### Out of Scope (Explicitly Confirmed by User)

| Area | Reason |
|------|--------|
| **actuators BC** | No UI exists in frontend; will be built in future iteration when UI is designed |
| **operations BC** | No UI exists in frontend; future iteration |
| **notifications BC** | Dashboard IA recommendations are served from evaluation/monitoring data; no dedicated notification BC UI |
| **audit BC** | No UI exists in frontend; future iteration |
| **New endpoints not consumed by frontend** | User explicitly said: "los endpoints o servicios que no se usan en el frontend no hay problema" — keep dead code |
| **Sophisticated auth interceptor** | Client-side route guard + Spring Security rejection is sufficient (minimum auth) |
| **401 interception / token refresh layer** | Out of scope per user decision |
| **DTO de vista transformation layer** | Backend adapts to frontend shape directly; no intermediate layer |

### Non-Goals

- Production-grade security hardening
- Multi-tenant support
- Full CQRS event sourcing
- Performance optimization (academic project, non-production)

---

## Affected Bounded Contexts

| BC | Current State | Changes Needed |
|----|--------------|---------------|
| **iam** | Partial — User entity, basic CRUD, JWT | Add `telefono` field, `pinBloqueado` flag (from LOCKED status), `viajesAsignados`/`viajesCompletados` computed fields, `POST /users/{id}/reset-password` command, extend sign-up to accept `telefono` |
| **monitoring** | Partial — Container entity, telemetry query exists but unused | Add `coolingActive`, `firmware`, `locked` to ContainerResource; implement `GET /devices` (real-time telemetry aggregation from containers) |
| **evaluation** | Partial — Alert entity exists but payload shape wrong | Restructure AlertResource to include `boxId`, `tripCode`, `value`; map `severity` to `critica`/`advertencia`; add limited query `GET /alertas?limit=&sort=` |
| **logistics** | Partial — Route entity, basic CRUD | Add `operatorName` (join to User), `alertCount`, and `assignedBoxes` list (containers per route) to RouteResource; add active-filtered trip query |
| **actuators** | **MISSING** | **DO NOT BUILD** — no frontend UI, deferred to future iteration |
| **operations** | **MISSING** | **DO NOT BUILD** — no frontend UI, deferred to future iteration |
| **notifications** | **MISSING** | **DO NOT BUILD** — dashboard IA recommendations served from evaluation data |
| **audit** | **MISSING** | **DO NOT BUILD** — no frontend UI, deferred to future iteration |

---

## New Endpoints to Build

### 1. Dashboard Stats Aggregation
```
GET /api/v1/dashboard/stats
Response 200:
{
  "operadoresActivos": { "valor": 24, "subtexto": "+3 esta semana" },
  "cajasIoT": { "valor": 156, "subtexto": "142 conectadas" },
  "viajesActivos": { "valor": 18, "subtexto": "5 finalizados hoy" },
  "alertasActivas": { "valor": 7, "subtexto": "3 críticas" }
}
```
- **Target BC**: monitoring (queries count of active containers, operators, routes, alerts)
- **Implementation**: Aggregate counts from iam (operators), monitoring (containers), logistics (active routes), evaluation (active alerts)

### 2. IA Precision
```
GET /api/v1/dashboard/ia/precision
Response 200: 82   (integer percentage)
```
- **Target BC**: evaluation (derived from ratio of confirmed alerts to total alerts)
- **Note**: Academic project — precision is a static computed metric, not a real ML model

### 3. IA Recommendations
```
GET /api/v1/dashboard/ia/recomendaciones
Response 200:
[
  { "titulo": "Mantenimiento Preventivo: CG-047", "descripcion": "...", "prioridad": "alta", "confianza": 85 },
  { "titulo": "Optimización de Rutas: Horarios Diurnos", "descripcion": "...", "prioridad": "media", "confianza": 90 }
]
```
- **Target BC**: evaluation (rule-based recommendations derived from alert patterns and container telemetry anomalies)
- **Implementation**: Query recent unconfirmed alerts grouped by containerId → generate recommendation text server-side

### 4. Recent Alerts (Limited)
```
GET /api/v1/alertas?limit=3&sort=reciente
Response 200:
[
  { "id": "ALT-001", "tipo": "Temperatura Alta", "caja": "CG-047", "tiempo": "Hace 5 minutos", "severidad": "critica" },
  { "id": "ALT-002", "tipo": "Apertura No Autorizada", "caja": "CG-023", "tiempo": "Hace 12 minutos", "severidad": "advertencia" },
  { "id": "ALT-003", "tipo": "Batería Baja", "caja": "CG-089", "tiempo": "Hace 25 minutos", "severidad": "info" }
]
```
- **Target BC**: evaluation
- **Note**: Simplified alert card for dashboard — different from full alerts page payload

### 5. Active Trips for Dashboard
```
GET /api/v1/viajes?estado=activo&limit=3
Response 200:
[
  { "codigo": "V-2024-0156", "operador": "Juan Pérez", "estado": "en_ruta", "progreso": 65, "cajasAsignadas": 8 },
  { "codigo": "V-2024-0157", "operador": "María González", "estado": "en_ruta", "progreso": 40, "cajasAsignadas": 12 },
  { "codigo": "V-2024-0158", "operador": "Carlos Ruiz", "estado": "iniciado", "progreso": 10, "cajasAsignadas": 6 }
]
```
- **Target BC**: logistics
- **Join required**: Route.authorizedOperator → User.name

### 6. Real-Time Device Monitoring
```
GET /api/v1/devices
Response 200:
[
  {
    "id": "CG-001", "tripCode": "V-2024-0156",
    "temperature": "4.2°C", "humidity": "65%", "battery": "85%",
    "cooling": "On", "location": "Quito Centro",
    "online": true, "locked": true, "status": "normal",
    "latitude": -0.1807, "longitude": -78.4678,
    "activeAlerts": [], "lastSync": "Hace 12 seg",
    "temperatureWarning": false
  }
]
```
- **Target BC**: monitoring (aggregates latest telemetry per container + alert count per container)
- **Note**: `temperatureWarning` derived from latest telemetry reading vs threshold

### 7. User PIN Reset
```
POST /api/v1/users/{userId}/reset-password
Request: { }
Response 200: { "newPin": "1234" }   (4-digit numeric)
```
- **Target BC**: iam
- **Behavior**: Generate 4-digit numeric PIN, store as password (hashed), return plain for display in modal

---

## Modified Endpoints / Payload Fixes

### Mismatch 1: Alert Payload (evaluation BC)

**Current backend** `AlertResource` shape:
```json
{ "id": "...", "type": "...", "severity": "HIGH|MEDIUM|LOW", "message": "...", "timestamp": "..." }
```

**Required Vue frontend** `AlertIncident` shape:
```json
{ "id": "ALT-001", "severity": "critica"|"advertencia", "status": "activa"|"pendiente"|"confirmada",
  "message": "...", "boxId": "CG-047", "tripCode": "V-2024-0157", "value": "8.5°C", "timestamp": "2026-06-01 14:35:00" }
```

**Changes:**
- Add `boxId` field (from Alert.containerId → Container.code mapping)
- Add `tripCode` field (from Alert.tripId → Route.code mapping)
- Add `value` field (from Alert.triggerValue — the sensor reading that triggered the alert)
- Add `status` field: `activa` (new), `pendiente` (acknowledged but not resolved), `confirmada` (resolved)
- Map `severity`: `HIGH` → `critica`, `MEDIUM` → `advertencia`, `LOW` → removed (frontend only has 2 severity levels)
- Full alerts page uses paginated `GET /api/v1/alertas`; dashboard uses limited `GET /api/v1/alertas?limit=3`

**Impact on existing tests**: Alert-related tests that assert on old `AlertResource` shape MUST be updated to reflect new fields.

### Mismatch 2: User Payload (iam BC)

**Current backend** `UserResource` shape:
```json
{ "id": 1, "name": "...", "email": "...", "role": "...", "status": "ACTIVE|INACTIVE|LOCKED", "lastLogin": "...", "createdAt": "..." }
```

**Required Vue frontend** `Operador` shape:
```json
{ "apiId": 1, "id": "OP-001", "nombre": "...", "email": "...",
  "telefono": "999888777", "estado": "activo"|"inactivo",
  "pinBloqueado": false, "viajesAsignados": 3, "viajesCompletados": 12,
  "ultimaActividad": "11/06/2026 14:35" }
```

**Changes:**
- Add `telefono` field to User entity and all UserResource responses
- Add `pinBloqueado` computed from `status === "LOCKED"`
- Add `viajesAsignados` computed from count of Route where authorizedOperator = this user AND status IN (INITIATED, IN_PROGRESS)
- Add `viajesCompletados` computed from count of Route where authorizedOperator = this user AND status = COMPLETED
- Extend `POST /api/v1/auth/sign-up` to accept optional `telefono` field
- Extend `PUT /api/v1/users/{id}` to accept optional `telefono` field

**Impact on existing tests**: UserResource assertion tests will need to include new fields; sign-up tests that validate request body will need to allow `telefono`.

### Mismatch 3: Container Payload (monitoring BC)

**Current backend** `ContainerResource` shape:
```json
{ "id": "...", "code": "...", "status": "...", "location": {...}, "temperature": ..., "humidity": ..., "batteryLevel": ..., "lastUpdate": "..." }
```

**Required Vue frontend** `CajaIot` shape:
```json
{ "id": "CG-001", "nombre": "...", "estado": "activo"|"inactivo"|"mantenimiento",
  "temperature": "4.2°C", "humidity": "65%", "batteryLevel": 85,
  "coolingActive": true, "firmware": "1.2.3", "locked": true,
  "connected": true, "location": "Quito Centro",
  "productType": "Vacunas", "lastUpdate": "..." }
```

**Changes:**
- Add `coolingActive` boolean (from latest telemetry.coolingActive or derived from peltier state)
- Add `firmware` string (Container.firmwareVersion field)
- Add `locked` boolean (Container.locked / servo lock state)
- Add `connected` boolean (online derived from last telemetry timestamp < 5 min)
- Rename/map fields to match frontend naming: `status` → `estado`, `code` → `id`

**Impact on existing tests**: ContainerResource tests that assert specific field lists will need updating.

### Mismatch 4: Route Payload (logistics BC)

**Current backend** `RouteResource` shape:
```json
{ "id": "...", "code": "...", "containerId": "...", "origin": "...", "destination": "...", "status": "...", "startTime": "...", "estimatedArrival": "..." }
```

**Required Vue frontend** `Trip` shape (for ViajesPage and dashboard active trips):
```json
{ "codigo": "V-2024-0156", "operador": "Juan Pérez", "estado": "en_ruta"|"iniciado"|"completado",
  "progreso": 65, "cajasAsignadas": 8,
  "alertCount": 1, "assignedBoxes": ["CG-001", "CG-002", ...] }
```

**Changes:**
- Add `operador` field (Route.authorizedOperator.name — join required)
- Add `progreso` integer 0-100 (derived from completed waypoints / total waypoints)
- Add `cajasAsignadas` count (count of containers assigned to this route)
- Add `alertCount` (count of active alerts for containers on this route)
- Add `assignedBoxes` list (Container.code[] for containers assigned to route)

**Impact on existing tests**: RouteResource tests will need updating to include new fields.

---

## New Entities / Aggregates / Commands / Queries per BC

### iam (Identity & Access Management)
- **Entity**: none new — extend existing User
- **Field additions**: `telefono` (String, optional), `firmwareVersion` (if tracking device info)
- **Command**: `ResetUserPin(userId)` → generates 4-digit PIN, stores hashed
- **Query**: `GetUserStats(userId)` → returns `{ viajesAsignados, viajesCompletados }` (computed from logistics Route counts)

### monitoring (Container & Telemetry)
- **Entity**: none new — extend existing Container
- **Field additions**: `coolingActive` (boolean), `firmware` (String), `locked` (boolean)
- **Query**: `GetAllDevices()` → returns real-time status aggregation per container (latest telemetry + alert count + computed online/cooling/locked fields)
- **Query**: `GetDevice(deviceId)` → single device with full telemetry history

### evaluation (Alerts & Rules)
- **Entity**: none new — extend existing Alert
- **Field additions**: `boxId` (String, Container.code), `tripCode` (String, Route.code), `value` (String, trigger value), `status` (ENUM: ACTIVA, PENDIENTE, CONFIRMADA — default ACTIVA on new alerts)
- **Query**: `GetAlerts(limit, sort)` → paginated alerts for full page
- **Query**: `GetRecentAlerts(limit)` → limited recent alerts for dashboard card
- **Query**: `GetAlertStats()` → `{ activas, kriticas, pendientes }` counts for alert page stats

### logistics (Routes & Trips)
- **Entity**: none new — extend existing Route
- **Field additions**: `operatorName` (transient, from join), `alertCount` (computed), `assignedBoxes` (transient list)
- **Query**: `GetActiveTrips(limit)` → filtered active routes with operator join and computed progress/alerts
- **Query**: `GetRoutes(filter)` → existing query extended with operator join

---

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| **Backend-to-frontend coupling** — backend shape is tightly bound to Vue contracts; any frontend change may require backend修改 | High | Document the exact payload shapes in specs; any frontend contract change must go through SDD change process |
| **Missing BCs are tempting scope creep** — developer may accidentally build for actuators/operations/notifications/audit | Medium | Explicit OUT OF SCOPE list in proposal; BCs are clearly marked; sdd-tasks MUST NOT include tasks for these BCs |
| **Alert payload restructure breaks existing tests** | High | Update AlertResource tests in same commit; run full test suite before PR |
| **Route operator join causes N+1 query** | Medium | Use JOIN FETCH or entity graph for Route + authorizedOperator; add integration test for route list with 100 routes |
| **telefono field added to sign-up but validation not defined** | Low | Add @Size(max=20) constraint; allow null for backward compat with existing clients |
| **Dashboard stats aggregation requires cross-BC queries** | Medium | Create a `DashboardStatsService` that coordinates queries across BCs; avoid circular dependencies |
| **Real-time device endpoint /devices may be slow** | Medium | Add pagination; cache telemetry results for 10s; add index on container_id + timestamp DESC |
| **IA precision and recommendations are synthetic** — academic project has no real ML | Low | Document clearly that precision is a ratio metric (confirmed/total alerts); recommendations are rule-based pattern matching |

---

## First-Slice Delivery Plan

The change is scoped as a single delivery (one PR). If the PR grows too large (>400 lines changed), split as follows:

**Slice 1 — IAM + Alert fixes (BCs: iam, evaluation)**
- Fix UserResource payload (telefono, pinBloqueado, viajes stats)
- Add POST /users/{id}/reset-password
- Fix AlertResource payload (boxId, tripCode, value, severity mapping, status)
- Update affected tests

**Slice 2 — Monitoring + Logistics fixes (BCs: monitoring, logistics)**
- Fix ContainerResource payload (coolingActive, firmware, locked, connected)
- Fix RouteResource payload (operatorName, progreso, cajasAsignadas, alertCount, assignedBoxes)
- Update affected tests

**Slice 3 — New dashboard/monitoring endpoints (BCs: monitoring, evaluation, logistics)**
- GET /dashboard/stats
- GET /dashboard/ia/precision
- GET /dashboard/ia/recomendaciones
- GET /alertas?limit=&sort=
- GET /viajes?estado=activo&limit=
- GET /devices

---

## Acceptance Criteria

All criteria are validated E2E against the Vue frontend (no separate postman/curl tests required).

### US Coverage (from explore report US01–US20)

| US | Description | Validation |
|----|-------------|------------|
| US01 | Login with email/password | POST /api/v1/auth/login returns JWT; frontend navigates to /dashboard |
| US02 | Registro (sign-up with telefono) | POST /api/v1/auth/sign-up accepts telefono; user created in iam |
| US03 | Dashboard stats display | GET /api/v1/dashboard/stats returns all 4 stat blocks; stats cards show real values |
| US04 | Dashboard IA precision | GET /api/v1/dashboard/ia/precision returns integer; precision % displayed |
| US05 | Dashboard IA recommendations | GET /api/v1/dashboard/ia/recomendaciones returns array; recommendations displayed |
| US06 | Dashboard recent alerts | GET /api/v1/alertas?limit=3&sort=reciente returns 3 alerts; alert cards show real data |
| US07 | Dashboard active trips | GET /api/v1/viajes?estado=activo&limit=3 returns active routes; trip cards show real operators |
| US08 | Operadores list | GET /api/v1/users?role=OPERATOR returns operators with telefono, pinBloqueado, viajes stats |
| US09 | Operador create | POST /api/v1/users creates operator; telefono stored and returned |
| US10 | Operador edit | PUT /api/v1/users/{id} updates name/email/telefono |
| US11 | Operador activate/deactivate | PUT /api/v1/users/{id} with status ACTIVE/INACTIVE works |
| US12 | Operador reset PIN | POST /api/v1/users/{id}/reset-password returns new 4-digit PIN; PIN displayed in modal |
| US13 | Cajas IoT list | GET /api/v1/containers returns containers with coolingActive, firmware, locked |
| US14 | Caja create | POST /api/v1/containers creates container |
| US15 | Caja edit | PUT /api/v1/containers/{id} updates container |
| US16 | Viajes list | GET /api/v1/routes returns routes with operatorName, alertCount, assignedBoxes |
| US17 | Viaje create | POST /api/v1/routes creates route |
| US18 | Viaje complete | POST /api/v1/routes/{id}/complete marks route complete |
| US19 | Alertas page | GET /api/v1/alertas returns paginated alerts with boxId, tripCode, value, status |
| US20 | Monitoreo tiempo real | GET /api/v1/devices returns real-time device list with temperature, humidity, battery, location, online status |

### Non-Functional Criteria

- [ ] `mvn test` passes with no test failures
- [ ] Application starts with `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- [ ] No new compiler warnings introduced
- [ ] H2 database schema auto-created on startup (JPA auditing enabled)

---

## Rollback Plan

For this academic project, rollback is straightforward:

1. **Code rollback**: `git revert <commit-hash>` of the PR — all changes are additive (new endpoints, field additions, payload restructures). No destructive migrations.
2. **No data migration needed**: H2 dev database is ephemeral; PostgreSQL NeonDB is not yet in production use.
3. **If breaking tests**: Identify failing test files, revert payload restructure commits first (most likely cause).
4. **If frontend breaks**: The Vue frontend is in a separate repo (`cryoguard-webapp`); backend rollback does not affect it unless payload shapes change. Frontend can continue using mock data if backend is reverted.

**Scope guard**: If a future change inadvertently includes work on actuators, operations, notifications, or audit BCs, those commits should be reverted separately. The change proposal MUST NOT include those BCs in tasks.md.

---

## Capabilities

### New Capabilities
- `dashboard-stats` — aggregated KPIs across BCs (operadores activos, cajas IoT, viajes activos, alertas activas)
- `dashboard-ia` — IA precision metric and rule-based recommendations from alert patterns
- `device-monitoring` — real-time device list with telemetry, location, battery, cooling status, online state
- `alert-management` — full alert list with box/trip context; limited recent alerts for dashboard
- `trip-tracking` — active trips with operator name, progress, assigned boxes, alert count
- `operator-stats` — computed viajesAsignados and viajesCompletados per operator

### Modified Capabilities
- `user-management` (iam): Extended with telefono field, computed pinBloqueado from LOCKED status, and computed trip stats
- `container-management` (monitoring): Extended with coolingActive, firmware, locked fields and computed connected state
- `route-management` (logistics): Extended with operatorName, progreso, cajasAsignadas, alertCount, assignedBoxes
- `alert-evaluation` (evaluation): Restructured alert payload with box/trip context, severity remapping, and status lifecycle