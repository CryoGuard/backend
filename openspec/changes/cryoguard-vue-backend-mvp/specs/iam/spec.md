# Delta for IAM — cryoguard-vue-backend-mvp

## MODIFIED Requirements

### Requirement: user-management

The system SHALL expose user management endpoints for authentication and operator lifecycle. The User entity MUST be extended with an optional `telefono` field (String, max 20 characters, nullable). All UserResource responses returned from `GET /users`, `POST /users`, `PUT /users/{id}`, `POST /auth/login`, and `POST /auth/sign-up` MUST include the following computed fields derived from existing entity data:

- `pinBloqueado` (boolean) — derived: `true` when `status === "LOCKED"`, else `false`
- `viajesAsignados` (integer) — count of Route entities where `authorizedOperator.id = this user.id` AND `status IN (INITIATED, IN_PROGRESS)`; MUST be `0` when user has no assigned routes
- `viajesCompletados` (integer) — count of Route entities where `authorizedOperator.id = this user.id` AND `status = COMPLETED`; MUST be `0` when user has no completed routes
- `ultimaActividad` (string, formatted) — derived from `lastLogin`; format MUST be `dd/MM/yyyy HH:mm` (es-PE locale); when `lastLogin` is null, return `"Sin actividad"`

The `telefono` field is NEW. The four computed fields above are NEW additions to the UserResource response shape.

**Cross-BC Contract (operator-stats)**: The `viajesAsignados` and `viajesCompletados` computed fields require iam to query the logistics BC's Route aggregate. The iam application layer query `GetUserStats(userId)` SHALL call a cross-BC query service exposed by logistics. The logistics BC MUST provide a query with signature:

```
GetRoutesByOperatorQuery(operatorId: Long): { activeCount: Int, completedCount: Int }
```

where `activeCount` = count of Routes with `authorizedOperator.id = operatorId` AND `status IN (INITIATED, IN_PROGRESS)` and `completedCount` = count of Routes with `authorizedOperator.id = operatorId` AND `status = COMPLETED`.

If the logistics BC does not yet expose this query, the implementation MAY use a stub that returns `{ activeCount: 0, completedCount: 0 }` for this iteration, provided the stub interface matches the contract above.

#### Scenario: US01 — Login returns user with computed fields

- GIVEN a user with email `operator@cryoguard.com`, password `Pin1234`, status `ACTIVE`, and no lastLogin
- WHEN client submits `POST /api/v1/auth/login` with `{ "email": "operator@cryoguard.com", "password": "Pin1234" }`
- THEN response SHALL be HTTP 200 with `{ "token": "<jwt>", "user": { "id": <id>, "name": "...", "email": "...", "role": "...", "status": "active", "pinBloqueado": false, "viajesAsignados": 0, "viajesCompletados": 0, "ultimaActividad": "Sin actividad" } }`
- AND `lastLogin` SHALL be updated to current timestamp
- **Test**: `should_return_user_with_computed_fields_on_successful_login`

#### Scenario: US01 — Login with LOCKED status returns pinBloqueado=true

- GIVEN a user with email `locked@cryoguard.com`, password `Pin1234`, and status `LOCKED`
- WHEN client submits `POST /api/v1/auth/login` with `{ "email": "locked@cryoguard.com", "password": "Pin1234" }`
- THEN response SHALL be HTTP 200 with `user.pinBloqueado === true`
- **Test**: `should_return_pinBloqueado_true_when_user_status_is_locked`

#### Scenario: US01 — Login with invalid credentials returns401

- GIVEN no user exists with email `invalid@cryoguard.com`
- WHEN client submits `POST /api/v1/auth/login` with `{ "email": "invalid@cryoguard.com", "password": "wrong" }`
- THEN response SHALL return HTTP 401 Unauthorized
- **Test**: `should_return_401_when_credentials_are_invalid`

#### Scenario: US01 — Login with LOCKED account returns 403

- GIVEN a user with email `locked@cryoguard.com`, password `Pin1234`, and status `LOCKED`
- WHEN client submits `POST /api/v1/auth/login` with correct credentials
- THEN response SHALL return HTTP 403 Forbidden
- **Test**: `should_return_403_when_account_is_locked`

#### Scenario: US02 — Sign-up accepts optional telefono field

- GIVEN no user exists with email `newop@cryoguard.com`
- WHEN client submits `POST /api/v1/auth/sign-up` with `{ "username": "New Operator", "email": "newop@cryoguard.com", "password": "Pin1234", "role": "OPERATOR", "telefono": "+51 987 654 321" }`
- THEN response SHALL be HTTP 201 with user where `telefono === "+51 987 654 321"`
- AND `pinBloqueado` SHALL be `false`, `viajesAsignados` SHALL be `0`, `viajesCompletados` SHALL be `0`
- **Test**: `should_accept_telefono_on_sign_up`

