package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.monitoring.application.ContainerCommandService;
import com.example.cryoguard.monitoring.domain.commands.UpdateContainerTelemetryCommand;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.domain.valueobjects.GpsCoordinates;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.presentation.resources.EdgeTelemetryResource;
import com.example.cryoguard.monitoring.presentation.resources.TelemetryReadingResource;
import com.example.cryoguard.monitoring.presentation.assemblers.TelemetryReadingResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/edge")
@RequiredArgsConstructor
@Tag(name = "Edge", description = "Edge device telemetry ingestion")
public class EdgeIngestController {

    private final ContainerRepository containerRepository;
    private final ContainerCommandService containerCommandService;
    private final TelemetryReadingResourceAssembler telemetryAssembler;

    @PostMapping("/ingest")
    @Operation(summary = "Ingest telemetry from edge device",
               description = "Receives telemetry from edge gateway, forwards to container telemetry endpoint.")
    public ResponseEntity<?> ingestTelemetry(@RequestBody EdgeTelemetryResource resource) {
        // Look up container by business containerId
        return containerRepository.findByContainerId(resource.getContainerId())
                .map(container -> {
                    // Create command with numeric container ID
                    UpdateContainerTelemetryCommand command = new UpdateContainerTelemetryCommand(
                            container.getId(),
                            resource.getTemperature(),
                            resource.getHumidity(),
                            null, // vibration
                            null, // doorOpen
                            resource.getLatitude(),
                            resource.getLongitude(),
                            resource.getBatteryLevel(),
                            resource.getTimestamp() != null ? resource.getTimestamp() : LocalDateTime.now(),
                            resource.getCoolingActive()
                    );
                    TelemetryReading reading = containerCommandService.recordTelemetry(command);
                    return ResponseEntity.status(HttpStatus.CREATED)
                            .body(Map.of("status", "forwarded", "containerId", resource.getContainerId()));
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Container not found", "containerId", resource.getContainerId())));
    }
}
