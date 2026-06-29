package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.evaluation.application.AlertCommandService;
import com.example.cryoguard.evaluation.domain.commands.CreateAlertCommand;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertType;
import com.example.cryoguard.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.example.cryoguard.monitoring.application.ContainerCommandService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.commands.UpdateContainerTelemetryCommand;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.presentation.resources.EdgeLocalAlertResource;
import com.example.cryoguard.monitoring.presentation.resources.EdgeTelemetryResource;
import com.example.cryoguard.monitoring.presentation.assemblers.TelemetryReadingResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/edge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Edge", description = "Edge device telemetry ingestion")
public class EdgeIngestController {

    private final ContainerRepository containerRepository;
    private final ContainerCommandService containerCommandService;
    private final AlertCommandService alertCommandService;
    private final TelemetryReadingResourceAssembler telemetryAssembler;
    private final BCryptHashingService hashingService;

    @ExceptionHandler(EdgeAuthException.class)
    public ResponseEntity<?> handleEdgeAuthException(EdgeAuthException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @PostMapping("/ingest")
    @Operation(summary = "Ingest telemetry from edge device",
               description = "Receives telemetry from edge gateway, forwards to container telemetry endpoint.")
    public ResponseEntity<?> ingestTelemetry(@RequestBody EdgeTelemetryResource resource,
                                             @RequestHeader("X-Edge-Id") String edgeIdHeader,
                                             @RequestHeader("X-Edge-Api-Key") String apiKeyHeader) {
        // Validate edge authentication before any processing
        validateEdgeAuth(resource.getContainerId(), edgeIdHeader, apiKeyHeader);

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

    /**
     * Forward edge-evaluated alerts to cloud.
     *
     * POST /api/v1/edge/alerts
     *
     * Authentication: X-Edge-Id + X-Edge-Api-Key headers (same as /ingest).
     *
     * Example curl:
     * <pre>
     * curl -X POST http://localhost:8080/api/v1/edge/alerts \
     *   -H "Content-Type: application/json" \
     *   -H "X-Edge-Id: CG-001" \
     *   -H "X-Edge-Api-Key: &lt;api-key&gt;" \
     *   -d '[{
     *     "containerId": "CG-001",
     *     "alertType": "TEMPERATURE_HIGH",
     *     "severity": "CRITICAL",
     *     "measuredValue": 12.5,
     *     "thresholdValue": 8.0,
     *     "message": "Temperature exceeded threshold",
     *     "receivedAt": "2026-06-29T10:30:00"
     *   }]'
     * </pre>
     */
    @PostMapping("/alerts")
    @Operation(summary = "Forward edge-evaluated alerts",
               description = "Receives a batch of alerts that the edge evaluated locally against cached thresholds.")
    public ResponseEntity<?> ingestEdgeAlerts(
            @RequestBody List<EdgeLocalAlertResource> alerts,
            @RequestHeader("X-Edge-Id") String edgeIdHeader,
            @RequestHeader("X-Edge-Api-Key") String apiKeyHeader) {

        if (alerts == null || alerts.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "alerts body is required and must not be empty"));
        }

        int created = 0;
        for (EdgeLocalAlertResource r : alerts) {
            // Validate edge auth using the containerId from the first alert as identity
            validateEdgeAuth(r.containerId(), edgeIdHeader, apiKeyHeader);

            // Look up container by business ID
            Container container = containerRepository.findByContainerId(r.containerId())
                    .orElse(null);
            if (container == null) {
                log.warn("Edge alert for unknown container: {}", r.containerId());
                continue;
            }

            try {
                // Embed trigger details in message since CreateAlertCommand doesn't have triggerValue
                String fullMessage = r.message() + " [trigger: measured="
                        + (r.measuredValue() == null ? "null" : String.format("%.1f", r.measuredValue()))
                        + " threshold=" + (r.thresholdValue() == null ? "null" : String.format("%.1f", r.thresholdValue()))
                        + " type=" + r.alertType() + "]";

                CreateAlertCommand command = new CreateAlertCommand(
                        container.getId(),
                        mapAlertSeverity(r.severity()),
                        mapAlertType(r.alertType()),
                        fullMessage,
                        null,  // latitude
                        null   // longitude
                );
                alertCommandService.createAlert(command);
                created++;
            } catch (Exception e) {
                log.error("Failed to ingest edge alert for {}: {}", r.containerId(), e.getMessage());
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("status", "ok", "created", created, "received", alerts.size()));
    }

    private AlertType mapAlertType(String edgeType) {
        // TEMPERATURE_HIGH/LOW -> TEMPERATURE; HUMIDITY_HIGH/LOW -> HUMIDITY; BATTERY_LOW -> BATTERY
        String upper = edgeType.toUpperCase();
        if (upper.startsWith("TEMPERATURE")) return AlertType.TEMPERATURE;
        if (upper.startsWith("HUMIDITY")) return AlertType.HUMIDITY;
        if (upper.startsWith("BATTERY")) return AlertType.BATTERY;
        if (upper.startsWith("DOOR")) return AlertType.DOOR;
        if (upper.startsWith("VIBRATION")) return AlertType.VIBRATION;
        if (upper.startsWith("GEOFENCE")) return AlertType.GEOFENCE;
        throw new IllegalArgumentException("Unknown alert type: " + edgeType);
    }

    private AlertSeverity mapAlertSeverity(String severity) {
        return AlertSeverity.fromValue(severity.toLowerCase());
    }

    private void validateEdgeAuth(String bodyContainerId, String edgeIdHeader, String apiKeyHeader) {
        if (edgeIdHeader == null || edgeIdHeader.isBlank()) {
            throw new EdgeAuthException("X-Edge-Id header missing");
        }
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            throw new EdgeAuthException("X-Edge-Api-Key header missing");
        }
        if (!edgeIdHeader.equals(bodyContainerId)) {
            log.warn("Edge auth header mismatch: edgeId={} body={}", edgeIdHeader, bodyContainerId);
            throw new EdgeAuthException("Edge identity mismatch");
        }
        Container container = containerRepository.findByContainerId(edgeIdHeader)
                .orElseThrow(() -> new EdgeAuthException("Container not found for edgeId"));
        if (container.getApiKeyHash() == null || !hashingService.matches(apiKeyHeader, container.getApiKeyHash())) {
            log.warn("Edge auth failed: edgeId={}", edgeIdHeader);
            throw new EdgeAuthException("Invalid X-Edge-Api-Key");
        }
    }
}
