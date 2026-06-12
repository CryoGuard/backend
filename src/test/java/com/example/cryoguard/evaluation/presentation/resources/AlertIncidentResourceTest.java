package com.example.cryoguard.evaluation.presentation.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlertIncidentResource - the new Vue-shaped response DTO.
 * T2.3 - Create AlertIncidentResource with fields: id, severity, status, message,
 *        boxId, tripCode, value, timestamp
 *
 * This resource replaces the old AlertResource for all HTTP responses.
 */
class AlertIncidentResourceTest {

    @Test
    @DisplayName("should create AlertIncidentResource with all fields")
    void shouldCreateAlertIncidentResourceWithAllFields() {
        // GIVEN AlertIncidentResource record
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-001",       // id
            "critica",       // severity
            "activa",        // status
            "Temperatura superior a 8°C detectada",  // message
            "CG-047",        // boxId
            "V-2024-0157",   // tripCode
            "8.5°C",         // value
            "01/06/2026 14:35"  // timestamp
        );

        // THEN all fields should be accessible
        assertEquals("ALT-001", resource.id());
        assertEquals("critica", resource.severity());
        assertEquals("activa", resource.status());
        assertEquals("Temperatura superior a 8°C detectada", resource.message());
        assertEquals("CG-047", resource.boxId());
        assertEquals("V-2024-0157", resource.tripCode());
        assertEquals("8.5°C", resource.value());
        assertEquals("01/06/2026 14:35", resource.timestamp());
    }

    @Test
    @DisplayName("should allow advertencia severity")
    void shouldAllowAdvertenciaSeverity() {
        // GIVEN AlertIncidentResource with advertencia severity
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-002", "advertencia", "pendiente",
            "Humedad elevada detectada", "CG-012", "Sin viaje", "78%", "02/06/2026 09:00"
        );

        // THEN severity should be advertencia
        assertEquals("advertencia", resource.severity());
    }

    @Test
    @DisplayName("should allow confirmed status")
    void shouldAllowConfirmadaStatus() {
        // GIVEN AlertIncidentResource with confirmada status
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-003", "critica", "confirmada",
            "Temperatura normalizada", "CG-047", "V-2024-0157", "6.5°C", "02/06/2026 10:30"
        );

        // THEN status should be confirmada
        assertEquals("confirmada", resource.status());
    }

    @Test
    @DisplayName("should allow Sin viaje as tripCode")
    void shouldAllowSinViajeAsTripCode() {
        // GIVEN AlertIncidentResource when alert has no trip
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-004", "advertencia", "activa",
            "Sensor desconectado", "CG-099", "Sin viaje", null, "03/06/2026 08:00"
        );

        // THEN tripCode should be Sin viaje
        assertEquals("Sin viaje", resource.tripCode());
    }

    @Test
    @DisplayName("should allow null value (no sensor reading)")
    void shouldAllowNullValue() {
        // GIVEN AlertIncidentResource with null value
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-005", "critica", "activa",
            "Sensor no responde", "CG-047", "V-2024-0157", null, "03/06/2026 11:00"
        );

        // THEN value should be null
        assertNull(resource.value());
    }

    @Test
    @DisplayName("should use alertId format ALT-XXX")
    void shouldUseAlertIdFormat() {
        // GIVEN AlertIncidentResource
        AlertIncidentResource resource = new AlertIncidentResource(
            "ALT-123", "critica", "activa",
            "Test alert", "CG-001", "V-2024-0001", "5.0°C", "04/06/2026 12:00"
        );

        // THEN id should match ALT-XXX pattern
        assertTrue(resource.id().startsWith("ALT-"));
        assertEquals(7, resource.id().length()); // ALT- + 3 digits
    }
}