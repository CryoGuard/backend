package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.evaluation.application.AlertQueryService;
import com.example.cryoguard.monitoring.application.DeviceAggregationService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import com.example.cryoguard.monitoring.domain.valueobjects.GpsCoordinates;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.infrastructure.persistence.TelemetryRepository;
import com.example.cryoguard.monitoring.interfaces.acl.RouteContextFacade;
import com.example.cryoguard.monitoring.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.monitoring.presentation.resources.DeviceResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for DeviceAggregationService.
 * T3.9 - Create DeviceAggregationService
 * T3.13 - Updated to include AlertQueryService mock for activeAlerts
 */
@ExtendWith(MockitoExtension.class)
class DeviceAggregationServiceTest {

    @Mock
    private ContainerRepository containerRepository;

    @Mock
    private TelemetryRepository telemetryRepository;

    @Mock
    private RouteContextFacade routeContextFacade;

    @Mock
    private AlertQueryService alertQueryService;

    private DeviceAggregationService service;

    @BeforeEach
    void setUp() {
        service = new DeviceAggregationService(containerRepository, telemetryRepository, routeContextFacade, alertQueryService);
    }

    @Test
    void shouldComputeOnlineWhenLastUpdateWithin5Minutes() {
        // GIVEN a container with lastUpdate 2 minutes ago
        Container container = createContainer(1L, "CG-001", "Contenedor 1");
        container.setLastUpdate(LocalDateTime.now().minusMinutes(2));

        when(containerRepository.findAll()).thenReturn(List.of(container));
        when(telemetryRepository.findByContainerIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        when(routeContextFacade.getInfoByContainerCode("CG-001")).thenReturn(null);

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN device should be online
        assertEquals(1, devices.size());
        assertTrue(devices.get(0).online());
    }

    @Test
    void shouldComputeOfflineWhenLastUpdateMoreThan5MinutesAgo() {
        // GIVEN a container with lastUpdate 10 minutes ago
        Container container = createContainer(1L, "CG-001", "Contenedor 1");
        container.setLastUpdate(LocalDateTime.now().minusMinutes(10));

        when(containerRepository.findAll()).thenReturn(List.of(container));
        when(telemetryRepository.findByContainerIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        when(routeContextFacade.getInfoByContainerCode("CG-001")).thenReturn(null);

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN device should be offline
        assertEquals(1, devices.size());
        assertFalse(devices.get(0).online());
        assertEquals("offline", devices.get(0).status());
    }

    @Test
    void shouldComputeOfflineWhenNoLastUpdate() {
        // GIVEN a container with no lastUpdate
        Container container = createContainer(1L, "CG-001", "Contenedor 1");
        container.setLastUpdate(null);

        when(containerRepository.findAll()).thenReturn(List.of(container));
        when(telemetryRepository.findByContainerIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        when(routeContextFacade.getInfoByContainerCode("CG-001")).thenReturn(null);

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN device should be offline
        assertEquals(1, devices.size());
        assertFalse(devices.get(0).online());
 }

    @Test
    void shouldIncludeTripCodeFromRoute() {
        // GIVEN a container on an active route
        Container container = createContainer(1L, "CG-001", "Contenedor 1");
        container.setLastUpdate(LocalDateTime.now().minusMinutes(1));

        RouteInfoDto routeInfo = new RouteInfoDto("V-2024-0156", -12.0464, -77.0428);
        when(containerRepository.findAll()).thenReturn(List.of(container));
        when(telemetryRepository.findByContainerIdOrderByTimestampDesc(1L)).thenReturn(List.of());
        when(routeContextFacade.getInfoByContainerCode("CG-001")).thenReturn(routeInfo);

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN device should have tripCode
        assertEquals(1, devices.size());
        assertEquals("V-2024-0156", devices.get(0).tripCode());
    }

    @Test
    void shouldUseLatestTelemetryForTemperatureAndHumidity() {
        // GIVEN a container with latest telemetry
        Container container = createContainer(1L, "CG-001", "Contenedor 1");
        container.setLastUpdate(LocalDateTime.now().minusMinutes(1));

        TelemetryReading latestTelemetry = new TelemetryReading();
        latestTelemetry.setContainerId(1L);
        latestTelemetry.setTemperature(new BigDecimal("2.5"));
        latestTelemetry.setHumidity(new BigDecimal("65"));
        latestTelemetry.setCoolingActive(true);
        latestTelemetry.setTimestamp(LocalDateTime.now().minusMinutes(1));

        when(containerRepository.findAll()).thenReturn(List.of(container));
        when(telemetryRepository.findByContainerIdOrderByTimestampDesc(1L)).thenReturn(List.of(latestTelemetry));
        when(routeContextFacade.getInfoByContainerCode("CG-001")).thenReturn(null);

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN device should have temperature and humidity from latest telemetry
        assertEquals(1, devices.size());
        assertEquals(new BigDecimal("2.5"), devices.get(0).temperature());
        assertEquals(new BigDecimal("65"), devices.get(0).humidity());
        assertTrue(devices.get(0).cooling());
    }

    @Test
    void shouldReturnEmptyListWhenNoContainers() {
        // GIVEN no containers
        when(containerRepository.findAll()).thenReturn(List.of());

        // WHEN calling getAllDevices
        List<DeviceResource> devices = service.getAllDevices();

        // THEN should return empty list
        assertTrue(devices.isEmpty());
    }

    private Container createContainer(Long id, String containerId, String name) {
        Container container = new Container();
        container.setId(id);
        container.setContainerId(containerId);
        container.setName(name);
        container.setStatus(ContainerStatus.ACTIVE);
        container.setBatteryLevel(85);
        container.setLocked(false);
        container.setCoolingActive(false);
        return container;
    }
}
