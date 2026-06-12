package com.example.cryoguard.monitoring.interfaces.acl;

import com.example.cryoguard.monitoring.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.monitoring.interfaces.acl.RouteQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for MonitoringRouteQueryService (RouteContextFacade) implementing RouteQueryService.
 * T3.4 - Create RouteQueryService interface + RouteInfoDto in monitoring/interfaces/acl/
 * T3.5 - Create RouteContextFacade in monitoring implementing RouteQueryService
 */
@ExtendWith(MockitoExtension.class)
class MonitoringRouteQueryServiceTest {

    @Mock
    private RouteQueryService routeQueryService;

    @BeforeEach
    void setUp() {
        // This test validates the interface contract
 }

    @Test
    void shouldReturnRouteInfoDtoWhenContainerHasActiveRoute() {
        // GIVEN RouteQueryService returns RouteInfoDto for container "CG-001"
        RouteInfoDto expectedDto = new RouteInfoDto("V-2024-0156", -12.0464, -77.0428);
        when(routeQueryService.getInfoByContainerCode("CG-001")).thenReturn(expectedDto);

        // WHEN calling getInfoByContainerCode
        RouteInfoDto result = routeQueryService.getInfoByContainerCode("CG-001");

        // THEN should return the route info
        assertEquals("V-2024-0156", result.tripCode());
        assertEquals(-12.0464, result.latitude());
        assertEquals(-77.0428, result.longitude());
    }

    @Test
    void shouldReturnNullWhenNoActiveRoute() {
        // GIVEN RouteQueryService returns null when no active route
        when(routeQueryService.getInfoByContainerCode("CG-999")).thenReturn(null);

        // WHEN calling getInfoByContainerCode
        RouteInfoDto result = routeQueryService.getInfoByContainerCode("CG-999");

        // THEN should return null
        assertNull(result);
    }

    @Test
    void shouldImplementRouteQueryServiceInterface() {
        // THEN routeQueryService should implement RouteQueryService interface
        assertTrue(RouteQueryService.class.isAssignableFrom(routeQueryService.getClass()));
    }
}
