package com.example.cryoguard.logistics.application;

import com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for LogisticsContextFacade implementing LogisticsQueryService.
 */
@ExtendWith(MockitoExtension.class)
class LogisticsContextFacadeTest {

    @Mock
    private RouteRepository routeRepository;

    private LogisticsContextFacade facade;

    @BeforeEach
    void setUp() {
        facade = new LogisticsContextFacade(routeRepository);
    }

    @Test
    void shouldReturnActiveAndCompletedCounts() {
        // GIVEN operator 5 has 2 active routes and 3 completed
        when(routeRepository.countByAuthorizedOperatorIdAndStatusIn(5L, List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS)))
                .thenReturn(2L);
        when(routeRepository.countByAuthorizedOperatorIdAndStatus(5L, RouteStatus.completed))
                .thenReturn(3L);

        // WHEN calling getStatsByOperator
        RouteStatsDto stats = facade.getStatsByOperator(5L);

        // THEN stats should reflect correct counts
        assertEquals(2, stats.activeCount());
        assertEquals(3, stats.completedCount());
    }

    @Test
    void shouldReturnZeroCountsWhenNoRoutes() {
        // GIVEN operator 99 has no routes
        when(routeRepository.countByAuthorizedOperatorIdAndStatusIn(99L, List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS)))
                .thenReturn(0L);
        when(routeRepository.countByAuthorizedOperatorIdAndStatus(99L, RouteStatus.completed))
                .thenReturn(0L);

        // WHEN calling getStatsByOperator
        RouteStatsDto stats = facade.getStatsByOperator(99L);

        // THEN stats should be zero
        assertEquals(0, stats.activeCount());
        assertEquals(0, stats.completedCount());
    }

    @Test
    void shouldImplementLogisticsQueryServiceInterface() {
        // THEN facade should implement LogisticsQueryService
        assertTrue(LogisticsQueryService.class.isAssignableFrom(LogisticsContextFacade.class));
    }
}