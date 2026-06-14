package com.example.cryoguard.logistics.application;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.commands.CompleteRouteCommand;
import com.example.cryoguard.logistics.domain.commands.CreateRouteCommand;
import com.example.cryoguard.logistics.domain.commands.RecordRouteLocationCommand;
import com.example.cryoguard.logistics.domain.commands.UpdateRouteCommand;
import com.example.cryoguard.logistics.domain.entities.RouteContainerAssignment;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.valueobjects.GeofenceStatus;
import com.example.cryoguard.logistics.domain.valueobjects.GeofenceType;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.GeofenceRepository;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteContainerAssignmentRepository;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteLocationHistoryRepository;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class RouteCommandService {

    private final RouteRepository routeRepository;
    private final GeofenceRepository geofenceRepository;
    private final RouteLocationHistoryRepository locationHistoryRepository;
    private final RouteContainerAssignmentRepository containerAssignmentRepository;

    public RouteCommandService(RouteRepository routeRepository, GeofenceRepository geofenceRepository,
                               RouteLocationHistoryRepository locationHistoryRepository,
                               RouteContainerAssignmentRepository containerAssignmentRepository) {
        this.routeRepository = routeRepository;
        this.geofenceRepository = geofenceRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.containerAssignmentRepository = containerAssignmentRepository;
    }

    public Route createRoute(CreateRouteCommand command) {
        String routeId = generateRouteId();
        Route route = new Route(
            routeId,
            command.name(),
            RouteStatus.INITIATED,
            command.origin(),
            command.destination(),
            command.distanceKm(),
            command.estimatedDurationMinutes(),
            command.checkpoints(),
            command.startTime(),
            command.estimatedArrival(),
            command.authorizedOperatorId()
        );

        Route savedRoute = routeRepository.save(route);

        // Create container assignments if provided
        if (command.containerIds() != null && !command.containerIds().isEmpty()) {
            for (Long containerId : command.containerIds()) {
                RouteContainerAssignment assignment = new RouteContainerAssignment(savedRoute, containerId);
                containerAssignmentRepository.save(assignment);
            }
        }

        return savedRoute;
    }

    public Route updateRoute(Long id, UpdateRouteCommand command) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        route.setName(command.name());
        route.setOrigin(command.origin());
        route.setDestination(command.destination());
        route.setDistanceKm(command.distanceKm());
        route.setEstimatedDurationMinutes(command.estimatedDurationMinutes());
        route.setCheckpoints(command.checkpoints());
        route.setStartTime(command.startTime());
        route.setEstimatedArrival(command.estimatedArrival());
        return routeRepository.save(route);
    }

    public Route completeRoute(Long id, CompleteRouteCommand command) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        if (route.getStatus() != RouteStatus.INITIATED && route.getStatus() != RouteStatus.IN_PROGRESS && route.getStatus() != RouteStatus.active) {
            throw new IllegalStateException("Only active or in-progress routes can be completed");
        }
        route.complete();
        return routeRepository.save(route);
    }

    public void deleteRoute(Long id) {
        Route route = routeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + id));
        routeRepository.delete(route);
    }

    public RouteLocationHistory recordLocation(Long routeId, RecordRouteLocationCommand command) {
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));

        RouteLocationHistory location = new RouteLocationHistory(
            route,
            command.timestamp(),
            command.latitude(),
            command.longitude(),
            command.speed(),
            command.heading()
        );
        RouteLocationHistory saved = locationHistoryRepository.save(location);

        checkGeofenceViolation(command.latitude(), command.longitude());

        return saved;
    }

    private void checkGeofenceViolation(BigDecimal latitude, BigDecimal longitude) {
        Set<String> activeAuthorizedZones = new HashSet<>();
        Set<String> activeRestrictedZones = new HashSet<>();

        geofenceRepository.findByStatus(GeofenceStatus.active).forEach(geofence -> {
            boolean isInside = geofence.containsPoint(latitude, longitude);
            if (geofence.getType() == GeofenceType.circle) {
                if (!isInside) {
                    activeAuthorizedZones.add(geofence.getName());
                }
            } else if (geofence.getType() == GeofenceType.polygon) {
                if (isInside) {
                    activeRestrictedZones.add(geofence.getName());
                }
            }
        });

        if (!activeRestrictedZones.isEmpty()) {
            throw new IllegalStateException("GEOFENCE_VIOLATION: Container entered restricted zone(s): " + activeRestrictedZones);
        }
        if (!activeAuthorizedZones.isEmpty()) {
            throw new IllegalStateException("GEOFENCE_VIOLATION: Container exited authorized zone(s): " + activeAuthorizedZones);
        }
    }

    private String generateRouteId() {
        String maxRouteId = routeRepository.findMaxRouteId();
        if (maxRouteId == null) {
            return "ROUTE-001";
        }
        // Extract the number from "ROUTE-XXX"
        int maxNum = Integer.parseInt(maxRouteId.substring(6));
        return String.format("ROUTE-%03d", maxNum + 1);
    }
}