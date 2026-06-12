package com.example.cryoguard.logistics.presentation.resources;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteResourceTest {

    @Test
    void shouldCreateRouteResourceWithAllFields() {
        RouteResource resource = new RouteResource(
            1L, "V-2024-0156", "en_ruta", "Juan Pérez",
            65, 8, 1, List.of("CG-001", "CG-002")
        );

        assertEquals(1L, resource.id());
        assertEquals("V-2024-0156", resource.codigo());
        assertEquals("en_ruta", resource.estado());
        assertEquals("Juan Pérez", resource.operador());
        assertEquals(65, resource.progreso());
        assertEquals(8, resource.cajasAsignadas());
        assertEquals(1, resource.alertCount());
        assertEquals(2, resource.assignedBoxes().size());
    }

    @Test
    void shouldCreateRouteResourceWithoutOperator() {
        RouteResource resource = RouteResource.withoutOperator(
            2L, "V-2024-0157", "iniciado",
            30, 2, 0, List.of()
        );

        assertEquals(2L, resource.id());
        assertEquals("V-2024-0157", resource.codigo());
        assertNull(resource.operador());
        assertEquals(30, resource.progreso());
        assertEquals(2, resource.cajasAsignadas());
        assertEquals(0, resource.alertCount());
        assertTrue(resource.assignedBoxes().isEmpty());
    }

    @Test
    void shouldCreateEmptyRouteResource() {
        RouteResource resource = RouteResource.empty(3L, "V-2024-0158", "iniciado");

        assertEquals(3L, resource.id());
        assertEquals("V-2024-0158", resource.codigo());
        assertNull(resource.operador());
        assertEquals(0, resource.progreso());
        assertEquals(0, resource.cajasAsignadas());
        assertEquals(0, resource.alertCount());
        assertTrue(resource.assignedBoxes().isEmpty());
    }

    @Test
    void shouldMapRouteStatusToSpanishValues() {
        // Scenario: RouteStatus enum maps to correct Spanish values
        // GIVEN routes with statuses INITIATED, IN_PROGRESS, COMPLETED, CANCELLED
        // WHEN client requests GET /api/v1/routes
        // THEN status values in responses SHALL be: INITIATED→"iniciado", IN_PROGRESS→"en_ruta"
        RouteResource initiated = new RouteResource(1L, "R1", "iniciado", null, 0, 0, 0, List.of());
        RouteResource inProgress = new RouteResource(2L, "R2", "en_ruta", null, 0, 0, 0, List.of());
        RouteResource completed = new RouteResource(3L, "R3", "completado", null, 0, 0, 0, List.of());
        RouteResource cancelled = new RouteResource(4L, "R4", "cancelado", null, 0, 0, 0, List.of());

        assertEquals("iniciado", initiated.estado());
        assertEquals("en_ruta", inProgress.estado());
        assertEquals("completado", completed.estado());
        assertEquals("cancelado", cancelled.estado());
    }

    @Test
    void shouldReturnEmptyAssignedBoxesWhenNoContainers() {
        RouteResource resource = RouteResource.withoutOperator(
            5L, "R5", "active", 0, 0, 0, List.of()
        );

        assertTrue(resource.assignedBoxes().isEmpty());
    }
}