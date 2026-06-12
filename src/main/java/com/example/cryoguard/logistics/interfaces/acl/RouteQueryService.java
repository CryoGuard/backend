package com.example.cryoguard.logistics.interfaces.acl;

import com.example.cryoguard.logistics.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;

/**
 * RouteQueryService - cross-BC ACL interface for route queries.
 * <p>
 * This interface is consumed by other bounded contexts (iam, monitoring, evaluation)
 * to query route information from the logistics BC.
 * </p>
 */
public interface RouteQueryService {

    /**
     * Resolves a Route database ID to its routeId field (String).
     * Used by evaluation BC to populate tripCode in alert resources.
     *
     * @param routeId the Route database ID (Long)
     * @return the routeId String (e.g. "V-2024-0156"), or null if not found
     */
    String getCode(Long routeId);

    /**
     * Returns trip statistics for a given operator.
     * Used by iam BC to populate viajesAsignados and viajesCompletados in UserResource.
     *
     * @param operatorId the operator's User.id (Long)
     * @return RouteStatsDto with activeCount (INITIATED + IN_PROGRESS) and completedCount (COMPLETED)
     */
    RouteStatsDto getStatsByOperator(Long operatorId);

    /**
     * Resolves a container code to its active route information.
     * Used by monitoring BC to populate tripCode, latitude, longitude in DeviceResource.
     *
     * @param containerCode the Container.containerId String (e.g. "CG-001")
     * @return RouteInfoDto with tripCode and coordinates, or null if no active route
     */
    RouteInfoDto getInfoByContainerCode(String containerCode);
}