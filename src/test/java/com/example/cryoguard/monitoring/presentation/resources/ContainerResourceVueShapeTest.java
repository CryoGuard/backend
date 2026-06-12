package com.example.cryoguard.monitoring.presentation.resources;

import com.example.cryoguard.monitoring.presentation.resources.ContainerResource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContainerResource Vue shape with Spanish field names and new fields.
 * T3.6 - Update ContainerResource to Vue shape
 */
class ContainerResourceVueShapeTest {

    @Test
    void shouldHaveAllVueFields() {
        // GIVEN a ContainerResource with all Vue fields
        LocalDateTime lastUpdate = LocalDateTime.now();
        ContainerResource.GpsLocationDTO location = new ContainerResource.GpsLocationDTO(
                new BigDecimal("-12.0464"), new BigDecimal("-77.0428"));

        ContainerResource resource = new ContainerResource(
                "CG-001",           // id (containerId)
                "Contenedor 1",     // nombre (name)
                "active",           // estado (status)
                new BigDecimal("2.5"),  // temperature
                new BigDecimal("65"), // humidity
                85,                 // batteryLevel
                true,               // coolingActive
                "1.2.3",            // firmware
                false,              // locked
                true,               // connected
                location,           // location
                "Vacunas",          // productType
                lastUpdate          // ultimaSync (lastUpdate)
        );

        // THEN all fields should be correctly set
        assertEquals("CG-001", resource.getId());
        assertEquals("Contenedor 1", resource.getNombre());
        assertEquals("active", resource.getEstado());
        assertEquals(new BigDecimal("2.5"), resource.getTemperature());
        assertEquals(new BigDecimal("65"), resource.getHumidity());
        assertEquals(85, resource.getBatteryLevel());
        assertTrue(resource.getCoolingActive());
        assertEquals("1.2.3", resource.getFirmware());
        assertFalse(resource.getLocked());
        assertTrue(resource.getConnected());
        assertNotNull(resource.getLocation());
        assertEquals(new BigDecimal("-12.0464"), resource.getLocation().getLat());
        assertEquals(new BigDecimal("-77.0428"), resource.getLocation().getLng());
        assertEquals("Vacunas", resource.getProductType());
        assertEquals(lastUpdate, resource.getUltimaSync());
    }

    @Test
    void shouldHaveGpsLocationDTO() {
        // GIVEN a GpsLocationDTO
        ContainerResource.GpsLocationDTO location = new ContainerResource.GpsLocationDTO(
                new BigDecimal("-12.0464"), new BigDecimal("-77.0428"));

        // THEN it should have lat and lng
        assertEquals(new BigDecimal("-12.0464"), location.getLat());
        assertEquals(new BigDecimal("-77.0428"), location.getLng());
    }

    @Test
    void shouldAllowNullLocation() {
        // GIVEN a ContainerResource with null location
        ContainerResource resource = new ContainerResource();
        resource.setLocation(null);

        // THEN location should be null
        assertNull(resource.getLocation());
    }

    @Test
    void shouldAllowNullConnected() {
        // GIVEN a ContainerResource without connected set
        ContainerResource resource = new ContainerResource();

        // THEN connected should be null (nullable for disconnected devices)
        assertNull(resource.getConnected());
    }

    @Test
    void shouldAllowNullFirmware() {
        // GIVEN a ContainerResource without firmware set
        ContainerResource resource = new ContainerResource();

        // THEN firmware should be null
        assertNull(resource.getFirmware());
    }

    @Test
    void shouldAllowNullLocked() {
        // GIVEN a ContainerResource without locked set
        ContainerResource resource = new ContainerResource();

        // THEN locked should be null
        assertNull(resource.getLocked());
    }

    @Test
    void shouldAllowNullCoolingActive() {
        // GIVEN a ContainerResource without coolingActive set
        ContainerResource resource = new ContainerResource();

        // THEN coolingActive should be null
        assertNull(resource.getCoolingActive());
    }
}
