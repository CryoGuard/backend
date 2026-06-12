package com.example.cryoguard.logistics.presentation.assemblers;

import com.example.cryoguard.iam.interfaces.acl.IamContextFacade;
import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.presentation.resources.RouteLocationResource;
import com.example.cryoguard.logistics.presentation.resources.RouteResource;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * RouteAssemblerImpl - Spring-managed assembler with cross-BC service injection.
 * <p>
 * Computes cross-BC fields (operador) via IamContextFacade.
 * </p>
 */
@Component
public class RouteAssemblerImpl {

    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private final IamContextFacade iamContextFacade;

    public RouteAssemblerImpl(IamContextFacade iamContextFacade) {
        this.iamContextFacade = iamContextFacade;
    }

    public RouteResource toResource(Route route) {
        String estado = mapStatusToSpanish(route.getStatus());
        Integer progreso = computeProgreso(route.getCheckpoints(), 0);
        int cajasAsignadas = route.getContainerAssignments() != null ? route.getContainerAssignments().size() : 0;

        String operador = resolveOperatorName(route.getAuthorizedOperatorId());
        String startTime = formatDateTime(route.getStartTime());
        String estimatedArrival = formatDateTime(route.getEstimatedArrival());

        return new RouteResource(
            route.getId(),
            route.getRouteId(),
            estado,
            operador,
            progreso,
            cajasAsignadas,
            0,
            List.of(),
            route.getOrigin(),
            route.getDestination(),
            startTime,
            estimatedArrival
        );
    }

    public List<RouteResource> toResourceList(List<Route> routes) {
        return routes.stream().map(this::toResource).toList();
    }

    private String resolveOperatorName(Long authorizedOperatorId) {
        if (authorizedOperatorId == null) {
            return null;
        }
        String name = iamContextFacade.fetchUsernameByUserId(authorizedOperatorId);
        return (name == null || name.isEmpty()) ? null : name;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(ISO_FORMATTER);
    }

    public String mapStatusToSpanish(RouteStatus status) {
        if (status == null) return null;
        return switch (status) {
            case INITIATED -> "iniciado";
            case IN_PROGRESS -> "en_ruta";
            case active -> "active";
            case completed -> "completado";
            case cancelled -> "cancelado";
        };
    }

    public Integer computeProgreso(Integer completedCheckpoints, Integer totalCheckpoints) {
        if (totalCheckpoints == null || totalCheckpoints == 0) {
            return 0;
        }
        if (completedCheckpoints == null) {
            return 0;
        }
        return Math.round((float) completedCheckpoints / totalCheckpoints * 100);
    }

    public RouteLocationResource toLocationResource(RouteLocationHistory location) {
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

    public List<RouteLocationResource> toLocationResourceList(List<RouteLocationHistory> locations) {
        return locations.stream().map(this::toLocationResource).toList();
    }
}