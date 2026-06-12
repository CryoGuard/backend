package com.example.cryoguard.monitoring.presentation.resources;

import com.example.cryoguard.monitoring.presentation.resources.DeviceResource;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DeviceResource record for /devices endpoint.
 * T3.8 - Create DeviceResource record for /devices endpoint
 */
class DeviceResourceTest {

    @Test
    void shouldHaveAllDeviceFields() {
        // GIVEN a DeviceResource with all fields
        LocalDateTime lastSync = LocalDateTime.now();
        DeviceResource.Location location = new DeviceResource.Location(
                new BigDecimal("-12.0464"), new BigDecimal("-77.0428"));
        List<DeviceResource.AlertInfo> alerts = List.of(
                new DeviceResource.AlertInfo("ALT-001", "critica", "Temperatura alta"),
                new DeviceResource.AlertInfo("ALT-002", "advertencia", "Bateria baja")
        );

        DeviceResource resource = new DeviceResource(
                "CG-001",           // id
                "V-2024-0156",      // tripCode
                new BigDecimal("2.5"),  // temperature
                new BigDecimal("65"),   // humidity
85,                 // battery
                true,               // cooling
                location,           // location
                true,               // online
                false,              // locked
                "normal",           // status
                new BigDecimal("-12.0464"), // latitude
                new BigDecimal("-77.0428"), // longitude
                alerts,             // activeAlerts
                lastSync,           // lastSync
                false // temperatureWarning
        );

        // THEN all fields should be correctly set
        assertEquals("CG-001", resource.id());
        assertEquals("V-2024-0156", resource.tripCode());
        assertEquals(new BigDecimal("2.5"), resource.temperature());
        assertEquals(new BigDecimal("65"), resource.humidity());
        assertEquals(85, resource.battery());
        assertTrue(resource.cooling());
        assertNotNull(resource.location());
        assertEquals(new BigDecimal("-12.0464"), resource.location().lat());
        assertEquals(new BigDecimal("-77.0428"), resource.location().lng());
        assertTrue(resource.online());
        assertFalse(resource.locked());
        assertEquals("normal", resource.status());
        assertEquals(new BigDecimal("-12.0464"), resource.latitude());
        assertEquals(new BigDecimal("-77.0428"), resource.longitude());
        assertEquals(2, resource.activeAlerts().size());
        assertEquals(lastSync, resource.lastSync());
        assertFalse(resource.temperatureWarning());
    }

    @Test
    void shouldHaveLocationSubRecord() {
        // GIVEN a Location
        DeviceResource.Location location = new DeviceResource.Location(
                new BigDecimal("-12.0464"), new BigDecimal("-77.0428"));

        // THEN it should have lat and lng
        assertEquals(new BigDecimal("-12.0464"), location.lat());
        assertEquals(new BigDecimal("-77.0428"), location.lng());
    }

    @Test
    void shouldHaveAlertInfoSubRecord() {
        // GIVEN an AlertInfo
        DeviceResource.AlertInfo alert = new DeviceResource.AlertInfo("ALT-001", "critica", "Temperatura alta");

        // THEN it should have id, severity, message
        assertEquals("ALT-001", alert.id());
        assertEquals("critica", alert.severity());
        assertEquals("Temperatura alta", alert.message());
    }

    @Test
    void shouldAllowNullTripCode() {
        // GIVEN a DeviceResource without trip (no active route)
        DeviceResource resource = new DeviceResource(
                "CG-001", null, null, null, null, null, null, false, null, "offline", null, null, List.of(), null, false
        );

        // THEN tripCode should be null
        assertNull(resource.tripCode());
    }

    @Test
    void shouldAllowEmptyAlerts() {
        // GIVEN a DeviceResource with no active alerts
        DeviceResource resource = new DeviceResource(
                "CG-001", null, null, null, null, null, null, false, null, "normal", null, null, List.of(), null, false
        );

        // THEN activeAlerts should be empty
        assertTrue(resource.activeAlerts().isEmpty());
    }

    @Test
    void shouldAllowNullTemperatureWarning() {
        // GIVEN a DeviceResource without temperatureWarning set
        DeviceResource resource = new DeviceResource(
                "CG-001", null, null, null, null, null, null, false, null, "normal", null, null, List.of(), null, null
        );

        // THEN temperatureWarning should be null
        assertNull(resource.temperatureWarning());
    }

    @Test
    void shouldReportOfflineWhenNotOnline() {
        // GIVEN a DeviceResource with online=false
        DeviceResource resource = new DeviceResource(
                "CG-001", null, null, null, null, null, null, false, null, "offline", null, null, List.of(), null, false
        );

        // THEN status should be "offline"
        assertEquals("offline", resource.status());
    }

    @Test
    void shouldStoreStatusAndAlerts() {
        // GIVEN a DeviceResource with online=true and has alerts
        List<DeviceResource.AlertInfo> alerts = List.of(
                new DeviceResource.AlertInfo("ALT-001", "advertencia", "Bateria baja")
        );
        DeviceResource resource = new DeviceResource(
                "CG-001", null, null, null, null, null, null, true, null, "warning", null, null, alerts, null, false
        );

        // THEN status and alerts should be stored as provided
        assertEquals("warning", resource.status());
        assertEquals(1, resource.activeAlerts().size());
        assertEquals("advertencia", resource.activeAlerts().get(0).severity());
    }
}
