package com.example.cryoguard.logistics.domain.aggregates;

import com.example.cryoguard.logistics.domain.entities.RouteContainerAssignment;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteEntityTest {

    @Test
    void shouldHaveAuthorizedOperatorIdField() {
        Route route = new Route("R001", "Test Route", RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), 5L);

        assertEquals(5L, route.getAuthorizedOperatorId());
    }

    @Test
    void shouldHaveContainerAssignmentsCollection() {
        Route route = new Route("R002", "Test Route 2", RouteStatus.IN_PROGRESS,
                "Origin", "Destination", BigDecimal.valueOf(200),
                90, 15, LocalDateTime.now(), LocalDateTime.now().plusHours(2), 3L);

        assertNotNull(route.getContainerAssignments());
        assertTrue(route.getContainerAssignments().isEmpty());
    }

    @Test
    void shouldAddContainerAssignmentToRoute() {
        Route route = new Route("R003", "Test Route 3", RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(150),
                45, 5, LocalDateTime.now(), LocalDateTime.now().plusHours(1), 2L);

        RouteContainerAssignment assignment = new RouteContainerAssignment(route, 10L);
        route.addContainerAssignment(assignment);

        assertEquals(1, route.getContainerAssignments().size());
        assertEquals(10L, route.getContainerAssignments().get(0).getContainerId());
    }

    @Test
    void shouldAllowNullAuthorizedOperatorId() {
        Route route = new Route("R004", "Test Route 4", RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);

        assertNull(route.getAuthorizedOperatorId());
    }

    @Test
    void shouldUseNewConstructorWithoutContainerId() {
        Route route = new Route("R005", "Viaje Quito", RouteStatus.PLANNED,
                "Quito", "Hospital", BigDecimal.valueOf(25.5),
                30, 8, LocalDateTime.now(), LocalDateTime.now().plusHours(1), 7L);

        assertEquals("R005", route.getRouteId());
        assertEquals("Viaje Quito", route.getName());
        assertEquals(7L, route.getAuthorizedOperatorId());
        assertEquals(RouteStatus.PLANNED, route.getStatus());
    }

    @Test
    void shouldHaveProgressField() {
        // progreso is computed from checkpoints, not stored directly
        // But checkpoints field exists
        Route route = new Route("R006", "Test Route", RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L);

        assertEquals(10, route.getCheckpoints());
    }

    @Test
    void shouldMaintainContainerIdForBackwardCompatibility() {
        // Backward compatibility: containerId is stored but @Transient (not persisted)
        Route route = new Route("R007", "Test Route", 1L, RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        assertEquals(1L, route.getContainerId());
    }

    @Test
    void shouldUpdateAuthorizedOperatorId() {
        Route route = new Route("R008", "Test Route", RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1), null);

        route.setAuthorizedOperatorId(99L);

        assertEquals(99L, route.getAuthorizedOperatorId());
    }
}