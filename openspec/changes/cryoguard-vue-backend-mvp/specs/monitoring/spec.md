# Delta for Monitoring — cryoguard-vue-backend-mvp

## MODIFIED Requirements

### Requirement: container-management

The system SHALL expose container CRUD endpoints at `GET /api/v1/containers`, `GET /api/v1/containers/{id}`, `POST /api/v1/containers`, and `PUT /api/v1/containers/{id}`. The Container entity MUST be extended with two new fields: `firmwareVersion` (String, nullable, stored as `"unknown"` when null) and `locked` (Boolean, nullable, defaults to `false`). The `connected` boolean field SHALL be derived at query time: `true` when the container's most recent `TelemetryReading.timestamp` is within 5 minutes of the current time, otherwise `false`. When no telemetry readings exist for a container, `connected` SHALL be `false`.

**Field mapping for `ContainerResource` response (MODIFIED)**:

| Backend field | Frontend field | Notes |
|---------------|----------------|-------|
| `containerId` | `id` | Renamed — Container.code |
| `name` | `nombre` | Renamed |
| `status` (lowercase) | `estado` | Value mapping: `active`→`activo`, `inactive`→`inactivo`, `maintenance`→`mantenimiento` |
| `coolingActive` | `coolingActive` | NEW — from latest TelemetryReading.coolingActive or derived; defaults `false` |
| `firmware` | `firmware` | NEW — from Container.firmwareVersion; `"unknown"` when null |
| `locked` | `locked` | NEW — from Container.locked; `false` when null |
| `connected` | `connected` | NEW — derived: last telemetry < 5 min ago |
| `currentLocation` | `location` | Renamed + reformatted: human-readable string from lat/lng or address |
| `deviceId` | `dispositivoId` | Renamed |
| `productType` | `productType` | Unchanged |
| `lastUpdate` | `ultimaSync` | Renamed + formatted `dd/MM/yyyy HH:mm` (es-PE); `"—"` when null |
| `temperature` | `temperatura` | Renamed |
| `humidity` | `humedad` | Renamed |
| `batteryLevel` | `bateria` | Renamed |

**BEFORE** (old `ContainerResource`):
```json
{
  "id": 1, "containerId": "CG-001", "name": "Caja Quito",
  "status": "active", "currentLocation": { "lat": -0.18, "lng": -78.46 },
  "temperature": 4.2, "humidity": 65, "batteryLevel": 85,
  "lastUpdate": "2026-06-01T14:35:00", "productType": "Vacunas",
  "deviceId": "DEV-001", "operatorId": 3
}
```

**AFTER** (new `ContainerResource`):
```json
{
  "id": "CG-001", "nombre": "Caja Quito", "estado": "activo",
  "temperature": "4.2°C", "humedad": "65%", "bateria": 85,
  "coolingActive": true, "firmware": "1.2.3", "locked": true,
  "connected": true, "location": "Quito Centro",
  "productType": "Vacunas", "ultimaSync": "01/06/2026 14:35",
  "dispositivoId": "DEV-001"
}
```

**Cross-BC contract — `ContainerQueryService.getCode(containerId: Long): String`**:
The monitoring BC MUST expose a query service interface `ContainerQueryService` with method `getCode(containerId: Long): String` that returns `Container.containerId` given a `Container.id` (Long). This is REQUIRED by the evaluation BC to resolve `Alert.boxId` from `Alert.containerId`. If the container is not found, this method SHALL return `null`.

**Cross-BC contract — `DeviceQueryService.getRouteInfo(containerCode: String): RouteInfoDto | null`**:
The monitoring BC MUST expose a query service interface `DeviceQueryService` with method `getRouteInfo(containerCode: String): RouteInfoDto | null` where `RouteInfoDto` has `tripCode: String`, `latitude: Double`, `longitude: Double`. This is an internal cross-BC contract from monitoring to logistics. The logistics BC MUST provide the implementation that joins Route.containerId (Long) to Container.id, then maps Route.routeId → tripCode and Route.currentLocation → lat/lng. If no active route exists for the container, this method SHALL return `null`.

#### Scenario: US13 — GET /containers returns containers with new fields

- GIVEN 3 containers exist: CG-001 (active, has telemetry< 5 min ago, locked=true, firmware="1.2.3"), CG-002 (active, no telemetry, unlocked, firmware=null), CG-003 (inactive)
- WHEN client requests `GET /api/v1/containers?size=100`
- THEN response SHALL be HTTP 200 with `content` array where each item includes `id` (containerId), `nombre` (name), `estado`, `coolingActive`, `firmware`, `locked`, `connected`, `location`, `dispositivoId`, `productType`, `ultimaSync`, `temperatura`, `humedad`, `bateria`
- AND CG-001 SHALL have `connected=true`, `locked=true`, `firmware="1.2.3"`
- AND CG-002 SHALL have `connected=false`, `locked=false`, `firmware="unknown"`
- AND CG-003 SHALL have `estado="inactivo"`, `connected=false`
- **Test**: `should_return_containers_with_cooling_active_firmware_locked_connected`

