package com.example.cryoguard.monitoring.interfaces.acl;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * ContainerContextFacade - cross-BC ACL facade implementing ContainerQueryService.
 * <p>
 * This component provides evaluation BC with access to monitoring container data.
 * Implements the {@link ContainerQueryService} interface defined by monitoring.
 * </p>
 */
@Component
public class ContainerContextFacade implements ContainerQueryService {

    private final ContainerRepository containerRepository;

    public ContainerContextFacade(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    @Override
    public String getCode(Long containerId) {
        Optional<Container> container = containerRepository.findById(containerId);
        return container.map(Container::getContainerId)
                .orElseGet(() -> String.valueOf(containerId));
    }
}
