package com.example.cryoguard.logistics.infrastructure.persistence;

import com.example.cryoguard.logistics.domain.entities.RouteContainerAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RouteContainerAssignmentRepository extends JpaRepository<RouteContainerAssignment, Long> {

    List<RouteContainerAssignment> findByRouteId(Long routeId);

    @Query("SELECT rca.containerId FROM RouteContainerAssignment rca WHERE rca.route.id = :routeId")
    List<Long> findContainerIdsByRouteId(@Param("routeId") Long routeId);

    long countByRouteId(Long routeId);
}