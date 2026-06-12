package com.example.cryoguard.evaluation.application;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity;
import com.example.cryoguard.evaluation.domain.valueobjects.AlertType;
import com.example.cryoguard.evaluation.infrastructure.persistence.AlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for AlertQueryService - cross-BC alert queries.
 * T2 - Tests for getActiveAlertsByContainerIds
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AlertQueryService Tests")
class AlertQueryServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private AlertQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AlertQueryServiceImpl(alertRepository);
    }

    @Test
    @DisplayName("should return empty list when containerIds is null")
    void shouldReturnEmptyListWhenContainerIdsNull() {
        // WHEN getActiveAlertsByContainerIds with null
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(null);

        // THEN returns empty list
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return empty list when containerIds is empty")
    void shouldReturnEmptyListWhenContainerIdsEmpty() {
        // WHEN getActiveAlertsByContainerIds with empty list
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of());

        // THEN returns empty list
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should return active alerts for given container IDs")
    void shouldReturnActiveAlertsForGivenContainerIds() {
        // GIVEN two active alerts for container IDs 1 and 2
        Alert alert1 = createAlert(1L, "ALT-001", AlertSeverity.CRITICAL, false, false);
        Alert alert2 = createAlert(2L, "ALT-002", AlertSeverity.WARNING, false, false);
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L, 2L)))
            .thenReturn(List.of(alert1, alert2));

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L, 2L));

        // THEN returns 2 alerts with correct fields
        assertEquals(2, result.size());
        AlertQueryService.AlertSummaryDto first = result.get(0);
        assertEquals("ALT-001", first.id());
        assertEquals("critica", first.severity());
        assertEquals("activa", first.status());
    }

    @Test
    @DisplayName("should return empty list when no active alerts for container IDs")
    void shouldReturnEmptyListWhenNoActiveAlerts() {
        // GIVEN no active alerts
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L, 2L)))
            .thenReturn(List.of());

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L, 2L));

        // THEN returns empty list
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("should remap CRITICAL severity to critica")
    void shouldRemapCriticalSeverityToCritica() {
        // GIVEN CRITICAL severity active alert
        Alert alert = createAlert(1L, "ALT-001", AlertSeverity.CRITICAL, false, false);
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L)))
            .thenReturn(List.of(alert));

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L));

        // THEN severity is "critica"
        assertEquals("critica", result.get(0).severity());
    }

    @Test
    @DisplayName("should remap WARNING severity to advertencia")
    void shouldRemapWarningSeverityToAdvertencia() {
        // GIVEN WARNING severity active alert
        Alert alert = createAlert(1L, "ALT-001", AlertSeverity.WARNING, false, false);
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L)))
            .thenReturn(List.of(alert));

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L));

        // THEN severity is "advertencia"
        assertEquals("advertencia", result.get(0).severity());
    }

    @Test
    @DisplayName("should derive status as activa when not acknowledged and not resolved")
    void shouldDeriveStatusActivaWhenNotAcknowledgedAndNotResolved() {
        // GIVEN active alert (not acknowledged, not resolved)
        Alert alert = createAlert(1L, "ALT-001", AlertSeverity.WARNING, false, false);
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L)))
            .thenReturn(List.of(alert));

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L));

        // THEN status is "activa"
        assertEquals("activa", result.get(0).status());
    }

    @Test
    @DisplayName("should derive status as pendente when acknowledged but not resolved")
    void shouldDeriveStatusPendienteWhenAcknowledgedButNotResolved() {
        // GIVEN acknowledged but not resolved alert
        Alert alert = createAlert(1L, "ALT-001", AlertSeverity.WARNING, true, false);
        when(alertRepository.findByContainerIdInAndResolvedFalse(List.of(1L)))
            .thenReturn(List.of(alert));

        // WHEN getActiveAlertsByContainerIds
        List<AlertQueryService.AlertSummaryDto> result = service.getActiveAlertsByContainerIds(List.of(1L));

        // THEN status is "pendiente"
        assertEquals("pendiente", result.get(0).status());
    }

    private Alert createAlert(Long containerId, String alertId, AlertSeverity severity, Boolean acknowledged, Boolean resolved) {
        Alert alert = new Alert();
        alert.setId(1L);
        alert.setAlertId(alertId);
        alert.setContainerId(containerId);
        alert.setAlertType(AlertType.TEMPERATURE);
        alert.setSeverity(severity);
        alert.setMessage("Test alert message");
        alert.setTimestamp(LocalDateTime.now());
        alert.setAcknowledged(acknowledged);
        alert.setResolved(resolved);
        return alert;
    }
}