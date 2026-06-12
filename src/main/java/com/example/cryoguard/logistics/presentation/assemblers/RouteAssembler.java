package com.example.cryoguard.logistics.presentation.assemblers;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.commands.CreateRouteCommand;
import com.example.cryoguard.logistics.domain.commands.UpdateRouteCommand;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.presentation.resources.CreateRouteResource;
import com.example.cryoguard.logistics.presentation.resources.RouteLocationResource;
import com.example.cryoguard.logistics.presentation.resources.RouteResource;
import com.example.cryoguard.logistics.presentation.resources.UpdateRouteResource;

import java.util.List;

/**
 * RouteAssembler - converts between Route entities and RouteResource DTOs.
 * <p>
 * Maps Route entity fields to Vue-shaped RouteResource response.
 * Cross-BC computed fields (operador, cajasAsignadas, alertCount, assignedBoxes)
 * require injected services and are computed in the Spring-managed RouteAssemblerImpl.
 * </p>
 */
public class RouteAssembler {

    /**
     * Maps Route entity to new Vue-shaped RouteResource.
     * Note: This static method does not compute cross-BC fields.
     * Use RouteAssemblerImpl for full computation with cross-BC services.
     */
    public static RouteResource toResource(Route route) {
        String estado = mapStatusToSpanish(route.getStatus());
        Integer progreso = computeProgreso(route.getCheckpoints(), 0); // completed checkpoints unknown here
        int cajasAsignadas = route.getContainerAssignments() != null ? route.getContainerAssignments().size() : 0;

        return new RouteResource(
            route.getId(),
            route.getRouteId(),
            estado,
            null, // operador - requires IamContextFacade cross-BC lookup
            progreso,
            cajasAsignadas,
            0, // alertCount - requires AlertQueryService cross-BC lookup
            List.of() // assignedBoxes - requires ContainerQueryService cross-BC lookup
        );
    }

    /**
     * Maps Route status enum value to Spanish string for Vue frontend.
     */
    public static String mapStatusToSpanish(RouteStatus status) {
        if (status == null) return null;
        return switch (status) {
            case INITIATED -> "iniciado";
            case IN_PROGRESS -> "en_ruta";
            case active -> "active"; // legacy
            case completed -> "completado";
            case cancelled -> "cancelado";
        };
    }

    /**
     * Computes progress percentage from completed vs total checkpoints.
     * Returns 0 when total is null or 0.
     */
    public static Integer computeProgreso(Integer completedCheckpoints, Integer totalCheckpoints) {
        if (totalCheckpoints == null || totalCheckpoints == 0) {
            return 0;
        }
        if (completedCheckpoints == null) {
            return 0;
        }
        return Math.round((float) completedCheckpoints / totalCheckpoints * 100);
    }

    public static CreateRouteCommand toCreateCommand(CreateRouteResource resource) {
        return new CreateRouteCommand(
            resource.name(),
            resource.containerId(),
            resource.origin(),
            resource.destination(),
            resource.distanceKm(),
            resource.estimatedDurationMinutes(),
            resource.checkpoints(),
            resource.startTime(),
            resource.estimatedArrival(),
            resource.authorizedOperatorId(),
            resource.containerIds()
        );
    }

    public static UpdateRouteCommand toUpdateCommand(UpdateRouteResource resource) {
        return new UpdateRouteCommand(
            resource.name(),
            resource.origin(),
            resource.destination(),
            resource.distanceKm(),
            resource.estimatedDurationMinutes(),
            resource.checkpoints(),
            resource.startTime(),
            resource.estimatedArrival()
        );
    }

    public static RouteLocationResource toLocationResource(RouteLocationHistory location) {
        return new RouteLocationResource(
            location.getId(),
            location.getRoute().getId(),
            location.getTimestamp(),
            location.getLatitude(),
            location.getLongitude(),
            location.getSpeed(),
            location.getHeading()
        );
    }

    public static List<RouteResource> toResourceList(List<Route> routes) {
        return routes.stream().map(RouteAssembler::toResource).toList();
    }

    public static List<RouteLocationResource> toLocationResourceList(List<RouteLocationHistory> locations) {
        return locations.stream().map(RouteAssembler::toLocationResource).toList();
    }
}