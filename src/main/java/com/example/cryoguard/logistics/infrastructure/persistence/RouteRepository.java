package com.example.cryoguard.logistics.infrastructure.persistence;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteRepository extends JpaRepository<Route, Long> {
    Optional<Route> findByRouteId(String routeId);
    List<Route> findByContainerId(Long containerId);
    List<Route> findByStatus(RouteStatus status);
    List<Route> findByContainerIdAndStatus(Long containerId, RouteStatus status);

    // Count methods for cross-BC ACL queries
    long countByAuthorizedOperatorIdAndStatusIn(Long authorizedOperatorId, List<RouteStatus> statuses);
    long countByAuthorizedOperatorIdAndStatus(Long authorizedOperatorId, RouteStatus status);

    // Active routes (INITIATED or IN_PROGRESS)
    @Query("SELECT r FROM Route r WHERE r.status IN :statuses")
    List<Route> findByStatusIn(@Param("statuses") List<RouteStatus> statuses);

    // For getInfoByContainerCode - find active route by container code via assignments
    @Query("SELECT r FROM Route r JOIN r.containerAssignments rca WHERE rca.containerId = :containerId AND r.status IN :statuses")
    List<Route> findActiveRoutesByContainerId(@Param("containerId") Long containerId, @Param("statuses") List<RouteStatus> statuses);

    // Find max route number for ID generation
    @Query("SELECT MAX(r.routeId) FROM Route r")
    String findMaxRouteId();
}