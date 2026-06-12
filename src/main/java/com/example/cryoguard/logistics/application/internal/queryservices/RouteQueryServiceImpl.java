package com.example.cryoguard.logistics.application.internal.queryservices;

import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.logistics.interfaces.acl.RouteQueryService;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * RouteQueryService implementation - cross-BC ACL facade for route queries.
 * <p>
 * Implements RouteQueryService interface for consumption by other BCs
 * (iam, monitoring, evaluation).
 * </p>
 */
@Service
@Transactional(readOnly = true)
public class RouteQueryServiceImpl implements RouteQueryService {

    private final RouteRepository routeRepository;
    private final ContainerRepository containerRepository;

    public RouteQueryServiceImpl(RouteRepository routeRepository, ContainerRepository containerRepository) {
        this.routeRepository = routeRepository;
        this.containerRepository = containerRepository;
    }

    @Override
    public String getCode(Long routeId) {
        return routeRepository.findById(routeId)
                .map(Route::getRouteId)
                .orElse(null);
    }

    @Override
    public RouteStatsDto getStatsByOperator(Long operatorId) {
        List<RouteStatus> activeStatuses = List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS);
        long activeCount = routeRepository.countByAuthorizedOperatorIdAndStatusIn(operatorId, activeStatuses);
        long completedCount = routeRepository.countByAuthorizedOperatorIdAndStatus(operatorId, RouteStatus.completed);
        return new RouteStatsDto((int) activeCount, (int) completedCount);
    }

    @Override
    public RouteInfoDto getInfoByContainerCode(String containerCode) {
        // Find container by its containerId code (e.g. "CG-001")
        // Then find active routes via RouteContainerAssignment
        return containerRepository.findByContainerId(containerCode)
                .map(container -> {
                    List<RouteStatus> activeStatuses = List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS);
                    List<Route> activeRoutes = routeRepository.findActiveRoutesByContainerId(container.getId(), activeStatuses);
                    if (activeRoutes.isEmpty()) {
                        return null;
                    }
                    Route route = activeRoutes.get(0);
                    Double lat = route.getCurrentLocation() != null && route.getCurrentLocation().getLatitude() != null
                            ? route.getCurrentLocation().getLatitude().doubleValue() : null;
                    Double lng = route.getCurrentLocation() != null && route.getCurrentLocation().getLongitude() != null
                            ? route.getCurrentLocation().getLongitude().doubleValue() : null;
                    return new RouteInfoDto(route.getRouteId(), lat, lng);
                })
                .orElse(null);
    }
}