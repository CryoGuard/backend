package com.example.cryoguard.logistics.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteStatusTest {

    @Test
    void shouldHaveActiveStatusWithCorrectValue() {
        assertEquals("active", RouteStatus.active.getValue());
    }

    @Test
    void shouldHaveInProgressStatusWithCorrectValue() {
        assertEquals("en_ruta", RouteStatus.IN_PROGRESS.getValue());
    }

    @Test
    void shouldHaveCompletedStatusWithCorrectValue() {
        assertEquals("completed", RouteStatus.completed.getValue());
    }

    @Test
    void shouldHaveCancelledStatusWithCorrectValue() {
        assertEquals("cancelled", RouteStatus.cancelled.getValue());
    }

    @Test
    void shouldHaveInitiatedStatusWithCorrectValue() {
        assertEquals("iniciado", RouteStatus.INITIATED.getValue());
    }

    @Test
    void shouldHaveAllFiveStatuses() {
        RouteStatus[] values = RouteStatus.values();
        assertEquals(5, values.length);
    }

    @Test
    void shouldMapStatusValuesCorrectly() {
        assertEquals("active", RouteStatus.active.getValue());
        assertEquals("iniciado", RouteStatus.INITIATED.getValue());
        assertEquals("en_ruta", RouteStatus.IN_PROGRESS.getValue());
        assertEquals("completed", RouteStatus.completed.getValue());
        assertEquals("cancelled", RouteStatus.cancelled.getValue());
    }
}
