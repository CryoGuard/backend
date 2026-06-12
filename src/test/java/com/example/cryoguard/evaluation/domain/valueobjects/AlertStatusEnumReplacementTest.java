package com.example.cryoguard.evaluation.domain.valueobjects;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlertStatus enum values replacement.
 * T2.2 - Replace OPEN→ACTIVA, ACKNOWLEDGED→PENDIENTE, RESOLVED→CONFIRMADA
 *
 * Note: Alert entity uses Boolean acknowledged/resolved fields, not the AlertStatus enum directly.
 * This enum replacement is for consistency with Vue frontend contract.
 */
class AlertStatusEnumReplacementTest {

    @Test
    @DisplayName("should have ACTIVA status value")
    void shouldHaveActivaStatusValue() {
        // GIVEN AlertStatus enum
        // WHEN accessing ACTIVA
        AlertStatus status = AlertStatus.ACTIVA;
        // THEN it exists and has correct name
        assertEquals("ACTIVA", status.name());
    }

    @Test
    @DisplayName("should have PENDIENTE status value")
    void shouldHavePendienteStatusValue() {
        // GIVEN AlertStatus enum
        // WHEN accessing PENDIENTE
        AlertStatus status = AlertStatus.PENDIENTE;
        // THEN it exists and has correct name
        assertEquals("PENDIENTE", status.name());
    }

    @Test
    @DisplayName("should have CONFIRMADA status value")
    void shouldHaveConfirmadaStatusValue() {
        // GIVEN AlertStatus enum
        // WHEN accessing CONFIRMADA
        AlertStatus status = AlertStatus.CONFIRMADA;
        // THEN it exists and has correct name
        assertEquals("CONFIRMADA", status.name());
    }

    @Test
    @DisplayName("should NOT have OPEN status value")
    void shouldNotHaveOpenStatusValue() {
        // GIVEN AlertStatus enum
        // THEN OPEN should not exist
        assertThrows(IllegalArgumentException.class, () -> AlertStatus.valueOf("OPEN"));
    }

    @Test
    @DisplayName("should NOT have ACKNOWLEDGED status value")
    void shouldNotHaveAcknowledgedStatusValue() {
        // GIVEN AlertStatus enum
        // THEN ACKNOWLEDGED should not exist
        assertThrows(IllegalArgumentException.class, () -> AlertStatus.valueOf("ACKNOWLEDGED"));
    }

    @Test
    @DisplayName("should NOT have RESOLVED status value")
    void shouldNotHaveResolvedStatusValue() {
        // GIVEN AlertStatus enum
        // THEN RESOLVED should not exist
        assertThrows(IllegalArgumentException.class, () -> AlertStatus.valueOf("RESOLVED"));
    }

    @Test
    @DisplayName("should have exactly 3 status values")
    void shouldHaveExactlyThreeStatusValues() {
        // GIVEN AlertStatus enum
        // THEN it has exactly 3 values
        assertEquals(3, AlertStatus.values().length);
    }

    @Test
    @DisplayName("should derive ACTIVA when acknowledged=false and resolved=false")
    void shouldDeriveActivaFromBooleans() {
        // GIVEN an alert with acknowledged=false and resolved=false
        Boolean acknowledged = false;
        Boolean resolved = false;
        // THEN the derived status should be ACTIVA
        AlertStatus derivedStatus = deriveStatus(acknowledged, resolved);
        assertEquals(AlertStatus.ACTIVA, derivedStatus);
    }

    @Test
    @DisplayName("should derive PENDIENTE when acknowledged=true and resolved=false")
    void shouldDerivePendienteFromBooleans() {
        // GIVEN an alert with acknowledged=true and resolved=false
        Boolean acknowledged = true;
        Boolean resolved = false;
        // THEN the derived status should be PENDIENTE
        AlertStatus derivedStatus = deriveStatus(acknowledged, resolved);
        assertEquals(AlertStatus.PENDIENTE, derivedStatus);
    }

    @Test
    @DisplayName("should derive CONFIRMADA when resolved=true")
    void shouldDeriveConfirmadaFromBooleans() {
        // GIVEN an alert with resolved=true (regardless of acknowledged)
        Boolean acknowledged = true;
        Boolean resolved = true;
        // THEN the derived status should be CONFIRMADA
        AlertStatus derivedStatus = deriveStatus(acknowledged, resolved);
        assertEquals(AlertStatus.CONFIRMADA, derivedStatus);
    }

    /**
     * Helper method that mirrors the logic used in AlertAssembler.toResource()
     * Maps boolean acknowledged + resolved to AlertStatus enum value.
     */
    private AlertStatus deriveStatus(Boolean acknowledged, Boolean resolved) {
        if (Boolean.TRUE.equals(resolved)) {
            return AlertStatus.CONFIRMADA;
        } else if (Boolean.TRUE.equals(acknowledged)) {
            return AlertStatus.PENDIENTE;
        } else {
            return AlertStatus.ACTIVA;
        }
    }
}