#### Scenario: US02 — Sign-up without telefono stores null

- GIVEN no user exists with email `no-telefono@cryoguard.com`
- WHEN client submits `POST /api/v1/auth/sign-up` with `{ "username": "No Telefono", "email": "no-telefono@cryoguard.com", "password": "Pin1234", "role": "OPERATOR" }` (no telefono field)
- THEN response SHALL be HTTP 201 with user where `telefono` is `null`
- **Test**: `should_store_null_telefono_when_field_is_absent`

#### Scenario: US02 — Sign-up with telefono exceeding max length

- GIVEN no user exists with email `long-telefono@cryoguard.com`
- WHEN client submits `POST /api/v1/auth/sign-up` with `{ "username": "Long", "email": "long-telefono@cryoguard.com", "password": "Pin1234", "role": "OPERATOR", "telefono": "123456789012345678901" }` (21 characters)
- THEN response SHALL return HTTP 400 Bad Request
- **Test**: `should_reject_telefono_exceeding_20_characters`

#### Scenario: US08 — GET /users?role=OPERATOR returns operators with computed fields

- GIVEN 3 operator users exist with assigned routes (2 active, 1 completed for operator1;0 for others)
- WHEN client requests `GET /api/v1/users?role=OPERATOR&size=100`
- THEN response SHALL be HTTP 200 with `content` array where each item includes `telefono`, `pinBloqueado`, `viajesAsignados`, `viajesCompletados`, `ultimaActividad`
- AND operator with assigned routes SHALL have non-zero `viajesAsignados` or `viajesCompletados`
- **Test**: `should_return_operadores_with_computed_stats`

#### Scenario: US08 — GET /users returns zero stats when user has no routes

- GIVEN an operator user exists with no assigned routes
- WHEN client requests `GET /api/v1/users?role=OPERATOR&size=100`
- THEN response SHALL include that operator with `viajesAsignados === 0` and `viajesCompletados === 0`
- **Test**: `should_return_zero_viajes_asignados_when_user_has_no_routes`

#### Scenario: US09 — Create operator via POST /users accepts telefono

- GIVEN authenticated admin user
- WHEN client submits `POST /api/v1/users` with `{ "username": "Created Op", "email": "created@cryoguard.com", "password": "Pin1234", "role": "OPERATOR", "telefono": "+51 999 888 777" }`
- THEN response SHALL be HTTP 201 with user where `telefono === "+51 999 888 777"`
- **Test**: `should_create_operator_with_telefono`

#### Scenario: US10 — Update operator telefono via PUT /users/{id}

- GIVEN an operator user with `telefono === "+51 111 222 333"`
- WHEN client submits `PUT /api/v1/users/{id}` with `{ "username": "Updated Op", "email": "updated@cryoguard.com", "roles": ["OPERATOR"], "telefono": "+51 999 777 555" }`
- THEN response SHALL be HTTP 200 with user where `telefono === "+51 999 777 555"`
- **Test**: `should_update_telefono_on_operator`

#### Scenario: US10 — Update operator without telefono preserves existing value

- GIVEN an operator user with `telefono === "+51 111 222 333"`
- WHEN client submits `PUT /api/v1/users/{id}` with `{ "username": "Updated Op", "email": "updated@cryoguard.com", "roles": ["OPERATOR"] }` (no telefono field)
- THEN response SHALL be HTTP 200 with user where `telefono` is unchanged (`"+51 111 222 333"`)
- **Test**: `should_preserve_existing_telefono_when_field_is_absent_in_update`

#### Scenario: US11 — Activate/deactivate operator preserves computed fields

- GIVEN an operator user with status `ACTIVE`, `viajesAsignados: 3`, `viajesCompletados: 10`
- WHEN client submits `PUT /api/v1/users/{id}` with `{ "status": "INACTIVE" }`
- THEN response SHALL be HTTP 200 with `status === "inactive"` and `viajesAsignados === 3` and `viajesCompletados === 10`
- **Test**: `should_preserve_computed_fields_when_changing_status`

#### Scenario: US11 — Non-admin cannot create operator

- GIVEN authenticated operator user (not admin)
- WHEN client submits `POST /api/v1/users` with operator data
- THEN response SHALL return HTTP 403 Forbidden
- **Test**: `should_return_403_when_non_admin_creates_operator`

#### Scenario: US12 — Reset PIN returns new 4-digit numeric PIN

