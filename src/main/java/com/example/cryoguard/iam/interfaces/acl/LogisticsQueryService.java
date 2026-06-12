package com.example.cryoguard.iam.interfaces.acl;

import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;

/**
 * LogisticsQueryService - cross-BC ACL interface for querying logistics data from IAM.
 * <p>
 * This interface is defined by the IAM bounded context and implemented by the logistics BC.
 * It allows IAM to query operator trip statistics for populating computed fields in UserResource.
 * </p>
 */
public interface LogisticsQueryService {

    /**
     * Returns trip statistics for a given operator.
     * Used by IAM to populate viajesAsignados and viajesCompletados in UserResource.
     *
     * @param operatorId the operator's User.id (Long)
     * @return RouteStatsDto with activeCount (INITIATED + IN_PROGRESS) and completedCount (COMPLETED)
     */
    RouteStatsDto getStatsByOperator(Long operatorId);
}