# Delta for Evaluation — cryoguard-vue-backend-mvp

## MODIFIED Requirements

### Requirement: alert-evaluation

The system SHALL provide alert evaluation and incident management for the CryoGuard IoT monitoring platform. The Alert entity MUST be extended with a `triggerValue` field (String, the raw sensor reading that caused the alert, e.g. "8.5°C", "18%", "> 5 min"). The system SHALL map internal severity and status values to the frontend's two-tier model.

**Alert Status Lifecycle** (NEW): The alert lifecycle state is represented by a `status` field with three values:
- `ACTIVA` (default on new alerts) — alert is active and unacknowledged; maps to frontend `"activa"`
- `PENDIENTE` — alert has been acknowledged but not resolved; maps to frontend `"pendiente"`
- `CONFIRMADA` — alert has been resolved; maps to frontend `"confirmada"`

The mapping from internal boolean fields to status SHALL be:
- `acknowledged=false AND resolved=false` → `ACTIVA` / `"activa"`
- `acknowledged=true AND resolved=false` → `PENDIENTE` / `"pendiente"`
- `resolved=true` → `CONFIRMADA` / `"confirmada"`

**Severity Remapping** (CHANGED): The internal `AlertSeverity` enum (`CRITICAL`, `WARNING`, `INFO`) SHALL be remapped to the frontend's two-tier model:
- `CRITICAL` → `"critica"`
- `WARNING` → `"advertencia"`
- `INFO` → `"advertencia"` (frontend only has two levels; INFO maps to the lower tier)

**ID Format** (CHANGED): The `id` field returned to the frontend SHALL be the `alertId` field (String, e.g. `"ALT-001"`) — NOT the internal Long database ID.

**Timestamp Format** (CHANGED): The `timestamp` field SHALL be formatted as `dd/MM/yyyy HH:mm` (es-PE locale, e.g. `"01/06/2026 14:35"`).

**Cross-BC Joins** (NEW): The `boxId` field SHALL be resolved by joining Alert.containerId (Long) to Container.code (String) via the monitoring BC. The `tripCode` field SHALL be resolved by joining Alert.tripId (Long, if present) to Route.code (String) via the logistics BC. If the joined BC is unavailable, the field SHALL return the raw Long as a String. If Alert.tripId is null (alert generated when container was not on a route), `tripCode` SHALL be `"Sin viaje"`.

**Note**: The current backend uses path `/api/v1/alerts`. The frontend calls `/api/v1/alertas`. The implementation MUST change the controller path to `/api/v1/alertas` to match the frontend contract.

#### Scenario: US19 — Paginated alerts return full AlertIncidentResource shape

- GIVEN 6 alerts exist in the database with varied severities and lifecycle states
- WHEN client requests `GET /api/v1/alertas?page=0&size=20`
- THEN response SHALL be HTTP 200 with a Page containing alert objects where each object has: `id` (String "ALT-XXX"), `severity` ("critica"|"advertencia"), `status` ("activa"|"pendiente"|"confirmada"), `message` (String), `boxId` (String, Container.code), `tripCode` (String, Route.code or "Sin viaje"), `value` (String, triggerValue), `timestamp` (formatted "dd/MM/yyyy HH:mm")
- AND `severity` SHALL be "critica" when internal severity is CRITICAL
- AND `severity` SHALL be "advertencia" when internal severity is WARNING or INFO
- AND `status` SHALL be "activa" when acknowledged=false and resolved=false
- AND `status` SHALL be "pendiente" when acknowledged=true and resolved=false
- AND `status` SHALL be "confirmada" when resolved=true
- **Test**: `should_return_paginated_alerts_with_correct_resource_shape`

#### Scenario: US19 — Alerts sorted by timestamp descending (most recent first)

- GIVEN alerts exist with timestamps spanning multiple days
- WHEN client requests `GET /api/v1/alertas?sort=reciente`
- THEN the first alert in the list SHALL have the most recent timestamp
- **Test**: `should_return_alerts_sorted_by_recent_timestamp`

#### Scenario: US19 — Alert with null tripId returns "Sin viaje" as tripCode

- GIVEN an alert exists with `containerId=5` and `tripId=null` (container not on active route)
- WHEN client requests `GET /api/v1/alertas`
- THEN the alert's `tripCode` SHALL be `"Sin viaje"`
- **Test**: `should_return_sin_viaje_when_alert_has_no_trip`

#### Scenario: US19 — Alert with missing container returns raw containerId as boxId

- GIVEN an alert exists with `containerId=99` but no Container with id=99 exists in monitoring BC
- WHEN client requests `GET /api/v1/alertas`
- THEN the alert's `boxId` SHALL be `"99"` (the raw Long as String, stub fallback)
- **Test**: `should_return_raw_container_id_as_string_when_container_not_found`

