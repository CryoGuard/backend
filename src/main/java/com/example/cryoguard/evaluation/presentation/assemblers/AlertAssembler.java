package com.example.cryoguard.evaluation.presentation.assemblers;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity;
import com.example.cryoguard.evaluation.interfaces.acl.EvaluationContainerContextFacade;
import com.example.cryoguard.evaluation.interfaces.acl.EvaluationRouteContextFacade;
import com.example.cryoguard.evaluation.presentation.resources.AlertIncidentResource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * AlertAssembler - converts Alert entities to AlertIncidentResource DTOs.
 * <p>
 * T2.10 - Updated to: remap severity, derive status, format timestamp,
 * resolve boxId and tripCode via ACLs
 * </p>
 */
@Component
public class AlertAssembler {

    private final EvaluationContainerContextFacade containerContextFacade;
    private final EvaluationRouteContextFacade routeContextFacade;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-PE"));

    public AlertAssembler(
            EvaluationContainerContextFacade containerContextFacade,
            EvaluationRouteContextFacade routeContextFacade) {
        this.containerContextFacade = containerContextFacade;
        this.routeContextFacade = routeContextFacade;
    }

    public AlertIncidentResource toResource(Alert alert) {
        String boxId = containerContextFacade.getCode(alert.getContainerId());
        String tripCode = alert.getTripId() != null
            ? routeContextFacade.getCode(alert.getTripId())
            : "Sin viaje";
        String severity = remapSeverity(alert.getSeverity());
        String status = deriveStatus(alert.getAcknowledged(), alert.getResolved());
        String timestamp = formatTimestamp(alert.getTimestamp());

        return new AlertIncidentResource(
            alert.getAlertId(),
            severity,
            status,
            alert.getMessage(),
            boxId,
            tripCode,
            alert.getTriggerValue(),
            timestamp
        );
    }

    /**
     * Remap internal AlertSeverity to Vue frontend strings.
     * CRITICAL → "critica"
     * WARNING/INFO → "advertencia"
     */
    public String remapSeverity(AlertSeverity severity) {
        return switch (severity) {
            case CRITICAL -> "critica";
            case WARNING, INFO -> "advertencia";
        };
    }

    /**
     * Derive status string from acknowledged and resolved booleans.
     * resolved=true → "confirmada"
     * acknowledged=true and resolved=false → "pendiente"
     * acknowledged=false and resolved=false → "activa"
     */
    String deriveStatus(Boolean acknowledged, Boolean resolved) {
        if (Boolean.TRUE.equals(resolved)) {
            return "confirmada";
        } else if (Boolean.TRUE.equals(acknowledged)) {
            return "pendiente";
        } else {
            return "activa";
        }
    }

    /**
     * Format timestamp as dd/MM/yyyy HH:mm (es-PE locale).
     */
    String formatTimestamp(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "";
        }
        return timestamp.format(TIMESTAMP_FORMATTER);
    }
}
