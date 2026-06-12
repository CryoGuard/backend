package com.example.cryoguard.evaluation.presentation.assemblers;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertType;
import com.example.cryoguard.evaluation.interfaces.acl.EvaluationContainerContextFacade;
import com.example.cryoguard.evaluation.interfaces.acl.EvaluationRouteContextFacade;
import com.example.cryoguard.evaluation.presentation.resources.AlertIncidentResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for AlertAssembler - updated to produce AlertIncidentResource.
 * T2.10 - Update AlertAssembler to: remap severity, derive status, format timestamp,
 *         resolve boxId and tripCode via ACLs
 */
@ExtendWith(MockitoExtension.class)
class AlertAssemblerTest {

    @Mock
    private EvaluationContainerContextFacade containerContextFacade;

    @Mock
    private EvaluationRouteContextFacade routeContextFacade;

    private AlertAssembler assembler;

    @BeforeEach
    void setUp() {
        assembler = new AlertAssembler(containerContextFacade, routeContextFacade);
    }

    @Test
    @DisplayName("should remap CRITICAL severity to critica")
    void shouldRemapCriticalSeverityToCritica() {
        // GIVEN an alert with CRITICAL severity
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN severity should be "critica"
        assertEquals("critica", resource.severity());
    }

    @Test
    @DisplayName("should remap WARNING severity to advertencia")
    void shouldRemapWarningSeverityToAdvertencia() {
        // GIVEN an alert with WARNING severity
        Alert alert = createAlert(AlertSeverity.WARNING, false, false);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN severity should be "advertencia"
        assertEquals("advertencia", resource.severity());
    }

    @Test
    @DisplayName("should remap INFO severity to advertencia")
    void shouldRemapInfoSeverityToAdvertencia() {
        // GIVEN an alert with INFO severity
        Alert alert = createAlert(AlertSeverity.INFO, false, false);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN severity should be "advertencia"
        assertEquals("advertencia", resource.severity());
    }

    @Test
    @DisplayName("should derive ACTIVA status when not acknowledged and not resolved")
    void shouldDeriveActivaStatusWhenNotAcknowledgedAndNotResolved() {
        // GIVEN an alert with acknowledged=false, resolved=false
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN status should be "activa"
        assertEquals("activa", resource.status());
    }

    @Test
    @DisplayName("should derive PENDIENTE status when acknowledged and not resolved")
    void shouldDerivePendienteStatusWhenAcknowledgedAndNotResolved() {
        // GIVEN an alert with acknowledged=true, resolved=false
        Alert alert = createAlert(AlertSeverity.CRITICAL, true, false);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN status should be "pendiente"
        assertEquals("pendiente", resource.status());
    }

    @Test
    @DisplayName("should derive CONFIRMADA status when resolved")
    void shouldDeriveConfirmadaStatusWhenResolved() {
        // GIVEN an alert with acknowledged=true, resolved=true
        Alert alert = createAlert(AlertSeverity.CRITICAL, true, true);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN status should be "confirmada"
        assertEquals("confirmada", resource.status());
    }

    @Test
    @DisplayName("should format timestamp as dd/MM/yyyy HH:mm")
    void shouldFormatTimestampAsDdMMyyyyHHmm() {
        // GIVEN an alert with timestamp 2026-06-01 14:35:00
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setTimestamp(LocalDateTime.of(2026, 6, 1, 14, 35, 0));

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN timestamp should be "01/06/2026 14:35"
        assertEquals("01/06/2026 14:35", resource.timestamp());
    }

    @Test
    @DisplayName("should use alertId as id")
    void shouldUseAlertIdAsId() {
        // GIVEN an alert with alertId "ALT-001"
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setAlertId("ALT-042");

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN id should be "ALT-042"
        assertEquals("ALT-042", resource.id());
    }

    @Test
    @DisplayName("should resolve boxId via ContainerContextFacade")
    void shouldResolveBoxIdViaContainerContextFacade() {
        // GIVEN alert with containerId=5 and ContainerContextFacade returns "CG-047"
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setContainerId(5L);
        when(containerContextFacade.getCode(5L)).thenReturn("CG-047");

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN boxId should be "CG-047"
        assertEquals("CG-047", resource.boxId());
    }

    @Test
    @DisplayName("should resolve tripCode via RouteContextFacade")
    void shouldResolveTripCodeViaRouteContextFacade() {
        // GIVEN alert with tripId=10 and RouteContextFacade returns "V-2024-0157"
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setTripId(10L);
        when(routeContextFacade.getCode(10L)).thenReturn("V-2024-0157");

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN tripCode should be "V-2024-0157"
        assertEquals("V-2024-0157", resource.tripCode());
    }

    @Test
    @DisplayName("should return Sin viaje when tripId is null")
    void shouldReturnSinViajeWhenTripIdIsNull() {
        // GIVEN alert with tripId=null
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setTripId(null);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN tripCode should be "Sin viaje"
        assertEquals("Sin viaje", resource.tripCode());
    }

    @Test
    @DisplayName("should use triggerValue as value")
    void shouldUseTriggerValueAsValue() {
        // GIVEN alert with triggerValue "8.5°C"
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setTriggerValue("8.5°C");

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN value should be "8.5°C"
        assertEquals("8.5°C", resource.value());
    }

    @Test
    @DisplayName("should return message from alert")
    void shouldReturnMessageFromAlert() {
        // GIVEN alert with message "Temperature exceeded threshold"
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setMessage("Temperature exceeded threshold");

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN message should be preserved
        assertEquals("Temperature exceeded threshold", resource.message());
    }

    @Test
    @DisplayName("should handle null triggerValue")
    void shouldHandleNullTriggerValue() {
        // GIVEN alert with triggerValue=null
        Alert alert = createAlert(AlertSeverity.CRITICAL, false, false);
        alert.setTriggerValue(null);

        // WHEN converting to AlertIncidentResource
        AlertIncidentResource resource = assembler.toResource(alert);

        // THEN value should be null
        assertNull(resource.value());
    }

    private Alert createAlert(AlertSeverity severity, Boolean acknowledged, Boolean resolved) {
        Alert alert = new Alert();
        alert.setId(1L);
        alert.setAlertId("ALT-001");
        alert.setContainerId(5L);
        alert.setAlertType(AlertType.TEMPERATURE);
        alert.setSeverity(severity);
        alert.setMessage("Test alert");
        alert.setTimestamp(LocalDateTime.of(2026, 6, 1, 14, 35, 0));
        alert.setAcknowledged(acknowledged);
        alert.setResolved(resolved);
        alert.setTriggerValue("8.5°C");
        alert.setTripId(10L);
        alert.setLatitude(new BigDecimal("40.7128"));
        alert.setLongitude(new BigDecimal("-74.0060"));
        return alert;
    }
}