#### Scenario: US06 — Dashboard recent alerts limited to N results

- GIVEN 10 alerts exist in the database
- WHEN client requests `GET /api/v1/alertas?limit=3&sort=reciente`
- THEN response SHALL be HTTP 200 with an array of at most 3 alerts
- AND alerts SHALL be sorted by timestamp descending (most recent first)
- AND each alert SHALL include `id`, `tipo` (from alertType), `caja` (from boxId), `tiempo` (relative time string "Hace X minutos"), `severidad` ("critica"|"advertencia")
- **Test**: `should_return_limited_recent_alerts_for_dashboard`

#### Scenario: US19 — Empty alerts list returns empty page

- GIVEN no alerts exist in the database
- WHEN client requests `GET /api/v1/alertas`
- THEN response SHALL be HTTP 200 with an empty `content` array and `totalElements=0`
- **Test**: `should_return_empty_page_when_no_alerts_exist`

#### Scenario: US19 — Alerts filtered by status (activas)

- GIVEN 3 active alerts (acknowledged=false, resolved=false) and 2 confirmed alerts exist
- WHEN client requests `GET /api/v1/alertas?status=activa`
- THEN response SHALL contain only the 3 active alerts
- AND each returned alert SHALL have `status="activa"`
- **Test**: `should_filter_alerts_by_active_status`

#### Scenario: US19 — Alerts filtered by severity (critica)

- GIVEN 2 CRITICAL alerts and 3 WARNING alerts exist
- WHEN client requests `GET /api/v1/alertas?severity=critica`
- THEN response SHALL contain only the 2 CRITICAL alerts
- AND returned alerts SHALL have `severity="critica"`
- **Test**: `should_filter_alerts_by_critica_severity`

#### Scenario: US19 — Acknowledge alert changes status to PENDIENTE

- GIVEN an active alert with id `1` exists (acknowledged=false, resolved=false)
- WHEN client submits `PUT /api/v1/alertas/1/acknowledge?userId=5`
- THEN response SHALL be HTTP 200 with the updated alert
- AND `status` SHALL be `"pendiente"`
- AND `acknowledged` SHALL be `true`
- AND `resolved` SHALL remain `false`
- **Test**: `should_change_status_to_pendiente_on_acknowledge`

#### Scenario: US19 — Resolve alert changes status to CONFIRMADA

- GIVEN an alert with id `1` exists (acknowledged=true, resolved=false)
- WHEN client submits `PUT /api/v1/alertas/1/resolve?userId=5`
- THEN response SHALL be HTTP 200 with the updated alert
- AND `status` SHALL be `"confirmada"`
- AND `resolved` SHALL be `true`
- **Test**: `should_change_status_to_confirmada_on_resolve`

#### Scenario: US19 — Escalate CRITICAL alert returns 409 Conflict

- GIVEN an alert with severity CRITICAL exists
- WHEN client submits `PUT /api/v1/alertas/{id}/escalate`
- THEN response SHALL return HTTP 409 Conflict
- **Test**: `should_return_409_when_escalating_already_critical_alert`

---

## ADDED Requirements

### Requirement: dashboard-ia-precision

The system SHALL expose `GET /api/v1/dashboard/ia/precision` which returns an integer percentage representing the ratio of confirmed alerts to total alerts. This metric is synthetic (academic project — no real ML model). The formula SHALL be: `precision = (confirmedCount / totalCount) * 100`, rounded to the nearest integer. If `totalCount` is 0, the endpoint SHALL return `0`.

#### Scenario: US04 — IA precision returns integer percentage with alerts

- GIVEN 10 total alerts exist, of which 8 are confirmed (resolved=true)
- WHEN client requests `GET /api/v1/dashboard/ia/precision`
- THEN response SHALL be HTTP 200 with body `80` (integer)
- **Test**: `should_return_80_percent_precision_when_8_of_10_confirmed`

#### Scenario: US04 — IA precision returns 0 when no alerts exist

- GIVEN no alerts exist in the database
- WHEN client requests `GET /api/v1/dashboard/ia/precision`
- THEN response SHALL be HTTP 200 with body `0`
- **Test**: `should_return_0_when_no_alerts_exist`

#### Scenario: US04 — IA precision returns 100 when all alerts confirmed

- GIVEN 5 total alerts exist and all 5 are confirmed (resolved=true)
- WHEN client requests `GET /api/v1/dashboard/ia/precision`
- THEN response SHALL be HTTP 200 with body `100`
- **Test**: `should_return_100_when_all_alerts_confirmed`

---

### Requirement: dashboard-ia-recommendations

The system SHALL expose `GET /api/v1/dashboard/ia/recomendaciones` which returns an array of rule-based recommendation objects derived from alert patterns. This is a synthetic/rule-based system — NOT a machine learning model. The recommendations SHALL be derived from the following rules applied to alerts from the last 7 days:

