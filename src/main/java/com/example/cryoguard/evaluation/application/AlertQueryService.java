package com.example.cryoguard.evaluation.application;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * AlertQueryService - query interface for alert operations.
 *
 * T2 - Cross-BC interface for monitoring BC to query active alerts by container IDs.
 */
public interface AlertQueryService {

    Optional<Alert> getAlertById(Long alertId);

    List<Alert> getAlertsByContainer(Long containerId);

    List<Alert> getAlertsByStatus(Boolean resolved);

    List<Alert> getAllAlerts();

    Page<Alert> getAlertsPage(Pageable pageable);

    Page<Alert> getAlertsByFilters(String severity, String status, Pageable pageable);

    /**
     * Get active alerts (not resolved) for given container IDs.
     * Used by monitoring BC to populate DeviceResource.activeAlerts.
     *
     * @param containerIds list of container database IDs (Long)
     * @return list of AlertSummaryDto for active alerts
     */
    List<AlertSummaryDto> getActiveAlertsByContainerIds(List<Long> containerIds);

    /**
     * DTO for alert summary in cross-BC queries.
     *
     * @param id alert id (String, e.g. "ALT-001")
     * @param severity remapped severity ("critica" or "advertencia")
     * @param status derived status ("activa", "pendiente", "confirmada")
     * @param message alert message
     * @param timestamp formatted timestamp (dd/MM/yyyy HH:mm)
     */
    record AlertSummaryDto(
        String id,
        String severity,
        String status,
        String message,
        String timestamp
    ) {}
}