#### Scenario: US13 — Container with no telemetry has connected=false

- GIVEN a container exists with no telemetry readings
- WHEN client requests `GET /api/v1/containers`
- THEN that container's `connected` SHALL be `false`
- AND `temperatura` SHALL be `"N/A"`, `humedad` SHALL be `"N/A"`, `bateria` SHALL be `"N/A"`
- **Test**: `should_return_na_fields_when_no_telemetry_for_container`

#### Scenario: US13 — GET /containers/{id} returns single container with new fields

- GIVEN a container CG-001 exists with firmware="2.0.0", locked=true, with recent telemetry
- WHEN client requests `GET /api/v1/containers/1`
- THEN response SHALL be HTTP 200 with container object including `firmware="2.0.0"`, `locked=true`, `connected=true`
- **Test**: `should_return_single_container_with_new_fields`

#### Scenario: US14 — POST /containers accepts optional firmware and locked fields

- GIVEN no container exists with containerId "CG-999"
- WHEN client submits `POST /api/v1/containers` with `{ "containerId": "CG-999", "name": "Nueva Caja", "deviceId": "DEV-999", "productType": "Vacunas", "firmware": "1.0.0", "locked": false }`
- THEN response SHALL be HTTP 201 with container where `firmware="1.0.0"` and `locked=false`
- **Test**: `should_create_container_with_firmware_and_locked_fields`

#### Scenario: US14 — POST /containers without optional fields uses defaults

- GIVEN no container exists with containerId "CG-998"
- WHEN client submits `POST /api/v1/containers` with `{ "containerId": "CG-998", "name": "Caja Sin Firmware", "deviceId": "DEV-998", "productType": "Vacunas" }` (no firmware, no locked)
- THEN response SHALL be HTTP 201 with container where `firmware="unknown"` and `locked=false`
- **Test**: `should_create_container_with_default_firmware_and_locked`

#### Scenario: US15 — PUT /containers/{id} updates firmware and locked

- GIVEN a container exists with id=1, firmware="1.0.0", locked=false
- WHEN client submits `PUT /api/v1/containers/1` with `{ "name": "Updated Caja", "firmware": "2.0.0", "locked": true }`
- THEN response SHALL be HTTP 200 with container where `firmware="2.0.0"` and `locked=true`
- **Test**: `should_update_container_firmware_and_locked`

#### Scenario: US15 — PUT /containers/{id} without optional fields preserves existing

- GIVEN a container exists with id=1, firmware="1.0.0", locked=true
- WHEN client submits `PUT /api/v1/containers/1` with `{ "name": "Updated" }` (no firmware, no locked)
- THEN response SHALL be HTTP 200 with container where `firmware="1.0.0"` and `locked=true` (unchanged)
- **Test**: `should_preserve_existing_firmware_and_locked_when_not_in_update`

#### Scenario: US13 — connected=true when latest telemetry within 5 minutes

- GIVEN a container has a telemetry reading with timestamp within the last 4 minutes
- WHEN client requests `GET /api/v1/containers`
- THEN that container's `connected` SHALL be `true`
- **Test**: `should_return_connected_true_when_telemetry_within_5_minutes`

#### Scenario: US13 — connected=false when latest telemetry older than 5 minutes

- GIVEN a container has a telemetry reading with timestamp older than 5 minutes
- WHEN client requests `GET /api/v1/containers`
- THEN that container's `connected` SHALL be `false`
- **Test**: `should_return_connected_false_when_telemetry_older_than_5_minutes`

---

## ADDED Requirements

### Requirement: device-monitoring

The system SHALL expose `GET /api/v1/devices` which returns a real-time aggregated view of all containers with their latest telemetry, online status, location, cooling state, battery, and active alert IDs. This endpoint aggregates data from the Container entity, TelemetryReading table, and cross-BC route information.

**DeviceResource response shape**:

| Field | Type | Source | Format/Notes |
|-------|------|--------|--------------|
| `id` | String | Container.containerId | e.g. "CG-001" |
| `tripCode` | String\|null | Route.routeId via DeviceQueryService | null if container not in active route |
| `temperature` | String | TelemetryReading.temperature | Formatted "X.X°C"; "N/A" when no reading |
| `humidity` | String | TelemetryReading.humidity | Formatted "X%"; "N/A" when no reading |
| `battery` | String | TelemetryReading.batteryLevel | Formatted "X%"; "N/A" when no reading |
| `cooling` | String | TelemetryReading.coolingActive | "On" or "Off"; "N/A" when no reading |
| `location` | String | TelemetryReading lat/lng or Container.currentLocation | Human-readable; "Ubicación desconocida" when unavailable |
| `online` | Boolean | Derived: latest telemetry < 5 min ago | `true`/`false` |
| `locked` | Boolean | Container.locked | `false` when null |
| `status` | String | Derived | "normal" when online + no alerts; "warning" when online + alerts; "offline" when not online |
| `latitude` | Double\|null | TelemetryReading.latitude or Container.currentLocation | From latest telemetry; null when unavailable |
| `longitude` | Double\|null | TelemetryReading.longitude or Container.currentLocation | From latest telemetry; null when unavailable |
| `activeAlerts` | String[] | Alert.alertId for active alerts on this container | Empty array when no alerts |
| `lastSync` | String | TelemetryReading.timestamp | Formatted relative time "Hace X seg/min/horas"; "Sin datos" when no reading |
| `temperatureWarning` | Boolean | Derived: temperature > container threshold | `true` if latest temp exceeds threshold; `false` otherwise; `false` when no reading |

**Status derivation logic**:
- `online = true` AND `activeAlerts.length == 0` → `status = "normal"`
- `online = true` AND `activeAlerts.length > 0` → `status = "warning"`
- `online = false` → `status = "offline"`

**Temperature threshold**: When a container has `temperatureMin`/`temperatureMax` set (from CreateContainerCommand), `temperatureWarning = true` if latest temperature is outside that range. If no thresholds are set, `temperatureWarning = false`.

**Cross-BC joins**:
- `tripCode`: resolved via `DeviceQueryService.getRouteInfo(containerCode)` → `RouteInfoDto.tripCode`. If null, `tripCode = null`.
- `latitude`/`longitude`: prefer latest TelemetryReading; fall back to Container.currentLocation if no telemetry.
- `activeAlerts`: query evaluation BC for alerts where `Alert.containerId = Container.id` AND `Alert.resolved = false`. If evaluation BC is unavailable, return empty array.

#### Scenario: US20 — GET /devices returns all devices with full telemetry

- GIVEN 2 containers: CG-001 (in active route V-2024-0156, latest telemetry 30 sec ago, temp=4.2, humidity=65, battery=85, coolingActive=true, locked=true) and CG-002 (not in route, latest telemetry 2 hours ago, temp=8.1, humidity=50, battery=20, coolingActive=false, locked=false)
- WHEN client requests `GET /api/v1/devices`
- THEN response SHALL be HTTP 200 with an array of 2 device objects
- AND CG-001 device SHALL have `id="CG-001"`, `tripCode="V-2024-0156"`, `temperature="4.2°C"`, `humidity="65%"`, `battery="85%"`, `cooling="On"`, `online=true`, `locked=true`, `status="normal"`, `temperatureWarning=false`
- AND CG-002 device SHALL have `id="CG-002"`, `tripCode=null`, `temperature="8.1°C"`, `online=false`, `status="offline"`
- **Test**: `should_return_all_devices_with_telemetry_aggregation`

#### Scenario: US20 — Device with no telemetry returns N/A values and offline status

- GIVEN a container CG-003 exists with no telemetry readings
- WHEN client requests `GET /api/v1/devices`
- THEN CG-003 device SHALL have `temperature="N/A"`, `humidity="N/A"`, `battery="N/A"`, `cooling="N/A"`, `online=false`, `status="offline"`, `lastSync="Sin datos"`, `latitude=null`, `longitude=null`
- **Test**: `should_return_na_fields_and_offline_for_device_with_no_telemetry`

#### Scenario: US20 — Device not in any active route has tripCode=null

- GIVEN a container CG-004 exists but is not assigned to any active route
- WHEN client requests `GET /api/v1/devices`
- THEN CG-004 device SHALL have `tripCode=null`
- AND `status` SHALL be derived from online state only (normal/warning/offline)
- **Test**: `should_return_null_tripcode_when_container_not_in_active_route`

#### Scenario: US20 — Device with no firmware info shows "unknown"

- GIVEN a container CG-005 exists with `firmwareVersion=null`
- WHEN client requests `GET /api/v1/devices`
- THEN CG-005 device SHALL have `firmware="unknown"` in the container resource (not in device resource — firmware is on ContainerResource, not DeviceResource)
- **Test**: `should_return_unknown_firmware_when_container_firmware_is_null`

