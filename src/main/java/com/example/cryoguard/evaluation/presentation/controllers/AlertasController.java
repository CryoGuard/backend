package com.example.cryoguard.evaluation.presentation.controllers;

import com.example.cryoguard.evaluation.application.AlertCommandService;
import com.example.cryoguard.evaluation.application.AlertCommandServiceImpl;
import com.example.cryoguard.evaluation.application.AlertQueryService;
import com.example.cryoguard.evaluation.application.IaMetricsService;
import com.example.cryoguard.evaluation.domain.commands.AcknowledgeAlertCommand;
import com.example.cryoguard.evaluation.domain.commands.ResolveAlertCommand;
import com.example.cryoguard.evaluation.domain.entities.Alert;
import com.example.cryoguard.evaluation.presentation.assemblers.AlertAssembler;
import com.example.cryoguard.evaluation.presentation.resources.AlertIncidentResource;
import com.example.cryoguard.evaluation.presentation.resources.AlertSummaryResource;
import com.example.cryoguard.evaluation.presentation.resources.RecommendationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * AlertasController - alert management endpoints for Vue frontend.
 * <p>
 * T2.11 - Renamed from AlertsController, path changed to /api/v1/alertas.
 * Updated to use AlertIncidentResource for all responses.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/alertas")
@RequiredArgsConstructor
@Tag(name = "Alertas", description = "Alert and notification management operations")
public class AlertasController {

    private final AlertCommandService alertCommandService;
    private final AlertQueryService alertQueryService;
    private final AlertAssembler alertAssembler;
    private final IaMetricsService iaMetricsService;

