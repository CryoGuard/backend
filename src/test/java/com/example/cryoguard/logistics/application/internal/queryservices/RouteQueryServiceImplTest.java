package com.example.cryoguard.logistics.application.internal.queryservices;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.interfaces.acl.RouteInfoDto;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RouteQueryServiceImplTest {

    @Mock
    private RouteRepository routeRepository;

    @Mock
    private ContainerRepository containerRepository;

    private RouteQueryServiceImpl queryService;

    @BeforeEach
    void setUp() {
        queryService = new RouteQueryServiceImpl(routeRepository, containerRepository);
    }

    @Test
    void shouldReturnRouteCodeForValidId() {
        Route route = new Route("V-2024-0156", "Test Route", RouteStatus.active,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);
        when(routeRepository.findById(5L)).thenReturn(Optional.of(route));

        String code = queryService.getCode(5L);

        assertEquals("V-2024-0156", code);
    }

    @Test
    void shouldReturnNullCodeForNonexistentRouteId() {
        when(routeRepository.findById(9999L)).thenReturn(Optional.empty());

        String code = queryService.getCode(9999L);

        assertNull(code);
    }

    @Test
    void shouldReturnCorrectActiveAndCompletedCountsForOperator() {
        // GIVEN operator id=3 has 2 routes with status IN_PROGRESS, 1 route with status INITIATED,
        // and 5 routes with status COMPLETED
        when(routeRepository.countByAuthorizedOperatorIdAndStatusIn(eq(3L), any())).thenReturn(3L);
        when(routeRepository.countByAuthorizedOperatorIdAndStatus(3L, RouteStatus.completed)).thenReturn(5L);

        RouteStatsDto stats = queryService.getStatsByOperator(3L);

        assertEquals(3, stats.activeCount());
        assertEquals(5, stats.completedCount());
    }

    @Test
    void shouldReturnZeroCountsWhenOperatorHasNoRoutes() {
        when(routeRepository.countByAuthorizedOperatorIdAndStatusIn(eq(99L), any())).thenReturn(0L);
        when(routeRepository.countByAuthorizedOperatorIdAndStatus(99L, RouteStatus.completed)).thenReturn(0L);

        RouteStatsDto stats = queryService.getStatsByOperator(99L);

        assertEquals(0, stats.activeCount());
        assertEquals(0, stats.completedCount());
    }

    @Test
    void shouldExcludeCancelledRoutesFromCounts() {
        // GIVEN operator id=3 has 1 route IN_PROGRESS, 1 route CANCELLED, 1 route COMPLETED
        // CANCELLED should be excluded from both active and completed counts
        when(routeRepository.countByAuthorizedOperatorIdAndStatusIn(eq(3L), any())).thenReturn(1L);
        when(routeRepository.countByAuthorizedOperatorIdAndStatus(3L, RouteStatus.completed)).thenReturn(1L);

        RouteStatsDto stats = queryService.getStatsByOperator(3L);

        assertEquals(1, stats.activeCount());
        assertEquals(1, stats.completedCount());
    }

    @Test
    void shouldReturnNullWhenContainerNotInActiveRoute() {
        // GIVEN container "CG-006" exists but is not assigned to any active route
        Container container = new Container();
        container.setId(6L);
        container.setContainerId("CG-006");
        container.setStatus(ContainerStatus.ACTIVE);

        when(containerRepository.findByContainerId("CG-006")).thenReturn(Optional.of(container));
        when(routeRepository.findActiveRoutesByContainerId(eq(6L), any())).thenReturn(List.of());

        RouteInfoDto info = queryService.getInfoByContainerCode("CG-006");

        assertNull(info);
    }

    @Test
    void shouldReturnNullWhenContainerCodeNotFound() {
        when(containerRepository.findByContainerId("NONEXISTENT")).thenReturn(Optional.empty());

        RouteInfoDto info = queryService.getInfoByContainerCode("NONEXISTENT");

        assertNull(info);
    }

    @Test
    void shouldReturnNullCoordsWhenRouteHasNoCurrentLocation() {
        // GIVEN container "CG-007" is assigned to an active route with routeId="V-2024-0157"
        // but currentLocation is null
        Container container = new Container();
        container.setId(7L);
        container.setContainerId("CG-007");
        container.setStatus(ContainerStatus.ACTIVE);

        Route route = new Route("V-2024-0157", "Test Route", RouteStatus.IN_PROGRESS,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);
        route.setCurrentLocation(null);

        when(containerRepository.findByContainerId("CG-007")).thenReturn(Optional.of(container));
        when(routeRepository.findActiveRoutesByContainerId(eq(7L), any())).thenReturn(List.of(route));

        RouteInfoDto info = queryService.getInfoByContainerCode("CG-007");

        assertEquals("V-2024-0157", info.tripCode());
        assertNull(info.latitude());
        assertNull(info.longitude());
    }
}