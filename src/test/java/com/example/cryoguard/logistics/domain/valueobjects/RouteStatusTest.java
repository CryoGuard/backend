package com.example.cryoguard.logistics.domain.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteStatusTest {

    @Test
    void shouldHaveInitiatedStatusWithSpanishValue() {
        assertEquals("iniciado", RouteStatus.INITIATED.getValue());
    }

    @Test
    void shouldHaveInProgressStatusWithSpanishValue() {
        assertEquals("en_ruta", RouteStatus.IN_PROGRESS.getValue());
    }

    @Test
    void shouldPreserveExistingActiveStatus() {
        assertEquals("active", RouteStatus.active.getValue());
    }

    @Test
    void shouldPreserveExistingCompletedStatus() {
        assertEquals("completed", RouteStatus.completed.getValue());
    }

    @Test
    void shouldPreserveExistingCancelledStatus() {
        assertEquals("cancelled", RouteStatus.cancelled.getValue());
    }

    @Test
    void shouldHaveAllFiveStatuses() {
        RouteStatus[] values = RouteStatus.values();
        assertEquals(5, values.length);
    }

    @Test
    void shouldMapInitiatedStatusValueInResponse() {
        // Scenario: RouteStatus enum maps to correct Spanish values
        // GIVEN routes with statuses INITIATED, IN_PROGRESS, COMPLETED, CANCELLED
        // WHEN client requests GET /api/v1/routes
        // THEN status values in responses SHALL be: INITIATED→"iniciado", IN_PROGRESS→"en_ruta"
        assertEquals("iniciado", RouteStatus.INITIATED.getValue());
        assertEquals("en_ruta", RouteStatus.IN_PROGRESS.getValue());
    }
}