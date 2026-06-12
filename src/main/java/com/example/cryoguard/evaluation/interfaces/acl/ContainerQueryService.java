package com.example.cryoguard.evaluation.interfaces.acl;

/**
 * ContainerQueryService - cross-BC ACL interface for container queries.
 * <p>
 * This interface is consumed by evaluation BC to query container information
 * from the monitoring BC. Used to populate boxId in AlertIncidentResource.
 * </p>
 *
 * T2.6 - Add ContainerQueryService interface in evaluation/interfaces/acl/
 */
public interface ContainerQueryService {

    /**
     * Resolves a Container database ID to its containerId field (String).
     * Used by evaluation BC to populate boxId in alert resources.
     *
     * @param containerId the Container database ID (Long)
     * @return the containerId String (e.g. "CG-001"), or String.valueOf(id) if not found
     */
    String getCode(Long containerId);
}