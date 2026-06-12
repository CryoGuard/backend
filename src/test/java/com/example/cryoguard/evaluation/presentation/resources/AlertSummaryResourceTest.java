package com.example.cryoguard.evaluation.presentation.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlertSummaryResource - dashboard card DTO.
 * T2.4 - Create AlertSummaryResource for dashboard: id, tipo, caja, tiempo, severidad
 *
 * Used by US06 dashboard recent alerts (limit=3&sort=reciente).
 */
class AlertSummaryResourceTest {

    @Test
    @DisplayName("should create AlertSummaryResource with all fields")
    void shouldCreateAlertSummaryResourceWithAllFields() {
        // GIVEN AlertSummaryResource
        AlertSummaryResource resource = new AlertSummaryResource(
            "ALT-001",
            "TEMPERATURE",
            "CG-047",
            "Hace 5 minutos",
            "critica"
        );

        // THEN all fields should be accessible
        assertEquals("ALT-001", resource.id());
        assertEquals("TEMPERATURE", resource.tipo());
        assertEquals("CG-047", resource.caja());
        assertEquals("Hace 5 minutos", resource.tiempo());
        assertEquals("critica", resource.severidad());
    }

    @Test
    @DisplayName("should allow advertencia severity")
    void shouldAllowAdvertenciaSeverity() {
        // GIVEN AlertSummaryResource with advertencia severity
        AlertSummaryResource resource = new AlertSummaryResource(
            "ALT-002", "HUMIDITY", "CG-012", "Hace 2 horas", "advertencia"
        );

        // THEN severity should be advertencia
        assertEquals("advertencia", resource.severidad());
    }

    @Test
    @DisplayName("should allow relative time formats")
    void shouldAllowRelativeTimeFormats() {
        // GIVEN various relative time formats
        AlertSummaryResource minutes = new AlertSummaryResource(
            "ALT-003", "DOOR", "CG-001", "Hace 15 minutos", "critica"
        );
        AlertSummaryResource hours = new AlertSummaryResource(
            "ALT-004", "VIBRATION", "CG-002", "Hace 3 horas", "advertencia"
        );
        AlertSummaryResource days = new AlertSummaryResource(
            "ALT-005", "TEMPERATURE", "CG-003", "Hace 2 días", "critica"
        );

        // THEN each should preserve the relative time string
        assertEquals("Hace 15 minutos", minutes.tiempo());
        assertEquals("Hace 3 horas", hours.tiempo());
        assertEquals("Hace 2 días", days.tiempo());
    }

    @Test
    @DisplayName("should use alertId format ALT-XXX")
    void shouldUseAlertIdFormat() {
        // GIVEN AlertSummaryResource
        AlertSummaryResource resource = new AlertSummaryResource(
            "ALT-099", "TEMPERATURE", "CG-001", "Hace 1 minuto", "critica"
        );

        // THEN id should match ALT-XXX pattern
        assertTrue(resource.id().startsWith("ALT-"));
        assertEquals(7, resource.id().length());
    }
}