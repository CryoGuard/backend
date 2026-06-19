package com.example.cryoguard.monitoring.presentation.assemblers;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.presentation.resources.ContainerResource;
import com.example.cryoguard.monitoring.presentation.resources.CreateContainerWithKeyResource;
import org.springframework.stereotype.Component;

@Component
public class CreateContainerWithKeyResourceAssembler {

    private final ContainerResourceAssembler containerAssembler;

    public CreateContainerWithKeyResourceAssembler(ContainerResourceAssembler containerAssembler) {
        this.containerAssembler = containerAssembler;
    }

    public CreateContainerWithKeyResource toResource(Container container, String apiKey) {
        ContainerResource containerResource = containerAssembler.toResource(container);
        return new CreateContainerWithKeyResource(containerResource, apiKey);
    }
}
