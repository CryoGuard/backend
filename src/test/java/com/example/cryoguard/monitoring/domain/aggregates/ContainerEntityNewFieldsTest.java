package com.example.cryoguard.monitoring.domain.aggregates;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Container entity new fields: firmwareVersion, locked, coolingActive.
 * T3.1 - Add firmwareVersion, locked, coolingActive fields to Container.java
 */
class ContainerEntityNewFieldsTest {

    @Test
    void shouldHaveFirmwareVersionField() {
        // GIVEN a Container with firmwareVersion set
        Container container = new Container();
        container.setFirmwareVersion("1.2.3");

        // THEN firmwareVersion should be retrievable
        assertEquals("1.2.3", container.getFirmwareVersion());
    }

    @Test
    void shouldHaveLockedField() {
        // GIVEN a Container with locked set to true
        Container container = new Container();
        container.setLocked(true);

        // THEN locked should be retrievable and true
        assertTrue(container.getLocked());
    }

    @Test
    void shouldHaveCoolingActiveField() {
        // GIVEN a Container with coolingActive set to false
        Container container = new Container();
        container.setCoolingActive(false);

        // THEN coolingActive should be retrievable and false
        assertFalse(container.getCoolingActive());
    }

    @Test
    void shouldPersistAllNewFields() {
        // GIVEN a Container with all new fields set
        Container container = new Container();
        container.setFirmwareVersion("2.0.0");
        container.setLocked(false);
        container.setCoolingActive(true);

        // THEN all fields should be correctly persisted and retrievable
        assertEquals("2.0.0", container.getFirmwareVersion());
        assertFalse(container.getLocked());
        assertTrue(container.getCoolingActive());
    }

    @Test
    void shouldAllowNullFirmwareVersion() {
        // GIVEN a Container without firmwareVersion set
        Container container = new Container();

        // THEN firmwareVersion should be null (nullable field)
        assertNull(container.getFirmwareVersion());
    }

    @Test
    void shouldAllowNullLocked() {
        // GIVEN a Container without locked set
        Container container = new Container();

        // THEN locked should be null (nullable field)
        assertNull(container.getLocked());
    }

    @Test
    void shouldAllowNullCoolingActive() {
        // GIVEN a Container without coolingActive set
        Container container = new Container();

        // THEN coolingActive should be null (nullable field)
        assertNull(container.getCoolingActive());
    }
}
