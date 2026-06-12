package com.example.cryoguard.monitoring.domain.entities;

import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for TelemetryReading entity new field: coolingActive.
 * T3.2 - Add coolingActive field to TelemetryReading entity
 */
class TelemetryReadingCoolingActiveTest {

    @Test
    void shouldHaveCoolingActiveField() {
        // GIVEN a TelemetryReading with coolingActive set to true
        TelemetryReading reading = new TelemetryReading();
        reading.setCoolingActive(true);

        // THEN coolingActive should be retrievable and true
        assertTrue(reading.getCoolingActive());
    }

    @Test
    void shouldHaveCoolingActiveFieldFalse() {
        // GIVEN a TelemetryReading with coolingActive set to false
        TelemetryReading reading = new TelemetryReading();
        reading.setCoolingActive(false);

        // THEN coolingActive should be retrievable and false
        assertFalse(reading.getCoolingActive());
    }

    @Test
    void shouldPersistCoolingActiveField() {
        // GIVEN a TelemetryReading with all fields set including coolingActive
        TelemetryReading reading = new TelemetryReading();
        reading.setContainerId(1L);
        reading.setTimestamp(LocalDateTime.now());
        reading.setTemperature(new BigDecimal("2.5"));
        reading.setCoolingActive(true);

        // THEN all fields should be correctly persisted and retrievable
        assertEquals(1L, reading.getContainerId());
        assertTrue(reading.getCoolingActive());
    }

    @Test
    void shouldAllowNullCoolingActive() {
        // GIVEN a TelemetryReading without coolingActive set
        TelemetryReading reading = new TelemetryReading();

        // THEN coolingActive should be null (nullable field)
        assertNull(reading.getCoolingActive());
    }
}
