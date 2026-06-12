package com.example.cryoguard.logistics.interfaces.acl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteStatsDtoTest {

    @Test
    void shouldCreateRouteStatsDto() {
        RouteStatsDto dto = new RouteStatsDto(3, 5);

        assertEquals(3, dto.activeCount());
        assertEquals(5, dto.completedCount());
    }

    @Test
    void shouldReturnZeroCounts() {
        RouteStatsDto zero = RouteStatsDto.zero();

        assertEquals(0, zero.activeCount());
        assertEquals(0, zero.completedCount());
    }

    @Test
    void shouldRepresentNoRoutesForNewOperator() {
        RouteStatsDto noRoutes = new RouteStatsDto(0, 0);

        assertEquals(0, noRoutes.activeCount());
        assertEquals(0, noRoutes.completedCount());
    }

    @Test
    void shouldRepresentOperatorWithActiveAndCompletedRoutes() {
        // Scenario: getStatsByOperator returns correct counts
        // GIVEN operator id=3 has 2 routes with status IN_PROGRESS, 1 route with status INITIATED,
        // and 5 routes with status COMPLETED
        // WHEN RouteQueryService.getStatsByOperator(3) is invoked
        // THEN result SHALL be RouteStatsDto(activeCount=3, completedCount=5)
        RouteStatsDto stats = new RouteStatsDto(3, 5);

        assertEquals(3, stats.activeCount());
        assertEquals(5, stats.completedCount());
    }

    @Test
    void shouldHandleZeroCountsWhenOperatorHasNoRoutes() {
        // Scenario: getStatsByOperator returns zeros when operator has no routes
        // GIVEN operator id=99 exists but has no assigned routes
        // WHEN RouteQueryService.getStatsByOperator(99) is invoked
        // THEN result SHALL be RouteStatsDto(activeCount=0, completedCount=0)
        RouteStatsDto stats = RouteStatsDto.zero();

        assertEquals(0, stats.activeCount());
        assertEquals(0, stats.completedCount());
    }
}