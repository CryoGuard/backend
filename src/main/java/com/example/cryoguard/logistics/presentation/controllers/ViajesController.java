package com.example.cryoguard.logistics.presentation.controllers;

import com.example.cryoguard.logistics.application.RouteQueryService;
import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.presentation.assemblers.RouteAssembler;
import com.example.cryoguard.logistics.presentation.resources.ViajeResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ViajesController - dashboard-optimized trip listing.
 * <p>
 * Provides GET /api/v1/viajes endpoint for active trip listing.
 * Distinct from GET /api/v1/routes in that it filters for active states
 * (INITIATED, IN_PROGRESS) and returns compact trip card payload.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/viajes")
@Tag(name = "Viajes", description = "Dashboard trip listing operations")
public class ViajesController {

    private final RouteRepository routeRepository;

    public ViajesController(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @GetMapping
    @Operation(summary = "Get active trips for dashboard",
               description = "Returns filtered active trips (INITIATED, IN_PROGRESS) with optional limit.")
    public ResponseEntity<List<ViajeResource>> getViajes(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer limit) {

        List<RouteStatus> activeStatuses = List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS);
        List<Route> routes;

        if ("activo".equalsIgnoreCase(estado)) {
            routes = routeRepository.findByStatusIn(activeStatuses);
        } else if (estado != null) {
            // Try to parse as RouteStatus
            try {
                RouteStatus status = RouteStatus.valueOf(estado.toUpperCase());
                routes = routeRepository.findByStatus(status);
            } catch (IllegalArgumentException e) {
                routes = List.of();
            }
        } else {
            routes = routeRepository.findByStatusIn(activeStatuses);
        }

        // Apply limit if specified
        if (limit != null && limit > 0 && routes.size() > limit) {
            routes = routes.subList(0, limit);
        }

        List<ViajeResource> viajes = routes.stream()
                .map(this::toViajeResource)
                .toList();

        return ResponseEntity.ok(viajes);
    }

    private ViajeResource toViajeResource(Route route) {
        String estado = RouteAssembler.mapStatusToSpanish(route.getStatus());
        int cajasAsignadas = route.getContainerAssignments() != null ? route.getContainerAssignments().size() : 0;

        // operador requires cross-BC lookup - for now use authorizedOperatorId
        // Full implementation would use IamContextFacade to get operator name
        String operador = null; // TODO: inject IamContextFacade for name lookup

        return new ViajeResource(
            route.getRouteId(),
            operador,
            estado,
            0, // progreso - requires checkpoint completion tracking
            cajasAsignadas,
            route.getOrigin(),
            route.getDestination()
        );
    }
}