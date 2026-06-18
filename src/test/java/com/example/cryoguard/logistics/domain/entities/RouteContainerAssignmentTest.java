package com.example.cryoguard.logistics.domain.entities;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class RouteContainerAssignmentTest {

    @Test
    void shouldCreateAssignmentWithRouteAndContainerId() {
        Route route = new Route("R001", "Test Route", 1L, RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        RouteContainerAssignment assignment = new RouteContainerAssignment(route, 5L);

        assertEquals(route, assignment.getRoute());
        assertEquals(5L, assignment.getContainerId());
    }

    @Test
    void shouldHaveAssignedAtTimestamp() {
        Route route = new Route("R002", "Test Route 2", 2L, RouteStatus.PLANNED,
                "Origin", "Destination", BigDecimal.valueOf(100),
                60, 10, LocalDateTime.now(), LocalDateTime.now().plusHours(1));

        LocalDateTime before = LocalDateTime.now();
        RouteContainerAssignment assignment = new RouteContainerAssignment(route, 10L);
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(assignment.getAssignedAt());
        assertTrue(assignment.getAssignedAt().compareTo(before) >= 0);
        assertTrue(assignment.getAssignedAt().compareTo(after) <= 0);
    }

    @Test
    void shouldDefaultConstructorExist() {
        RouteContainerAssignment assignment = new RouteContainerAssignment();
        assertNull(assignment.getId());
        assertNull(assignment.getRoute());
        assertNull(assignment.getContainerId());
    }
}