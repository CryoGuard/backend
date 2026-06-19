package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.example.cryoguard.monitoring.application.ContainerCommandService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.exceptions.ContainerNotFoundException;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.presentation.resources.EdgeHeartbeatResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Edge-facing heartbeat endpoint.
 * Receives periodic heartbeat signals from edge devices to confirm liveness.
 */
@RestController
@RequestMapping("/api/v1/edge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Edge", description = "Edge device heartbeat and configuration")
public class EdgeHeartbeatController {

    private final ContainerRepository containerRepository;
    private final ContainerCommandService containerCommandService;
    private final BCryptHashingService hashingService;

    @ExceptionHandler(EdgeAuthException.class)
    public ResponseEntity<?> handleEdgeAuthException(EdgeAuthException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ContainerNotFoundException.class)
    public ResponseEntity<?> handleContainerNotFoundException(ContainerNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }

    @PostMapping("/heartbeat")
    @Operation(summary = "Edge heartbeat",
               description = "Receives periodic heartbeat from edge device to confirm liveness.")
    public ResponseEntity<?> heartbeat(@RequestBody EdgeHeartbeatResource resource,
                                       @RequestHeader("X-Edge-Id") String edgeIdHeader,
                                       @RequestHeader("X-Edge-Api-Key") String apiKeyHeader) {
        // Use body containerId if provided, otherwise fall back to header
        String containerId = (resource.getContainerId() != null && !resource.getContainerId().isBlank())
                ? resource.getContainerId()
                : edgeIdHeader;

        // Validate edge authentication
        validateEdgeAuth(containerId, edgeIdHeader, apiKeyHeader);

        // Record the heartbeat
        LocalDateTime now = LocalDateTime.now();
        containerCommandService.recordHeartbeat(containerId, now);

        return ResponseEntity.ok(Map.of(
                "status", "ok",
                "timestamp", now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        ));
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