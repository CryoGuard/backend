package com.example.cryoguard.evaluation.domain.entities;

import com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Alert entity fields: triggerValue and tripId.
 * T2.1 - Add triggerValue and tripId fields to Alert entity
 */
class AlertEntityTriggerValueAndTripIdTest {

    @Test
    @DisplayName("should have triggerValue field")
    void shouldHaveTriggerValueField() {
        // GIVEN an Alert entity
        Alert alert = new Alert();

        // WHEN setting triggerValue
        alert.setTriggerValue("8.5°C");

        // THEN triggerValue should be accessible
        assertEquals("8.5°C", alert.getTriggerValue());
    }

    @Test
    @DisplayName("should have tripId field")
    void shouldHaveTripIdField() {
        // GIVEN an Alert entity
        Alert alert = new Alert();

        // WHEN setting tripId
        alert.setTripId(42L);

        // THEN tripId should be accessible
        assertEquals(42L, alert.getTripId());
    }

    @Test
    @DisplayName("should allow null triggerValue")
    void shouldAllowNullTriggerValue() {
        // GIVEN an Alert entity
        Alert alert = new Alert();

        // WHEN triggerValue is null (not all alerts have sensor readings)
        // THEN no exception is thrown
        assertNull(alert.getTriggerValue());
    }

    @Test
    @DisplayName("should allow null tripId")
    void shouldAllowNullTripId() {
        // GIVEN an Alert entity
        Alert alert = new Alert();

        // WHEN alert is generated when container is not on a route
        alert.setTripId(null);

        // THEN tripId should be null
        assertNull(alert.getTripId());
    }

    @Test
    @DisplayName("should preserve triggerValue and other fields together")
    void shouldPreserveTriggerValueWithOtherFields() {
        // GIVEN an Alert with multiple fields set
        Alert alert = new Alert();
        alert.setAlertId("ALT-001");
        alert.setContainerId(5L);
        alert.setAlertType(AlertType.TEMPERATURE);
        alert.setSeverity(AlertSeverity.CRITICAL);
        alert.setMessage("Temperature exceeded threshold");
        alert.setTimestamp(LocalDateTime.now());
        alert.setAcknowledged(false);
        alert.setResolved(false);
        alert.setTriggerValue("8.5°C");
        alert.setTripId(10L);
        alert.setLatitude(new BigDecimal("40.7128"));
        alert.setLongitude(new BigDecimal("-74.0060"));

        // THEN all fields should be preserved
        assertEquals("ALT-001", alert.getAlertId());
        assertEquals(5L, alert.getContainerId());
        assertEquals(AlertType.TEMPERATURE, alert.getAlertType());
        assertEquals(AlertSeverity.CRITICAL, alert.getSeverity());
        assertEquals("Temperature exceeded threshold", alert.getMessage());
        assertNotNull(alert.getTimestamp());
        assertFalse(alert.getAcknowledged());
        assertFalse(alert.getResolved());
        assertEquals("8.5°C", alert.getTriggerValue());
        assertEquals(10L, alert.getTripId());
        assertEquals(new BigDecimal("40.7128"), alert.getLatitude());
        assertEquals(new BigDecimal("-74.0060"), alert.getLongitude());
    }
}