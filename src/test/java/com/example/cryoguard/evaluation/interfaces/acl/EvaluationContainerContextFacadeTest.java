package com.example.cryoguard.evaluation.interfaces.acl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for ContainerContextFacade - cross-BC ACL facade implementing ContainerQueryService.
 * T2.8 - Create ContainerContextFacade in evaluation (implements evaluation's ContainerQueryService)
 *
 * This facade delegates to monitoring BC's ContainerContextFacade.
 * Safe fallback: return String.valueOf(id) if monitoring BC is unavailable.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationContainerContextFacadeTest {

    // Mock monitoring's ContainerQueryService (not evaluation's)
    @Mock
    private com.example.cryoguard.monitoring.interfaces.acl.ContainerQueryService monitoringContainerQueryService;

    private EvaluationContainerContextFacade facade;

    @BeforeEach
    void setUp() {
        facade = new EvaluationContainerContextFacade(monitoringContainerQueryService);
    }

    @Test
    @DisplayName("should return container code when found")
    void shouldReturnContainerCodeWhenFound() {
        // GIVEN container 5 exists with code "CG-047"
        when(monitoringContainerQueryService.getCode(5L)).thenReturn("CG-047");

        // WHEN calling getCode(5L)
        String result = facade.getCode(5L);

        // THEN result should be "CG-047"
        assertEquals("CG-047", result);
    }

    @Test
    @DisplayName("should return fallback when container not found")
    void shouldReturnFallbackWhenContainerNotFound() {
        // GIVEN container 99 does not exist
        when(monitoringContainerQueryService.getCode(99L)).thenReturn("99");

        // WHEN calling getCode(99L)
        String result = facade.getCode(99L);

        // THEN result should be "99" (fallback)
        assertEquals("99", result);
    }

    @Test
    @DisplayName("should return raw id when monitoring service returns null")
    void shouldReturnRawIdWhenMonitoringServiceReturnsNull() {
        // GIVEN monitoring service returns null (container not found)
        when(monitoringContainerQueryService.getCode(42L)).thenReturn(null);

        // WHEN calling getCode(42L)
        String result = facade.getCode(42L);

        // THEN result should be "42" (raw Long as String)
        assertEquals("42", result);
    }

    @Test
    @DisplayName("should implement ContainerQueryService interface")
    void shouldImplementContainerQueryServiceInterface() {
        // THEN facade should implement evaluation's ContainerQueryService
        assertTrue(facade instanceof com.example.cryoguard.evaluation.interfaces.acl.ContainerQueryService);
    }

    @Test
    @DisplayName("should handle null monitoring service gracefully")
    void shouldHandleNullMonitoringServiceGracefully() {
        // GIVEN a facade with null monitoring service
        EvaluationContainerContextFacade facadeWithNull = new EvaluationContainerContextFacade(null);

        // WHEN calling getCode
        String result = facadeWithNull.getCode(5L);

        // THEN result should be fallback "5"
        assertEquals("5", result);
    }
}