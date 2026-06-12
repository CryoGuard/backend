package com.example.cryoguard.evaluation.presentation.resources;

/**
 * AlertSummaryResource - dashboard card DTO for recent alerts.
 *
 * Used by US06 dashboard: GET /api/v1/alertas?limit=3&sort=reciente
 *
 * Fields:
 * - id: String (Alert.alertId, e.g. "ALT-001")
 * - tipo: String (AlertType name, e.g. "TEMPERATURE", "HUMIDITY")
 * - caja: String (Container.code via monitoring BC)
 * - tiempo: String (relative time, e.g. "Hace 5 minutos")
 * - severidad: String ("critica" or "advertencia")
 *
 * T2.4 - Create AlertSummaryResource for dashboard card
 */
public record AlertSummaryResource(
    String id,
    String tipo,
    String caja,
    String tiempo,
    String severidad
) {
}