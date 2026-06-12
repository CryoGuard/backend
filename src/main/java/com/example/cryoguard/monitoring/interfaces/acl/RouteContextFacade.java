package com.example.cryoguard.monitoring.interfaces.acl;

import org.springframework.stereotype.Component;

/**
 * RouteContextFacade - cross-BC ACL facade implementing RouteQueryService for monitoring.
 * <p>
 * This component provides monitoring BC with access to logistics route information.
 * Implements the {@link com.example.cryoguard.monitoring.interfaces.acl.RouteQueryService} interface.
 * Delegates to logistics BC's RouteQueryService implementation.
 * </p>
 */
@Component
public class RouteContextFacade implements com.example.cryoguard.monitoring.interfaces.acl.RouteQueryService {

    private final com.example.cryoguard.logistics.interfaces.acl.RouteQueryService logisticsRouteQueryService;

    public RouteContextFacade(
            com.example.cryoguard.logistics.interfaces.acl.RouteQueryService logisticsRouteQueryService) {
        this.logisticsRouteQueryService = logisticsRouteQueryService;
    }

    @Override
    public RouteInfoDto getInfoByContainerCode(String containerCode) {
        com.example.cryoguard.logistics.interfaces.acl.RouteInfoDto logisticsDto =
                logisticsRouteQueryService.getInfoByContainerCode(containerCode);
        if (logisticsDto == null) {
            return null;
        }
        return new RouteInfoDto(logisticsDto.tripCode(), logisticsDto.latitude(), logisticsDto.longitude());
    }
}
