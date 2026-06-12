package com.example.cryoguard.iam.interfaces.acl;

import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for LogisticsQueryService interface in IAM BC.
 * This interface is consumed by IAM to query operator trip statistics from logistics BC.
 */
class LogisticsQueryServiceTest {

    @Test
    void shouldDefineGetStatsByOperatorMethod() throws NoSuchMethodException {
        // GIVEN the LogisticsQueryService interface
        Class<?> iface = LogisticsQueryService.class;

        // THEN it should have getStatsByOperator method returning RouteStatsDto
        var method = iface.getMethod("getStatsByOperator", Long.class);
        assertNotNull(method);
        assertEquals(RouteStatsDto.class, method.getReturnType());
    }

    @Test
    void shouldBeInterface() {
        // THEN LogisticsQueryService should be an interface
        assertTrue(LogisticsQueryService.class.isInterface());
    }
}