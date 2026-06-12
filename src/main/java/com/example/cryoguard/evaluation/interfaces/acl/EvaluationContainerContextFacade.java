package com.example.cryoguard.evaluation.interfaces.acl;

import com.example.cryoguard.monitoring.interfaces.acl.ContainerQueryService;
import org.springframework.stereotype.Component;

/**
 * EvaluationContainerContextFacade - cross-BC ACL facade implementing evaluation's ContainerQueryService.
 * <p>
 * This component provides evaluation BC with access to monitoring container data.
 * Implements the {@link ContainerQueryService} interface defined by evaluation.
 * Delegates to monitoring BC's ContainerQueryService with safe fallback.
 * </p>
 *
 * T2.8 - Create EvaluationContainerContextFacade in evaluation
 */
@Component("evaluationContainerContextFacade")
public class EvaluationContainerContextFacade implements com.example.cryoguard.evaluation.interfaces.acl.ContainerQueryService {

    private final ContainerQueryService monitoringContainerQueryService;

    public EvaluationContainerContextFacade(ContainerQueryService monitoringContainerQueryService) {
        this.monitoringContainerQueryService = monitoringContainerQueryService;
    }

    @Override
    public String getCode(Long containerId) {
        if (monitoringContainerQueryService == null) {
            return String.valueOf(containerId);
        }
        try {
            String code = monitoringContainerQueryService.getCode(containerId);
            return code != null ? code : String.valueOf(containerId);
        } catch (Exception e) {
            return String.valueOf(containerId);
        }
    }
}
