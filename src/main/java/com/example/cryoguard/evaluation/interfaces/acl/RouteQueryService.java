package com.example.cryoguard.evaluation.interfaces.acl;

/**
 * RouteQueryService - cross-BC ACL interface for route queries.
 * <p>
 * This interface is consumed by evaluation BC to query route information
 * from the logistics BC. Used to populate tripCode in AlertIncidentResource.
 * </p>
 *
 * T2.7 - Add RouteQueryService interface in evaluation/interfaces/acl/
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
}