- GIVEN an operator user with id `5` exists
- WHEN client submits `POST /api/v1/users/5/reset-password` with empty body `{}`
- THEN response SHALL be HTTP 200 with `{ "newPin": "1234" }` where `newPin` matches `^\d{4}$`
- AND the user's stored password SHALL be updated to the hashed representation of the returned PIN
- AND user status SHALL remain unchanged (NOT automatically unlocked)
- **Test**: `should_return_new_4_digit_pin_and_update_hashed_password`

#### Scenario: US12 — Reset PIN for non-existent user returns 404

- GIVEN no user exists with id `9999`
- WHEN client submits `POST /api/v1/users/9999/reset-password` with `{}`
- THEN response SHALL return HTTP 404 Not Found
- **Test**: `should_return_404_when_resetting_pin_for_nonexistent_user`

#### Scenario: US12 — Reset PIN generates different PIN each time

- GIVEN an operator user with id `5`
- WHEN client submits `POST /api/v1/users/5/reset-password` twice in sequence
- THEN both responses SHALL contain a `newPin` matching `^\d{4}$`
- AND the two PINs MAY be different (random generation)
- **Test**: `should_generate_random_pin_not_predictable`

---

## ADDED Requirements

### Requirement: operator-stats

The system SHALL provide a `GetUserStats(userId)` query in the iam application layer that returns computed trip statistics for a given operator. The query SHALL call into the logistics BC to obtain route counts. The cross-BC call SHALL NOT block the HTTP response if logistics is unavailable — instead, it SHALL return `{ viajesAsignados: 0, viajesCompletados: 0 }` as a fallback.

The iam BC SHALL NOT directly query the logistics database. It SHALL use a cross-BC query interface (`LogisticsQueryService`) injected into iam's query service implementation.

#### Scenario: GetUserStats returns correct counts from logistics

- GIVEN logistics BC returns `{ activeCount: 3, completedCount: 12 }` for operator id `5`
- WHEN `GetUserStats(5)` is invoked
- THEN result SHALL be `{ viajesAsignados: 3, viajesCompletados: 12 }`
- **Test**: `should_return_viajes_stats_from_logistics_query`

#### Scenario: GetUserStats returns zeros when logistics returns null

- GIVEN logistics BC query returns `null` or throws an exception for operator id `5`
- WHEN `GetUserStats(5)` is invoked
- THEN result SHALL be `{ viajesAsignados: 0, viajesCompletados: 0 }` (graceful fallback)
- **Test**: `should_return_zero_stats_when_logistics_is_unavailable`

---

### Requirement: reset-user-pin

The system SHALL expose `POST /api/v1/users/{userId}/reset-password` as a command that generates a new random 4-digit numeric PIN, stores it as the user's hashed password (replacing the previous password), and returns the plain-text PIN in the response body. This endpoint SHALL NOT modify the user's status — if the account is `LOCKED`, it remains `LOCKED` after the PIN reset.

#### Scenario: Reset PIN updates password hash but not status

- GIVEN a locked operator user with id `5`
- WHEN client submits `POST /api/v1/users/5/reset-password` with `{}`
- THEN response SHALL be HTTP 200 with `{ "newPin": "1234" }`
- AND the user status SHALL remain `LOCKED` (no automatic unlock)
- AND subsequent login with the new PIN SHALL succeed
- **Test**: `should_not_unlock_account_after_pin_reset`

---

## REMOVED Requirements

None for this change.

---

## Cross-Cutting Notes

| Field | Type | Source | Notes |
|-------|------|--------|-------|
| `telefono` | String, nullable, @Size(max=20) | User entity field (NEW) | No E.164 validation; stored as-is |
| `pinBloqueado` | boolean | Computed from `status === "LOCKED"` | NEW computed field |
| `viajesAsignados` | integer | Cross-BC query to logistics Route | NEW computed field |
| `viajesCompletados` | integer | Cross-BC query to logistics Route | NEW computed field |
| `ultimaActividad` | String | Formatted from `lastLogin` | Format: `dd/MM/yyyy HH:mm` (es-PE); "Sin actividad" when null |

**Logistics BC contract**: `LogisticsQueryService.getRoutesByOperator(operatorId: Long): RouteStatsDto` where `RouteStatsDto` has `activeCount: Int` and `completedCount: Int`. The RouteStatus values used are `INITIATED`, `IN_PROGRESS` for active, and `COMPLETED` for completed. This requires logistics to add `authorizedOperator` field to Route entity and add the `INITIATED` and `IN_PROGRESS` values to RouteStatus enum — these are OUT OF SCOPE for iam spec but are REQUIRED dependencies for the cross-BC contract to function.
