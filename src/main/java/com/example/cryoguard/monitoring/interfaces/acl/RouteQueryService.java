package com.example.cryoguard.monitoring.interfaces.acl;

/**
 * RouteQueryService - cross-BC ACL interface for route queries from monitoring BC.
 * <p>
 * This interface is consumed by monitoring BC to query route information from logistics.
 * It differs from logistics.interfaces.acl.RouteQueryService which is the provider side.
 * </p>
 */
public interface RouteQueryService {

    /**
     * Resolves a container code to its active route information.
     * Used by monitoring BC to populate tripCode, latitude, longitude in DeviceResource.
     *
     * @param containerCode the Container.containerId String (e.g. "CG-001")
     * @return RouteInfoDto with tripCode and coordinates, or null if no active route
     */
    RouteInfoDto getInfoByContainerCode(String containerCode);
}
