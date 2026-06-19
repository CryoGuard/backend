package com.example.cryoguard.logistics.application;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.queries.GetActiveRoutesQuery;
import com.example.cryoguard.logistics.domain.queries.GetRouteByIdQuery;
import com.example.cryoguard.logistics.domain.queries.GetRouteHistoryQuery;
import com.example.cryoguard.logistics.domain.queries.GetRoutesByContainerQuery;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteLocationHistoryRepository;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class RouteQueryService {

    private final RouteRepository routeRepository;
    private final RouteLocationHistoryRepository locationHistoryRepository;

    public RouteQueryService(RouteRepository routeRepository, RouteLocationHistoryRepository locationHistoryRepository) {
        this.routeRepository = routeRepository;
        this.locationHistoryRepository = locationHistoryRepository;
    }

    public Route getById(GetRouteByIdQuery query) {
        return routeRepository.findById(query.id())
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + query.id()));
    }

    public List<Route> getByContainer(GetRoutesByContainerQuery query) {
        return routeRepository.findByContainerId(query.containerId());
    }

    public List<Route> getActiveRoutes(GetActiveRoutesQuery query) {
        return routeRepository.findByStatus(RouteStatus.INITIATED);
    }

    public List<Route> getAllRoutes() {
        return routeRepository.findAll();
    }

    public List<RouteLocationHistory> getRouteHistory(GetRouteHistoryQuery query) {
        return locationHistoryRepository.findByRouteIdOrderByTimestamp(query.routeId());
    }
}