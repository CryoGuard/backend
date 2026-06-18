package com.example.cryoguard.logistics.domain.valueobjects;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteStatusTest {

    @Test
    void shouldHavePlannedStatusWithCorrectValue() {
        assertEquals("planned", RouteStatus.PLANNED.getValue());
    }

    @Test
    void shouldHaveInProgressStatusWithCorrectValue() {
        assertEquals("in_progress", RouteStatus.IN_PROGRESS.getValue());
    }

    @Test
    void shouldHaveCompletedStatusWithCorrectValue() {
        assertEquals("completed", RouteStatus.COMPLETED.getValue());
    }

    @Test
    void shouldHaveCancelledStatusWithCorrectValue() {
        assertEquals("cancelled", RouteStatus.CANCELLED.getValue());
    }

    @Test
    void shouldHaveAllFourStatuses() {
        RouteStatus[] values = RouteStatus.values();
        assertEquals(4, values.length);
    }

    @Test
    void shouldMapStatusValuesCorrectly() {
        assertEquals("planned", RouteStatus.PLANNED.getValue());
        assertEquals("in_progress", RouteStatus.IN_PROGRESS.getValue());
        assertEquals("completed", RouteStatus.COMPLETED.getValue());
        assertEquals("cancelled", RouteStatus.CANCELLED.getValue());
    }
}