    @GetMapping
    @Operation(summary = "Get all alertas", description = "Retrieves all alerts with optional filtering by status, severity, or container ID. When 'since' is provided, returns all alerts after that timestamp (no pagination, ordered ASC).")
    public ResponseEntity<?> getAllAlertas(
            @RequestParam(required = false) String severity,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long containerId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String since,
            @RequestParam(required = false) String until,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // B1: Polling endpoint - since/until returns all matching alerts (no pagination)
        if (since != null) {
            LocalDateTime sinceDt;
            try {
                sinceDt = LocalDateTime.parse(since);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid 'since' timestamp format. Expected ISO-8601 (e.g., 2026-06-29T10:00:00)");
            }

            LocalDateTime untilDt = null;
            if (until != null) {
                try {
                    untilDt = LocalDateTime.parse(until);
                } catch (DateTimeParseException e) {
                    throw new IllegalArgumentException("Invalid 'until' timestamp format. Expected ISO-8601 (e.g., 2026-06-29T10:00:00)");
                }
            }

            List<Alert> alerts;
            if (untilDt != null) {
                alerts = alertQueryService.getAlertsBetween(sinceDt, untilDt, severity, status);
            } else {
                alerts = alertQueryService.getAlertsSince(sinceDt, severity, status);
            }

            List<AlertIncidentResource> resources = alerts.stream()
                .map(alertAssembler::toResource)
                .toList();
            return ResponseEntity.ok(resources);
        }

        // Handle US06: dashboard recent alerts with limit and sort
        if (limit != null && sort != null && "reciente".equals(sort)) {
            List<Alert> recentAlerts = alertQueryService.getAlertsByStatus(false);
            List<Alert> limitedAlerts = recentAlerts.stream()
                .sorted((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()))
                .limit(limit)
                .toList();

            List<AlertSummaryResource> summaryResources = limitedAlerts.stream()
                .map(this::toSummaryResource)
                .toList();

            // Return as Page for consistency
            Page<AlertSummaryResource> summaryPage = new org.springframework.data.domain.PageImpl<>(
                summaryResources);
            @SuppressWarnings("unchecked")
            ResponseEntity<Page<AlertIncidentResource>> castResponse =
                (ResponseEntity<Page<AlertIncidentResource>>) (ResponseEntity<?>) ResponseEntity.ok(summaryPage);
            return castResponse;
        }

        // Standard paginated query
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<Alert> alerts;

        if (severity != null || status != null) {
            alerts = alertQueryService.getAlertsByFilters(severity, status, pageable);
        } else if (containerId != null) {
            alerts = alertQueryService.getAlertsPage(pageable);
        } else {
            alerts = alertQueryService.getAlertsPage(pageable);
        }

        Page<AlertIncidentResource> resourcePage = alerts.map(alertAssembler::toResource);
        return ResponseEntity.ok(resourcePage);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get alerta by ID", description = "Retrieves a specific alert by its unique identifier.")
    public ResponseEntity<AlertIncidentResource> getAlertaById(@PathVariable Long id) {
        return alertQueryService.getAlertById(id)
                .map(alert -> ResponseEntity.ok(alertAssembler.toResource(alert)))
                .orElse(ResponseEntity.notFound().build());
    }

    // B2: Active alerts count for dashboard badge
    @GetMapping("/active/count")
    @Operation(summary = "Count active alertas", description = "Returns the count of active (unresolved) alerts for dashboard badge counters.")
    public ResponseEntity<ActiveCountResource> countActiveAlertas() {
        long count = alertQueryService.countActiveAlerts();
        return ResponseEntity.ok(new ActiveCountResource(count));
    }

    /**
     * DTO for active alert count response.
     */
    public record ActiveCountResource(long count) {}

    @PutMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge alerta", description = "Marks an alert as acknowledged by a user.")
    public ResponseEntity<AlertIncidentResource> acknowledgeAlerta(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            AcknowledgeAlertCommand command = new AcknowledgeAlertCommand(id, userId);
            Alert alert = alertCommandService.acknowledgeAlert(command);
            return ResponseEntity.ok(alertAssembler.toResource(alert));
        } catch (AlertCommandServiceImpl.AlertAlreadyAcknowledgedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}/resolve")
    @Operation(summary = "Resolve alerta", description = "Marks an alert as resolved by a user.")
    public ResponseEntity<AlertIncidentResource> resolveAlerta(
            @PathVariable Long id,
            @RequestParam Long userId) {
        try {
            ResolveAlertCommand command = new ResolveAlertCommand(id, userId);
            Alert alert = alertCommandService.resolveAlert(command);
            return ResponseEntity.ok(alertAssembler.toResource(alert));
        } catch (AlertCommandServiceImpl.AlertAlreadyResolvedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PutMapping("/{id}/escalate")
    @Operation(summary = "Escalate alerta", description = "Escalates an alert to a higher severity level.")
    public ResponseEntity<AlertIncidentResource> escalateAlerta(@PathVariable Long id) {
        try {
            Alert alert = alertCommandService.escalateAlert(id);
            return ResponseEntity.ok(alertAssembler.toResource(alert));
        } catch (AlertCommandServiceImpl.AlertAlreadyCriticalException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (AlertCommandServiceImpl.AlertResolvedException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    /**
     * Convert Alert to AlertSummaryResource for dashboard card.
     */
    private AlertSummaryResource toSummaryResource(Alert alert) {
        String severity = alertAssembler.remapSeverity(alert.getSeverity());
        String tipo = alert.getAlertType() != null ? alert.getAlertType().name() : "UNKNOWN";
        String caja = alertAssembler.toResource(alert).boxId();
        String tiempo = computeRelativeTime(alert.getTimestamp());
        return new AlertSummaryResource(alert.getAlertId(), tipo, caja, tiempo, severity);
    }

    /**
     * Compute relative time string like "Hace X minutos".
     * SYNTHETIC: academic project, simplified relative time.
     */
    private String computeRelativeTime(java.time.LocalDateTime timestamp) {
        if (timestamp == null) return "Sin datos";
        java.time.Duration diff = java.time.Duration.between(timestamp, java.time.LocalDateTime.now());
        long minutes = diff.toMinutes();
        if (minutes < 60) {
            return "Hace " + minutes + " minutos";
        } else if (minutes < 1440) {
            long hours = minutes / 60;
            return "Hace " + hours + " horas";
        } else {
            long days = minutes / 1440;
            return "Hace " + days + " días";
        }
    }
}