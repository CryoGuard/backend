package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.exceptions.ContainerNotFoundException;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.presentation.resources.EdgeConfigResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Edge-facing configuration endpoint.
 * Returns static configuration parameters for a container to edge devices.
 */
@RestController
@RequestMapping("/api/v1/edge")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Edge", description = "Edge device heartbeat and configuration")
public class EdgeConfigController {

    private final ContainerRepository containerRepository;
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

    @GetMapping("/config")
    @Operation(summary = "Get edge configuration",
               description = "Returns static configuration parameters for a container.")
    public ResponseEntity<?> getConfig(@RequestParam("edgeId") String edgeId,
                                       @RequestHeader("X-Edge-Id") String edgeIdHeader,
                                       @RequestHeader("X-Edge-Api-Key") String apiKeyHeader) {
        // Validate edge authentication using the edgeId query param
        validateEdgeAuth(edgeId, edgeIdHeader, apiKeyHeader);

        // Look up container by edgeId (containerId)
        Container container = containerRepository.findByContainerId(edgeId)
                .orElseThrow(() -> new ContainerNotFoundException("Container not found: " + edgeId));

        EdgeConfigResource config = new EdgeConfigResource(
                container.getContainerId(),
                container.getName(),
                container.getTemperatureMin(),
                container.getTemperatureMax(),
                container.getHumidityMin(),
                container.getHumidityMax(),
                container.getFirmwareVersion()
        );

        return ResponseEntity.ok(config);
    }

    private void validateEdgeAuth(String queryEdgeId, String edgeIdHeader, String apiKeyHeader) {
        if (edgeIdHeader == null || edgeIdHeader.isBlank()) {
            throw new EdgeAuthException("X-Edge-Id header missing");
        }
        if (apiKeyHeader == null || apiKeyHeader.isBlank()) {
            throw new EdgeAuthException("X-Edge-Api-Key header missing");
        }
        // The X-Edge-Id header must match the edgeId query param
        if (!edgeIdHeader.equals(queryEdgeId)) {
            log.warn("Edge auth header mismatch: edgeId={} queryEdgeId={}", edgeIdHeader, queryEdgeId);
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