package com.example.cryoguard.evaluation.presentation.resources;

/**
 * AlertIncidentResource - Vue-shaped response DTO for alert endpoints.
 *
 * Replaces the old AlertResource for all HTTP responses consumed by the Vue frontend.
 *
 * Fields:
 * - id: String (Alert.alertId, e.g. "ALT-001")
 * - severity: String ("critica" or "advertencia")
 * - status: String ("activa", "pendiente", "confirmada")
 * - message: String (alert message)
 * - boxId: String (Container.code via monitoring BC, or stub Long as String)
 * - tripCode: String (Route.code via logistics BC, or "Sin viaje")
 * - value: String (triggerValue - raw sensor reading that caused the alert)
 * - timestamp: String (dd/MM/yyyy HH:mm, es-PE locale)
 *
 * T2.3 - Create AlertIncidentResource
 */
public record AlertIncidentResource(
    String id,
    String severity,
    String status,
    String message,
    String boxId,
    String tripCode,
    String value,
    String timestamp
) {
}