package com.example.cryoguard.evaluation.interfaces.acl;

import org.springframework.stereotype.Component;

/**
 * EvaluationRouteContextFacade - cross-BC ACL facade implementing evaluation's RouteQueryService.
 * <p>
 * This component provides evaluation BC with access to logistics route data.
 * Implements the {@link RouteQueryService} interface defined by evaluation.
 * Delegates to logistics BC's RouteQueryService with safe fallback.
 * </p>
 *
 * T2.9 - Create EvaluationRouteContextFacade in evaluation
 */
@Component("evaluationRouteContextFacade")
public class EvaluationRouteContextFacade implements com.example.cryoguard.evaluation.interfaces.acl.RouteQueryService {

    private final com.example.cryoguard.logistics.interfaces.acl.RouteQueryService logisticsRouteQueryService;

    public EvaluationRouteContextFacade(
            com.example.cryoguard.logistics.interfaces.acl.RouteQueryService logisticsRouteQueryService) {
        this.logisticsRouteQueryService = logisticsRouteQueryService;
    }

    @Override
    public String getCode(Long routeId) {
        if (routeId == null) {
            return "Sin viaje";
        }
        if (logisticsRouteQueryService == null) {
            return String.valueOf(routeId);
        }
        try {
            String code = logisticsRouteQueryService.getCode(routeId);
            return code != null ? code : String.valueOf(routeId);
        } catch (Exception e) {
            return String.valueOf(routeId);
        }
    }
}
