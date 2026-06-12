package com.example.cryoguard.logistics.interfaces.acl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RouteQueryService contract tests.
 * Verifies the interface defines the required methods for cross-BC queries.
 */
class RouteQueryServiceTest {

    @Test
    void shouldHaveGetCodeMethod() throws NoSuchMethodException {
        var method = RouteQueryService.class.getMethod("getCode", Long.class);
        assertEquals(String.class, method.getReturnType());
    }

    @Test
    void shouldHaveGetStatsByOperatorMethod() throws NoSuchMethodException {
        var method = RouteQueryService.class.getMethod("getStatsByOperator", Long.class);
        assertEquals(RouteStatsDto.class, method.getReturnType());
    }

    @Test
    void shouldHaveGetInfoByContainerCodeMethod() throws NoSuchMethodException {
        var method = RouteQueryService.class.getMethod("getInfoByContainerCode", String.class);
        assertEquals(RouteInfoDto.class, method.getReturnType());
    }

    @Test
    void shouldBeInterface() {
        assertTrue(RouteQueryService.class.isInterface());
    }
}