#### Scenario: US20 — Multiple telemetry readings in last 5 min uses most recent

- GIVEN a container CG-006 has 3 telemetry readings in the last 5 minutes: 5 min ago (temp=5.0), 3 min ago (temp=4.5), 1 min ago (temp=4.2)
- WHEN client requests `GET /api/v1/devices`
- THEN CG-006 device SHALL use the 1-min-ago reading for temperature, humidity, battery, cooling, latitude, longitude
- AND `lastSync` SHALL reflect "Hace 1 min" (most recent)
- **Test**: `should_use_most_recent_telemetry_when_multiple_readings_exist`

#### Scenario: US20 — Device with active alerts has status="warning"

- GIVEN a container CG-007 is online (latest telemetry 1 min ago) and has 2 active alerts
- WHEN client requests `GET /api/v1/devices`
- THEN CG-007 device SHALL have `status="warning"` and `activeAlerts` array with 2 alert IDs
- **Test**: `should_return_warning_status_when_device_has_active_alerts`

#### Scenario: US20 — lastSync formats relative time correctly

- GIVEN a container has latest telemetry timestamp25 seconds ago
- WHEN client requests `GET /api/v1/devices`
- THEN `lastSync` SHALL be `"Hace25 seg"`
- AND GIVEN latest telemetry is 5 minutes ago → `lastSync = "Hace 5 min"`
- AND GIVEN latest telemetry is 2 hours ago → `lastSync = "Hace 2 horas"`
- **Test**: `should_format_last_sync_as_relative_time`

#### Scenario: US20 — temperatureWarning=true when temp exceeds threshold

- GIVEN a container CG-008 has temperatureMax=8.0 and latest telemetry temperature=8.5
- WHEN client requests `GET /api/v1/devices`
- THEN CG-008 device SHALL have `temperatureWarning=true`
- **Test**: `should_return_temperature_warning_true_when_exceeds_max`

#### Scenario: US20 — Empty device list returns empty array

- GIVEN no containers exist in the database
- WHEN client requests `GET /api/v1/devices`
- THEN response SHALL be HTTP 200 with body `[]`
- **Test**: `should_return_empty_array_when_no_containers_exist`

---

### Requirement: container-query-service-code-lookup

The monitoring BC SHALL provide `ContainerQueryService.getCode(containerId: Long): String` as a query method that resolves a Container database ID (Long) to its `containerId` field (String, e.g. "CG-001"). This method is REQUIRED by the evaluation BC to populate the `boxId` field in alert resources. If the container is not found, this method SHALL return `null`.

#### Scenario: getCode returns containerId for valid container id

- GIVEN a container exists with id=5 and containerId="CG-005"
- WHEN `ContainerQueryService.getCode(5)` is invoked
- THEN result SHALL be `"CG-005"`
- **Test**: `should_return_container_code_for_valid_id`

#### Scenario: getCode returns null for non-existent container id

- GIVEN no container exists with id=9999
- WHEN `ContainerQueryService.getCode(9999)` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_code_for_nonexistent_container_id`

---

### Requirement: device-query-service-route-info

The monitoring BC SHALL provide `DeviceQueryService.getRouteInfo(containerCode: String): RouteInfoDto | null` as a query method that resolves a container code (String, e.g. "CG-001") to its active route information. The `RouteInfoDto` SHALL contain `tripCode: String`, `latitude: Double`, `longitude: Double`. This method queries the logistics BC's Route aggregate — it joins `Route.containerId` (Long) to `Container.id`, then returns the active route's `routeId` as `tripCode` and its `currentLocation` coordinates. If no active route exists for the container, this method SHALL return `null`.

**Cross-BC dependency**: This method requires logistics BC to expose a query that finds active routes by container database ID. The logistics BC MUST provide `RouteQueryService.findActiveByContainerId(containerId: Long): Route | null`.

#### Scenario: getRouteInfo returns route info for container in active route

- GIVEN logistics BC has an active route with routeId="V-2024-0156", containerId=5 (matching CG-005), currentLocation with lat=-0.1807, lng=-78.4678
- WHEN `DeviceQueryService.getRouteInfo("CG-005")` is invoked
- THEN result SHALL be `{ tripCode: "V-2024-0156", latitude: -0.1807, longitude: -78.4678 }`
- **Test**: `should_return_route_info_for_container_in_active_route`

#### Scenario: getRouteInfo returns null when container not in active route

- GIVEN container CG-006 exists but is not assigned to any active route
- WHEN `DeviceQueryService.getRouteInfo("CG-006")` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_route_info_when_container_not_in_active_route`

