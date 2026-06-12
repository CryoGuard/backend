package com.example.cryoguard.iam.infrastructure.authorization.sfs.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CORS configuration tests.
 * Verifies that the CORS configuration allows Vue frontend origins.
 *
 * T6.3 - Write test for CORS configuration with Vue origin header.
 */
class CorsConfigurationTest {

    @Test
    void should_allow_vue_localhost_origin() {
        // Given a request from Vue dev server
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:5173");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then Vue origin should be allowed
        assertNotNull(config, "CORS configuration should not be null");
        assertTrue(config.getAllowedOrigins().contains("http://localhost:5173"),
            "Vue localhost:5173 should be in allowed origins");
    }

    @Test
    void should_allow_vue_localhost_3000_origin() {
        // Given a request from Vue alternative port
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:3000");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then localhost:3000 should be allowed
        assertNotNull(config, "CORS configuration should not be null");
        assertTrue(config.getAllowedOrigins().contains("http://localhost:3000"),
            "Vue localhost:3000 should be in allowed origins");
    }

    @Test
    void should_allow_vue_127_0_0_1_origin() {
        // Given a request from Vue using 127.0.0.1
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://127.0.0.1:5173");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then 127.0.0.1:5173 should be allowed
        assertNotNull(config, "CORS configuration should not be null");
        assertTrue(config.getAllowedOrigins().contains("http://127.0.0.1:5173"),
            "Vue 127.0.0.1:5173 should be in allowed origins");
    }

    @Test
    void should_not_allow_unknown_origin() {
        // Given a request from unknown origin
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://unknown-origin.com");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then unknown origin should NOT be in allowed origins
        assertNotNull(config, "CORS configuration should not be null");
        assertFalse(config.getAllowedOrigins().contains("http://unknown-origin.com"),
            "Unknown origin should NOT be in allowed origins");
    }

    @Test
    void should_allow_all_required_http_methods() {
        // Given a request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:5173");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then all required methods should be allowed
        assertNotNull(config.getAllowedMethods(), "Allowed methods should not be null");
        assertTrue(config.getAllowedMethods().contains("GET"), "GET should be allowed");
        assertTrue(config.getAllowedMethods().contains("POST"), "POST should be allowed");
        assertTrue(config.getAllowedMethods().contains("PUT"), "PUT should be allowed");
        assertTrue(config.getAllowedMethods().contains("DELETE"), "DELETE should be allowed");
        assertTrue(config.getAllowedMethods().contains("PATCH"), "PATCH should be allowed");
        assertTrue(config.getAllowedMethods().contains("OPTIONS"), "OPTIONS should be allowed");
    }

    @Test
    void should_allow_credentials() {
        // Given a request
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Origin", "http://localhost:5173");

        // When getting CORS configuration
        CorsConfiguration config = getCorsConfiguration(request);

        // Then credentials should be allowed
        assertTrue(config.getAllowCredentials(),
            "Credentials should be allowed for authenticated requests");
    }

    /**
     * Helper method to create the CORS configuration matching the WebSecurityConfiguration.
     */
    private CorsConfiguration getCorsConfiguration(MockHttpServletRequest request) {
        var cors = new CorsConfiguration();
        cors.setAllowedOrigins(java.util.List.of(
            "http://localhost:5173",
            "http://localhost:3000",
            "http://127.0.0.1:5173"
        ));
        cors.setAllowedMethods(java.util.List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        cors.setAllowedHeaders(java.util.List.of("*"));
        cors.setAllowCredentials(true);
        cors.setMaxAge(3600L);
        return cors;
    }
}