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
 * Tests for RouteContextFacade - cross-BC ACL facade implementing RouteQueryService.
 * T2.9 - Create RouteContextFacade in evaluation (implements evaluation's RouteQueryService)
 *
 * This facade delegates to logistics BC's RouteQueryService.
 * Safe fallback: "Sin viaje" when null, raw Long as string.
 */
@ExtendWith(MockitoExtension.class)
class EvaluationRouteContextFacadeTest {

    // Mock logistics's RouteQueryService (not evaluation's)
    @Mock
    private com.example.cryoguard.logistics.interfaces.acl.RouteQueryService logisticsRouteQueryService;

    private EvaluationRouteContextFacade facade;

    @BeforeEach
    void setUp() {
        facade = new EvaluationRouteContextFacade(logisticsRouteQueryService);
    }

    @Test
    @DisplayName("should return route code when found")
    void shouldReturnRouteCodeWhenFound() {
        // GIVEN route 10 exists with code "V-2024-0157"
        when(logisticsRouteQueryService.getCode(10L)).thenReturn("V-2024-0157");

        // WHEN calling getCode(10L)
        String result = facade.getCode(10L);

        // THEN result should be "V-2024-0157"
        assertEquals("V-2024-0157", result);
    }

    @Test
    @DisplayName("should return fallback when route not found")
    void shouldReturnFallbackWhenRouteNotFound() {
        // GIVEN route 99 does not exist
        when(logisticsRouteQueryService.getCode(99L)).thenReturn(null);

        // WHEN calling getCode(99L)
        String result = facade.getCode(99L);

        // THEN result should be "99" (raw Long as String)
        assertEquals("99", result);
    }

    @Test
    @DisplayName("should return Sin viaje when routeId is null")
    void shouldReturnSinViajeWhenRouteIdIsNull() {
        // WHEN calling getCode with null
        String result = facade.getCode(null);

        // THEN result should be "Sin viaje"
        assertEquals("Sin viaje", result);
    }

    @Test
    @DisplayName("should implement RouteQueryService interface")
    void shouldImplementRouteQueryServiceInterface() {
        // THEN facade should implement evaluation's RouteQueryService
        assertTrue(facade instanceof com.example.cryoguard.evaluation.interfaces.acl.RouteQueryService);
    }

    @Test
    @DisplayName("should handle null logistics service gracefully")
    void shouldHandleNullLogisticsServiceGracefully() {
        // GIVEN a facade with null logistics service
        EvaluationRouteContextFacade facadeWithNull = new EvaluationRouteContextFacade(null);

        // WHEN calling getCode with null
        String result = facadeWithNull.getCode(null);

        // THEN result should be "Sin viaje"
        assertEquals("Sin viaje", result);
    }

    @Test
    @DisplayName("should return raw id when logistics returns null for non-null routeId")
    void shouldReturnRawIdWhenLogisticsReturnsNull() {
        // GIVEN logistics service returns null for route 42
        when(logisticsRouteQueryService.getCode(42L)).thenReturn(null);

        // WHEN calling getCode(42L)
        String result = facade.getCode(42L);

        // THEN result should be "42" (raw Long as String)
        assertEquals("42", result);
    }
}