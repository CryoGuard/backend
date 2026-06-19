package com.example.cryoguard.monitoring.presentation.assemblers;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.valueobjects.GpsCoordinates;
import com.example.cryoguard.monitoring.presentation.resources.ContainerResource;
import com.example.cryoguard.monitoring.presentation.resources.CreateContainerResource;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class ContainerResourceAssembler {

    private static final int CONNECTED_THRESHOLD_MINUTES = 5;

    public ContainerResource toResource(Container container) {
        ContainerResource.GpsLocationDTO locationDTO = null;
        if (container.getCurrentLocation() != null) {
            GpsCoordinates loc = container.getCurrentLocation();
            locationDTO = new ContainerResource.GpsLocationDTO(loc.getLatitude(), loc.getLongitude());
        }

        boolean connected = isConnected(container.getLastUpdate(), container.getLastSeenAt());

        boolean apiKeySet = container.getApiKeyHash() != null && !container.getApiKeyHash().isBlank();

        return new ContainerResource(
                container.getId(),                 // id (database ID)
                container.getContainerId(),        // containerId (business ID)
                container.getName(),              // name
                container.getStatus().name().toLowerCase(), // estado
                container.getCurrentTemperature(), // temperature
                container.getCurrentHumidity(),   // humidity
                container.getBatteryLevel(),      // batteryLevel
                container.getCoolingActive(),     // coolingActive
                container.getFirmwareVersion(),   // firmware
                container.getLocked(),             // locked
                connected,                         // connected
                locationDTO,                       // location
                container.getProductType(),        // productType
                container.getDeviceId(),           // deviceId
                container.getLastUpdate(),        // lastTelemetryUpdate
                container.getLastConfigSyncAt(),    // lastConfigSync
                container.getTemperatureMin(),     // temperatureMin
                container.getTemperatureMax(),     // temperatureMax
                container.getHumidityMin(),       // humidityMin
                container.getHumidityMax(),        // humidityMax
                container.getLastSeenAt(),        // lastSeenAt
                apiKeySet                          // apiKeySet
        );
    }

    public Container toEntity(CreateContainerResource resource) {
        Container container = new Container();
        container.setContainerId(resource.getContainerId());
        container.setName(resource.getName());
        container.setDeviceId(resource.getDeviceId());
        return container;
    }

    /**
     * Determines if a container is "connected" based on last heartbeat.
     * Uses lastSeenAt (when available) with 2-minute threshold.
     * Falls back to lastUpdate (5-minute threshold) for legacy containers without lastSeenAt.
     */
    public boolean isConnected(LocalDateTime lastUpdate, LocalDateTime lastSeenAt) {
        if (lastSeenAt != null) {
            long minutesSinceSeen = Duration.between(lastSeenAt, LocalDateTime.now()).toMinutes();
            return minutesSinceSeen < 2;
        }
        if (lastUpdate == null) {
            return false;
        }
        long minutesSinceUpdate = Duration.between(lastUpdate, LocalDateTime.now()).toMinutes();
        return minutesSinceUpdate < CONNECTED_THRESHOLD_MINUTES;
    }
}