**Rule 1 — High-Frequency Container Alert (Mantenimiento Preventivo)**:
If a specific container has 3 or more active alerts in the last 7 days → generate recommendation:
- `titulo`: `"Mantenimiento Preventivo: {containerCode}"` (e.g., "Mantenimiento Preventivo: CG-047")
- `descripcion`: `"Detectados {count} alertas en los últimos 7 días. Recomendamos revisar sistema de enfriamiento."`
- `prioridad`: `"alta"`
- `confianza`: `min(95, 50 + count * 15)` (more alerts = higher confidence, capped at 95)

**Rule 2 — Night Operations Alert (Optimización de Rutas)**:
If 3 or more alerts occurred between 22:00 and 05:59 (night hours) in the last 7 days → generate recommendation:
- `titulo`: `"Optimización de Rutas: Horarios Diurnos"`
- `descripcion`: `"{nightCount} alertas ocurrió durante horario nocturno (22:00-05:59). Considera ajustar horarios de operación."`
- `prioridad`: `"media"`
- `confianza`: `min(95, 50 + nightCount * 5)` (capped at 95)

**Rule 3 — Unconfirmed Alert Accumulation**:
If more than 5 alerts remain unconfirmed (status != CONFIRMADA) for more than 48 hours → generate recommendation:
- `titulo`: `"Revisión de Alertas Pendientes"`
- `descripcion`: `"{oldCount} alertas llevan más de 48 horas sin confirmación. Revisa y confirma las alertas resueltas."`
- `prioridad`: `"alta"`
- `confianza`: `85`

The endpoint SHALL return an empty array `[]` when no rules match. Maximum 5 recommendations per response.

#### Scenario: US05 — Recommendations returns maintenance recommendation for high-frequency container

- GIVEN container CG-047 has 4 active alerts in the last 7 days (no other patterns)
- WHEN client requests `GET /api/v1/dashboard/ia/recomendaciones`
- THEN response SHALL be HTTP 200 with an array containing 1 recommendation
- AND the recommendation SHALL have `titulo: "Mantenimiento Preventivo: CG-047"`, `prioridad: "alta"`, `confianza: 85` (50 + 4*15)
- **Test**: `should_return_maintenance_recommendation_for_container_with_4_alerts`

#### Scenario: US05 — Recommendations returns night operations recommendation

- GIVEN 8 alerts occurred during night hours (22:00-05:59) in the last 7 days, no other patterns
- WHEN client requests `GET /api/v1/dashboard/ia/recomendaciones`
- THEN response SHALL be HTTP 200 with an array containing 1 recommendation
- AND the recommendation SHALL have `titulo: "Optimización de Rutas: Horarios Diurnos"`, `prioridad: "media"`, `confianza: 90` (50 + 8*5, capped at 95)
- **Test**: `should_return_night_operations_recommendation_when_8_night_alerts`

#### Scenario: US05 — Recommendations returns empty array when no patterns match

- GIVEN no alerts exist in the last 7 days
- WHEN client requests `GET /api/v1/dashboard/ia/recomendaciones`
- THEN response SHALL be HTTP 200 with body `[]`
- **Test**: `should_return_empty_array_when_no_patterns_match`

#### Scenario: US05 — Recommendations returns multiple recommendations when multiple rules match

- GIVEN container CG-047 has 5 active alerts AND 10 night-hour alerts exist AND 6 unconfirmed alerts older than 48 hours
- WHEN client requests `GET /api/v1/dashboard/ia/recomendaciones`
- THEN response SHALL be HTTP 200 with an array containing 3 recommendations
- AND each recommendation SHALL match one of the rule patterns above
- **Test**: `should_return_all_matching_recommendations`

#### Scenario: US05 — Recommendations limits to maximum 5 items

- GIVEN 10 containers each have 3+ alerts (triggering Rule 1 multiple times)
- WHEN client requests `GET /api/v1/dashboard/ia/recomendaciones`
- THEN response SHALL contain at most 5 recommendations
- **Test**: `should_limit_recommendations_to_maximum_5`

---

### Requirement: alert-incident-resource

The system SHALL use `AlertIncidentResource` as the response DTO for all alert endpoints consumed by the Vue frontend. This resource SHALL replace the old `AlertResource` for all HTTP responses. The `AlertIncidentResource` DTO SHALL have the following fields:

