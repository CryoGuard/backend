package com.example.cryoguard.evaluation.application;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import com.example.cryoguard.evaluation.infrastructure.persistence.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AlertQueryServiceImpl implements AlertQueryService {

    private final AlertRepository alertRepository;

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-PE"));

    @Override
    public Optional<Alert> getAlertById(Long alertId) {
        return alertRepository.findById(alertId);
    }

    @Override
    public List<Alert> getAlertsByContainer(Long containerId) {
        return alertRepository.findByContainerId(containerId);
    }

    @Override
    public List<Alert> getAlertsByStatus(Boolean resolved) {
        if (Boolean.TRUE.equals(resolved)) {
            return alertRepository.findByResolvedTrueOrderByTimestampDesc();
        } else {
            return alertRepository.findByResolvedFalseOrderByTimestampDesc();
        }
    }

    @Override
    public List<Alert> getAllAlerts() {
        return alertRepository.findAll();
    }

    @Override
    public Page<Alert> getAlertsPage(Pageable pageable) {
        return alertRepository.findAll(pageable);
    }

    @Override
    public Page<Alert> getAlertsByFilters(String severity, String status, Pageable pageable) {
        return alertRepository.findByFilters(severity, status, pageable);
    }

    @Override
    public List<AlertSummaryDto> getActiveAlertsByContainerIds(List<Long> containerIds) {
        if (containerIds == null || containerIds.isEmpty()) {
            return List.of();
        }
        return alertRepository.findByContainerIdInAndResolvedFalse(containerIds).stream()
            .map(this::toAlertSummaryDto)
            .toList();
    }

    private AlertSummaryDto toAlertSummaryDto(Alert alert) {
        String severity = alert.getSeverity() == com.example.cryoguard.evaluation.domain.valueobjects.AlertSeverity.CRITICAL
            ? "critica" : "advertencia";
        String status = deriveStatus(alert.getAcknowledged(), alert.getResolved());
        String timestamp = alert.getTimestamp() != null
            ? alert.getTimestamp().format(TIMESTAMP_FORMATTER) : "";
        return new AlertSummaryDto(
            alert.getAlertId(),
            severity,
            status,
            alert.getMessage(),
            timestamp
        );
    }

    private String deriveStatus(Boolean acknowledged, Boolean resolved) {
        if (Boolean.TRUE.equals(resolved)) {
            return "confirmada";
        } else if (Boolean.TRUE.equals(acknowledged)) {
            return "pendiente";
        } else {
            return "activa";
        }
    }

    @Override
    public List<Alert> getAlertsSince(LocalDateTime since, String severity, String status) {
        return alertRepository.findByTimestampAfterWithFilters(since, severity, status);
    }

    @Override
    public List<Alert> getAlertsBetween(LocalDateTime since, LocalDateTime until, String severity, String status) {
        return alertRepository.findByTimestampRangeWithFilters(since, until, severity, status);
    }

    @Override
    public long countActiveAlerts() {
        return alertRepository.countByResolvedFalse();
    }
}