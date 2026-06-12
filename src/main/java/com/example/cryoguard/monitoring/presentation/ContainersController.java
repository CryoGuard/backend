package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.monitoring.application.ContainerCommandService;
import com.example.cryoguard.monitoring.application.ContainerQueryService;
import com.example.cryoguard.monitoring.application.TelemetryQueryService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.commands.CreateContainerCommand;
import com.example.cryoguard.monitoring.domain.commands.SyncContainerCommand;
import com.example.cryoguard.monitoring.domain.commands.UpdateContainerTelemetryCommand;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import com.example.cryoguard.monitoring.presentation.assemblers.ContainerResourceAssembler;
import com.example.cryoguard.monitoring.presentation.assemblers.TelemetryReadingResourceAssembler;
import com.example.cryoguard.monitoring.presentation.resources.ContainerResource;
import com.example.cryoguard.monitoring.presentation.resources.CreateContainerResource;
import com.example.cryoguard.monitoring.presentation.resources.SyncContainerResource;
import com.example.cryoguard.monitoring.presentation.resources.TelemetryInputResource;
import com.example.cryoguard.monitoring.presentation.resources.TelemetryReadingResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/containers")
@RequiredArgsConstructor
@Tag(name = "Containers", description = "Container monitoring operations")
public class ContainersController {

    private final ContainerCommandService containerCommandService;
    private final ContainerQueryService containerQueryService;
    private final TelemetryQueryService telemetryQueryService;
    private final ContainerResourceAssembler containerAssembler;
    private final TelemetryReadingResourceAssembler telemetryAssembler;

    @PostMapping
    @Operation(summary = "Create container", description = "Creates a new container for monitoring.")
    public ResponseEntity<ContainerResource> createContainer(@RequestBody CreateContainerResource resource) {
        CreateContainerCommand command = new CreateContainerCommand(
                resource.getContainerId(),
                resource.getName(),
                resource.getDeviceId(),
                resource.getProductType(),
                resource.getOperatorId(),
                resource.getTemperatureMin(),
                resource.getTemperatureMax(),
                resource.getHumidityMin(),
                resource.getHumidityMax(),
                resource.getFirmwareVersion(),
                resource.getLocked()
        );
        Container container = containerCommandService.createContainer(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(containerAssembler.toResource(container));
    }

    @GetMapping
    @Operation(summary = "Get all containers", description = "Returns a list of all monitored containers with optional filtering and pagination.")
    public ResponseEntity<Page<ContainerResource>> getAllContainers(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String productType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ContainerStatus containerStatus = null;
        if (status != null) {
            try {
                containerStatus = ContainerStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Invalid status, ignore filter
            }
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Container> containers = containerQueryService.getAllContainers(containerStatus, productType, pageable);
        Page<ContainerResource> resources = containers.map(containerAssembler::toResource);
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get container by ID", description = "Retrieves a specific container by its unique identifier.")
    public ResponseEntity<ContainerResource> getContainerById(@PathVariable Long id) {
        Container container = containerQueryService.getContainerById(id);
        return ResponseEntity.ok(containerAssembler.toResource(container));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update container", description = "Updates an existing container.")
    public ResponseEntity<ContainerResource> updateContainer(@PathVariable Long id, @RequestBody CreateContainerResource resource) {
        CreateContainerCommand command = new CreateContainerCommand(
                resource.getContainerId(),
                resource.getName(),
                resource.getDeviceId(),
                resource.getProductType(),
                resource.getOperatorId(),
                resource.getTemperatureMin(),
                resource.getTemperatureMax(),
                resource.getHumidityMin(),
                resource.getHumidityMax(),
                resource.getFirmwareVersion(),
                resource.getLocked()
        );
        Container container = containerCommandService.updateContainer(id, command);
        return ResponseEntity.ok(containerAssembler.toResource(container));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete container", description = "Removes a container from the monitoring system.")
    public ResponseEntity<Void> deleteContainer(@PathVariable Long id) {
        containerCommandService.deleteContainer(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/telemetry")
    @Operation(summary = "Record telemetry", description = "Records a new telemetry reading for a container.")
    public ResponseEntity<TelemetryReadingResource> recordTelemetry(@PathVariable Long id, @RequestBody TelemetryInputResource resource) {
        UpdateContainerTelemetryCommand command = new UpdateContainerTelemetryCommand(
                id,
                resource.getTemperature(),
                resource.getHumidity(),
                resource.getVibration(),
                resource.getDoorOpen(),
                resource.getLatitude(),
                resource.getLongitude(),
                resource.getBatteryLevel(),
                resource.getTimestamp(),
                resource.getCoolingActive()
        );
        TelemetryReading reading = containerCommandService.recordTelemetry(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(telemetryAssembler.toResource(reading));
    }

    @GetMapping("/{id}/telemetry")
    @Operation(summary = "Get container telemetry", description = "Retrieves telemetry readings for a specific container. Use time range for history.")
    public ResponseEntity<List<TelemetryReadingResource>> getTelemetry(
            @PathVariable Long id,
            @RequestParam(required = false) LocalDateTime from,
            @RequestParam(required = false) LocalDateTime to) {

        List<TelemetryReading> readings;
        if (from != null && to != null) {
            readings = telemetryQueryService.getTelemetryByContainerId(id, from, to);
        } else {
            readings = telemetryQueryService.getTelemetryByContainerId(id);
        }

        List<TelemetryReadingResource> resources = readings.stream()
                .map(telemetryAssembler::toResource)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PostMapping("/{id}/sync")
    @Operation(summary = "Sync container location", description = "Synchronizes a container's current GPS location.")
    public ResponseEntity<ContainerResource> syncContainer(@PathVariable Long id, @RequestBody SyncContainerResource resource) {
        SyncContainerCommand command = new SyncContainerCommand(id, resource.getLatitude(), resource.getLongitude());
        Container container = containerCommandService.syncContainer(command);
        return ResponseEntity.ok(containerAssembler.toResource(container));
    }
}