| Field | Type | Source | Format/Value |
|-------|------|--------|-------------|
| `id` | String | Alert.alertId | e.g. "ALT-001" |
| `severity` | String | Alert.severity remapped | "critica" or "advertencia" |
| `status` | String | Computed from acknowledged+resolved | "activa", "pendiente", "confirmada" |
| `message` | String | Alert.message | e.g. "Temperatura superior a 8°C detectada" |
| `boxId` | String | Container.code via monitoring BC join | e.g. "CG-047"; stub to raw Long if unavailable |
| `tripCode` | String | Route.code via logistics BC join, or "Sin viaje" | e.g. "V-2024-0157"; "Sin viaje" if tripId is null |
| `value` | String | Alert.triggerValue | e.g. "8.5°C", "18%", "> 5 min" |
| `timestamp` | String | Alert.timestamp | dd/MM/yyyy HH:mm (es-PE, e.g. "01/06/2026 14:35") |

**BEFORE** (old `AlertResource`):
```json
{
  "id": 1,
  "alertId": "ALT-001",
  "containerId": 5,
  "alertType": "TEMPERATURE",
  "severity": "CRITICAL",
  "message": "Temperatura superior a 8°C",
  "timestamp": "2026-06-01T14:35:00",
  "acknowledged": false,
  "resolved": false
}
```

**AFTER** (new `AlertIncidentResource`):
```json
{
  "id": "ALT-001",
  "severity": "critica",
  "status": "activa",
  "message": "Temperatura superior a 8°C detectada",
  "boxId": "CG-047",
  "tripCode": "V-2024-0157",
  "value": "8.5°C",
  "timestamp": "01/06/2026 14:35"
}
```

---

## Cross-Cutting Notes

| Aspect | Detail |
|--------|--------|
| **Scope** | evaluation BC ONLY. Dashboard aggregation endpoints (/dashboard/*) are out of scope — evaluation provides the data, but the controller endpoint may live in a separate BC or be injected as a query into evaluation |
| **Academic ML** | IA precision and recommendations are synthetic rule-based metrics. No real ML model exists or is planned for this iteration. Document this clearly in code comments. |
| **Cross-BC: monitoring** | `boxId` lookup requires `ContainerQueryService.getCode(containerId: Long): String`. If unavailable, return raw Long as String. |
| **Cross-BC: logistics** | `tripCode` lookup requires `RouteQueryService.getCode(routeId: Long): String`. If unavailable, return raw Long as String. If tripId is null, return "Sin viaje". |
| **Stub behavior** | When monitoring or logistics BCs are not yet implemented, the join fields SHALL return the raw Long value cast to String as a stub. This allows the frontend to work with placeholder data until the full BC integration is complete. |
| **Alert.entity changes** | Add `triggerValue` (String, nullable) and `tripId` (Long, nullable) fields to the Alert entity. These fields MUST be populated when alerts are created by the monitoring/telemetry layer. |
| **Controller path change** | Change from `/api/v1/alerts` to `/api/v1/alertas` to match frontend contract. This is a breaking path change — existing tests and API documentation MUST be updated. |
| **Relative time for dashboard** | The `tiempo` field in dashboard-limited alerts SHALL be a human-readable relative string: "Hace X minutos", "Hace X horas", "Hace X días". Format using the Alert.timestamp field. |

---

## Test Coverage Summary

| Scenario | Test Name |
|----------|-----------|
| Paginated alerts with correct resource shape | `should_return_paginated_alerts_with_correct_resource_shape` |
| Alerts sorted by recent timestamp | `should_return_alerts_sorted_by_recent_timestamp` |
| Alert with null tripId returns "Sin viaje" | `should_return_sin_viaje_when_alert_has_no_trip` |
| Alert with missing container returns raw ID | `should_return_raw_container_id_as_string_when_container_not_found` |
| Dashboard limited recent alerts | `should_return_limited_recent_alerts_for_dashboard` |
| Empty alerts list | `should_return_empty_page_when_no_alerts_exist` |
| Filter alerts by active status | `should_filter_alerts_by_active_status` |
| Filter alerts by critica severity | `should_filter_alerts_by_critica_severity` |
| Acknowledge alert → pendente | `should_change_status_to_pendiente_on_acknowledge` |
| Resolve alert → confirmada | `should_change_status_to_confirmada_on_resolve` |
| Escalate already critical → 409 | `should_return_409_when_escalating_already_critical_alert` |
| IA precision with alerts | `should_return_80_percent_precision_when_8_of_10_confirmed` |
| IA precision with no alerts → 0 | `should_return_0_when_no_alerts_exist` |
| IA precision all confirmed → 100 | `should_return_100_when_all_alerts_confirmed` |
| Recommendations: maintenance rule | `should_return_maintenance_recommendation_for_container_with_4_alerts` |
| Recommendations: night ops rule | `should_return_night_operations_recommendation_when_8_night_alerts` |
| Recommendations: no patterns → empty | `should_return_empty_array_when_no_patterns_match` |
| Recommendations: multiple rules | `should_return_all_matching_recommendations` |
| Recommendations: max 5 limit | `should_limit_recommendations_to_maximum_5` |