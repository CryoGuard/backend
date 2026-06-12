package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.evaluation.application.AlertQueryService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.infrastructure.persistence.TelemetryRepository;
import com.example.cryoguard.monitoring.interfaces.acl.RouteContextFacade;
import com.example.cryoguard.monitoring.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.monitoring.presentation.resources.DeviceResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * DeviceAggregationService - aggregates container, telemetry, route, and alert data.
 * <p>
 * Produces a unified real-time device view for the Vue frontend /devices endpoint.
 * Joins container data with latest telemetry, route info, and computes derived fields
 * like online status and temperature warnings.
 * </p>
 *
 * T3.13 - Wired to evaluation BC's AlertQueryService for activeAlerts population.
 */
@Service
public class DeviceAggregationService {

    private static final int CONNECTED_THRESHOLD_MINUTES = 5;

    private final ContainerRepository containerRepository;
    private final TelemetryRepository telemetryRepository;
    private final RouteContextFacade routeContextFacade;
    private final AlertQueryService alertQueryService;

    public DeviceAggregationService(ContainerRepository containerRepository,
                                     TelemetryRepository telemetryRepository,
                                     RouteContextFacade routeContextFacade,
                                     @Autowired(required = false) AlertQueryService alertQueryService) {
        this.containerRepository = containerRepository;
        this.telemetryRepository = telemetryRepository;
        this.routeContextFacade = routeContextFacade;
        this.alertQueryService = alertQueryService;
    }

    /**
     * Returns all devices with aggregated real-time data.
     */
    public List<DeviceResource> getAllDevices() {
        List<Container> containers = containerRepository.findAll();
        return containers.stream()
                .map(this::aggregateDevice)
                .toList();
    }

    private DeviceResource aggregateDevice(Container container) {
        String containerCode = container.getContainerId();
        LocalDateTime lastUpdate = container.getLastUpdate();
        boolean online = isConnected(lastUpdate);

        // Get latest telemetry
        List<TelemetryReading> telemetryHistory = telemetryRepository.findByContainerIdOrderByTimestampDesc(container.getId());
        TelemetryReading latestTelemetry = telemetryHistory.isEmpty() ? null : telemetryHistory.get(0);

        // Get route info
        RouteInfoDto routeInfo = routeContextFacade.getInfoByContainerCode(containerCode);

        // Compute location from route or telemetry
        DeviceResource.Location location = computeLocation(routeInfo, latestTelemetry);

        // Compute latitude/longitude
        BigDecimal latitude = computeLatitude(routeInfo, latestTelemetry);
        BigDecimal longitude = computeLongitude(routeInfo, latestTelemetry);

        // Compute status
        String status = computeStatus(online, latestTelemetry);

        // Compute temperature warning
        Boolean temperatureWarning = computeTemperatureWarning(latestTelemetry);

        // Get active alerts from evaluation BC (optional - fallback to empty if not available)
        List<DeviceResource.AlertInfo> activeAlerts = getActiveAlerts(container.getId());

        return new DeviceResource(
                containerCode,                                    // id
                routeInfo != null ? routeInfo.tripCode() : null,  // tripCode
                latestTelemetry != null ? latestTelemetry.getTemperature() : null, // temperature
                latestTelemetry != null ? latestTelemetry.getHumidity() : null,       // humidity
                container.getBatteryLevel(),                      // battery
                latestTelemetry != null ? latestTelemetry.getCoolingActive() : null, // cooling
                location,                                         // location
                online,                                           // online
                container.getLocked(),                            // locked
                status,                                           // status
                latitude,                                         // latitude
                longitude,                                        // longitude
                activeAlerts,                                     // activeAlerts (wired to evaluation BC)
                lastUpdate,                                       // lastSync
                temperatureWarning                                // temperatureWarning
        );
    }

    /**
     * Get active alerts for a container from evaluation BC.
     * Falls back to empty list if AlertQueryService is not available.
     */
    private List<DeviceResource.AlertInfo> getActiveAlerts(Long containerId) {
        if (alertQueryService == null) {
            return List.of();
        }
        try {
            List<AlertQueryService.AlertSummaryDto> alerts =
                alertQueryService.getActiveAlertsByContainerIds(List.of(containerId));
            return alerts.stream()
                .map(a -> new DeviceResource.AlertInfo(
                    a.id(),
                    a.severity(),
                    a.message()
                ))
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to empty if evaluation BC is not available
            return List.of();
        }
    }

    private boolean isConnected(LocalDateTime lastUpdate) {
        if (lastUpdate == null) {
            return false;
        }
        long minutesSinceUpdate = Duration.between(lastUpdate, LocalDateTime.now()).toMinutes();
        return minutesSinceUpdate < CONNECTED_THRESHOLD_MINUTES;
    }

    private DeviceResource.Location computeLocation(RouteInfoDto routeInfo, TelemetryReading telemetry) {
        if (routeInfo != null && routeInfo.latitude() != null && routeInfo.longitude() != null) {
            return new DeviceResource.Location(
                    BigDecimal.valueOf(routeInfo.latitude()),
                    BigDecimal.valueOf(routeInfo.longitude())
            );
        }
        if (telemetry != null && telemetry.getLatitude() != null && telemetry.getLongitude() != null) {
            return new DeviceResource.Location(telemetry.getLatitude(), telemetry.getLongitude());
        }
        return null;
    }

    private BigDecimal computeLatitude(RouteInfoDto routeInfo, TelemetryReading telemetry) {
        if (routeInfo != null && routeInfo.latitude() != null) {
            return BigDecimal.valueOf(routeInfo.latitude());
        }
        if (telemetry != null && telemetry.getLatitude() != null) {
            return telemetry.getLatitude();
        }
        return null;
    }

    private BigDecimal computeLongitude(RouteInfoDto routeInfo, TelemetryReading telemetry) {
        if (routeInfo != null && routeInfo.longitude() != null) {
            return BigDecimal.valueOf(routeInfo.longitude());
        }
        if (telemetry != null && telemetry.getLongitude() != null) {
            return telemetry.getLongitude();
        }
        return null;
    }

    private String computeStatus(boolean online, TelemetryReading telemetry) {
        if (!online) {
            return "offline";
        }
        // If online but has temperature warning, status is warning
        if (telemetry != null && Boolean.TRUE.equals(computeTemperatureWarning(telemetry))) {
            return "warning";
        }
        return "normal";
    }

    private Boolean computeTemperatureWarning(TelemetryReading telemetry) {
        if (telemetry == null || telemetry.getTemperature() == null) {
            return null;
        }
        // Simple threshold: warning if temp > 8°C or < 0°C (vaccine cold chain)
        BigDecimal temp = telemetry.getTemperature();
        return temp.compareTo(new BigDecimal("8")) > 0 || temp.compareTo(new BigDecimal("0")) < 0;
    }
}