#### Scenario: getRouteInfo returns null when container code not found

- GIVEN no container exists with containerId="NONEXISTENT"
- WHEN `DeviceQueryService.getRouteInfo("NONEXISTENT")` is invoked
- THEN result SHALL be `null`
- **Test**: `should_return_null_when_container_code_not_found`

---

## Cross-Cutting Notes

| Aspect | Detail |
|--------|--------|
| **Container entity changes** | Add `firmwareVersion` (String, nullable) and `locked` (Boolean, nullable, default false) |
| **TelemetryReading changes** | Add `coolingActive` (Boolean, nullable, default false) — tracks peltier/cooler state |
| **ContainerResource changes** | Add `coolingActive`, `firmware`, `locked`, `connected` fields; rename `name`→`nombre`, `deviceId`→`dispositivoId`, `lastUpdate`→`ultimaSync`; format temperature/humidity as strings |
| **CreateContainerResource changes** | Add `firmware` (String, optional), `locked` (Boolean, optional), `coolingActive` (Boolean, optional) |
| **Online derivation** | `online = true` when most recent TelemetryReading.timestamp is within 5 minutes of `LocalDateTime.now()` |
| **Cross-BC: evaluation** | `ContainerQueryService.getCode(containerId: Long)` is consumed by evaluation BC for Alert.boxId resolution |
| **Cross-BC: logistics** | `DeviceQueryService.getRouteInfo(containerCode)` queries logistics for active route by container ID |
| **Cross-BC: evaluation** | DeviceResource.activeAlerts requires evaluation BC query for unconfirmed alerts by container ID |
| **Temperature string format** | Formatted as `"X.X°C"` using `String.format("%.1f°C", value)` |
| **Humidity string format** | Formatted as `"X%"` using `String.format("%d%%", value.intValue())` |
| **Battery string format** | Formatted as `"X%"` using `String.format("%d%%", value)` |
| **lastSync relative time** | Use `Duration.between(telemetryTimestamp, now)` to compute: < 60 sec → "Hace X seg";< 60 min → "Hace X min"; else → "Hace X horas" |
| **Location string** | If TelemetryReading has lat/lng, format as human-readable string (coordinate string is acceptable for MVP); fall back to "Ubicación desconocida" |
| **Connected field** | Added to ContainerResource; derived at query time from telemetry freshness |
| **Firmware default** | When `Container.firmwareVersion` is null, serialize as `"unknown"` in responses |

---

## Test Coverage Summary

| Scenario | Test Name |
|----------|-----------|
| GET /containers with new fields | `should_return_containers_with_cooling_active_firmware_locked_connected` |
| Container with no telemetry → connected=false | `should_return_na_fields_when_no_telemetry_for_container` |
| GET /containers/{id} with new fields | `should_return_single_container_with_new_fields` |
| POST /containers accepts firmware/locked | `should_create_container_with_firmware_and_locked_fields` |
| POST /containers defaults | `should_create_container_with_default_firmware_and_locked` |
| PUT /containers updates firmware/locked | `should_update_container_firmware_and_locked` |
| PUT /containers preserves when absent | `should_preserve_existing_firmware_and_locked_when_not_in_update` |
| connected=true when telemetry < 5 min | `should_return_connected_true_when_telemetry_within_5_minutes` |
| connected=false when telemetry > 5 min | `should_return_connected_false_when_telemetry_older_than_5_minutes` |
| GET /devices full aggregation | `should_return_all_devices_with_telemetry_aggregation` |
| Device with no telemetry | `should_return_na_fields_and_offline_for_device_with_no_telemetry` |
| Device not in active route → tripCode=null | `should_return_null_tripcode_when_container_not_in_active_route` |
| Multiple telemetry → use most recent | `should_use_most_recent_telemetry_when_multiple_readings_exist` |
| Device with active alerts → warning | `should_return_warning_status_when_device_has_active_alerts` |
| lastSync relative time formatting | `should_format_last_sync_as_relative_time` |
| temperatureWarning exceeds threshold | `should_return_temperature_warning_true_when_exceeds_max` |
| Empty device list | `should_return_empty_array_when_no_containers_exist` |
| getCode valid id | `should_return_container_code_for_valid_id` |
| getCode non-existent | `should_return_null_code_for_nonexistent_container_id` |
| getRouteInfo active route | `should_return_route_info_for_container_in_active_route` |
| getRouteInfo no active route | `should_return_null_route_info_when_container_not_in_active_route` |
| getRouteInfo unknown code | `should_return_null_when_container_code_not_found` |
