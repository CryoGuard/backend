package com.example.cryoguard.monitoring.interfaces.acl;

import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for ContainerContextFacade implementing ContainerQueryService.
 * T3.3 - Create ContainerContextFacade implementing ContainerQueryService.getCode(id)
 */
@ExtendWith(MockitoExtension.class)
class ContainerContextFacadeTest {

    @Mock
    private ContainerRepository containerRepository;

    private ContainerContextFacade facade;

    @BeforeEach
    void setUp() {
        facade = new ContainerContextFacade(containerRepository);
    }

    @Test
    void shouldReturnContainerCodeById() {
        // GIVEN container with id 5 has containerId "CG-001"
        Container container = new Container();
        container.setId(5L);
        container.setContainerId("CG-001");
        when(containerRepository.findById(5L)).thenReturn(Optional.of(container));

        // WHEN calling getCode(5L)
        String code = facade.getCode(5L);

        // THEN should return "CG-001"
        assertEquals("CG-001", code);
    }

    @Test
    void shouldReturnStringValueOfIdWhenContainerNotFound() {
        // GIVEN container not found
        when(containerRepository.findById(99L)).thenReturn(Optional.empty());

        // WHEN calling getCode(99L)
        String code = facade.getCode(99L);

        // THEN should return "99" as fallback
        assertEquals("99", code);
    }

    @Test
    void shouldImplementContainerQueryServiceInterface() {
        // THEN facade should implement ContainerQueryService
        assertTrue(ContainerQueryService.class.isAssignableFrom(ContainerContextFacade.class));
    }
}
