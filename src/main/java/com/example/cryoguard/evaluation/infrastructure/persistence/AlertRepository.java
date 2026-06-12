package com.example.cryoguard.evaluation.infrastructure.persistence;

import com.example.cryoguard.evaluation.domain.entities.Alert;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    Optional<Alert> findByAlertId(String alertId);

    List<Alert> findByContainerId(Long containerId);

    Page<Alert> findAll(Pageable pageable);

    // T2.11: Map frontend status values to DB booleans
    // 'activa' → acknowledged=false AND resolved=false
    // 'pendiente' → acknowledged=true AND resolved=false
    // 'confirmada' → resolved=true
    @Query("SELECT a FROM Alert a WHERE " +
           "(:severity IS NULL OR LOWER(CAST(a.severity AS string)) = LOWER(:severity)) AND " +
           "(:status IS NULL OR " +
           "  (:status = 'activa' AND a.acknowledged = false AND a.resolved = false) OR " +
           "  (:status = 'pendiente' AND a.acknowledged = true AND a.resolved = false) OR " +
           "  (:status = 'confirmada' AND a.resolved = true))")
    Page<Alert> findByFilters(@Param("severity") String severity, @Param("status") String status, Pageable pageable);

    List<Alert> findByResolvedFalseOrderByTimestampDesc();

    List<Alert> findByResolvedTrueOrderByTimestampDesc();

    // T2.15: IA Precision - count confirmed alerts
    long countByResolvedTrue();

    // T2.16: IA Recommendations - count alerts by container since timestamp
    Long countByContainerIdAndTimestampAfter(Long containerId, java.time.LocalDateTime timestamp);

    // T2.16: IA Recommendations - count alerts in night hours (22:00-05:59) since timestamp
    @Query("SELECT COUNT(a) FROM Alert a WHERE " +
           "a.timestamp >= :since AND " +
           "EXTRACT(HOUR FROM a.timestamp) >= :startHour OR " +
           "EXTRACT(HOUR FROM a.timestamp) < :endHour")
    Long countAlertsByHourRange(
        @Param("startHour") int startHour,
        @Param("endHour") int endHour,
        @Param("since") java.time.LocalDateTime since);

    // T2.16: IA Recommendations - count unconfirmed alerts older than hours
    @Query("SELECT COUNT(a) FROM Alert a WHERE " +
           "a.resolved = false AND " +
           "a.timestamp < :cutoff")
    Long countUnconfirmedOlderThanHours(@Param("cutoff") java.time.LocalDateTime cutoff);

    // T2.16: IA Recommendations - count alerts since timestamp
    Long countByTimestampAfter(java.time.LocalDateTime timestamp);

    // T2 - Cross-BC: find active alerts (not resolved) for given container IDs
    List<Alert> findByContainerIdInAndResolvedFalse(List<Long> containerIds);
}