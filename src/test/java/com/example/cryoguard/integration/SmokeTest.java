package com.example.cryoguard.integration;

import com.example.cryoguard.iam.interfaces.rest.resources.SignInResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignUpResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Smoke test - verifies all new endpoints return valid responses.
 * Tests endpoint shapes and basic response structure.
 *
 * T5.4 - Smoke test: hit all new endpoints, verify response shapes match Vue frontend contract.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmokeTest {

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();
    private String jwtToken;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @BeforeEach
    void setUp() {
        // Create a unique user for this test run
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String email = "smoke-" + uniqueId + "@cryoguard.com";

        // Create test user
        SignUpResource signUpResource = new SignUpResource(
            "smoke-" + uniqueId,
            email,
            "password123",
            "ADMINISTRATOR",
            "+51900000001"
        );

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<SignUpResource> entity = new HttpEntity<>(signUpResource, headers);
            restTemplate.exchange(baseUrl() + "/auth/sign-up", HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            // User might already exist, continue
        }

        // Login to get JWT
        SignInResource signInResource = new SignInResource(email, "password123");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignInResource> entity = new HttpEntity<>(signInResource, headers);

        try {
            ResponseEntity<String> loginResponse = restTemplate.exchange(
                baseUrl() + "/auth/login",
                HttpMethod.POST,
                entity,
                String.class
            );
            if (loginResponse.getStatusCode() == HttpStatus.OK) {
                JsonNode loginBody = objectMapper.readTree(loginResponse.getBody());
                jwtToken = loginBody.get("token").asText();
            }
        } catch (Exception e) {
            jwtToken = null;
        }
    }

    private HttpHeaders authHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (jwtToken != null) {
            headers.setBearerAuth(jwtToken);
        }
        return headers;
    }

    private ResponseEntity<String> get(String path) {
        return restTemplate.exchange(
            baseUrl() + path,
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class
        );
    }

    private ResponseEntity<String> postJson(String path, String jsonBody) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
        return restTemplate.exchange(baseUrl() + path, HttpMethod.POST, entity, String.class);
    }

    /**
     * Perform POST and catch exceptions to allow testing endpoints that may return errors.
     */
    private ResponseEntity<String> tryPost(String path, String jsonBody) {
        try {
            return postJson(path, jsonBody);
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    @Test
    void smoke_test_all_endpoints_return_valid_responses() throws Exception {
        if (jwtToken == null) {
            fail("Could not obtain JWT token for smoke test");
        }

        // Test: GET /api/v1/users?role=OPERATOR
        ResponseEntity<String> usersResponse = get("/users?role=OPERATOR");
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode(), "GET /users should return 200");
        JsonNode usersBody = objectMapper.readTree(usersResponse.getBody());
        assertNotNull(usersBody.get("content"), "Users response should have content array");
        assertTrue(usersBody.get("content").isArray(), "Users content should be an array");

        // Test: GET /api/v1/users/{id} - use first user id from list
        if (usersBody.get("content").size() > 0) {
            Long userId = usersBody.get("content").get(0).get("id").asLong();
            ResponseEntity<String> userResponse = get("/users/" + userId);
            assertEquals(HttpStatus.OK, userResponse.getStatusCode(), "GET /users/{id} should return 200");
            JsonNode userBody = objectMapper.readTree(userResponse.getBody());
            assertNotNull(userBody.get("email"), "User should have email field");
        }

        // Test: POST /api/v1/users (create operator)
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        String createUserJson = "{\"username\":\"newoperator-" + uniqueId + "\",\"email\":\"newoperator-" + uniqueId + "@cryoguard.com\",\"password\":\"password123\",\"role\":\"OPERATOR\",\"telefono\":\"+51987654321\"}";
        ResponseEntity<String> createResponse = postJson("/users", createUserJson);
        assertEquals(HttpStatus.CREATED, createResponse.getStatusCode(), "POST /users should return 201");

        // Test: PUT /api/v1/users/{id} (update user)
        if (usersBody.get("content").size() > 0) {
            Long userId = usersBody.get("content").get(0).get("id").asLong();
            String updateJson = "{\"name\":\"Updated Name\",\"status\":\"ACTIVE\"}";
            HttpHeaders putHeaders = authHeaders();
            putHeaders.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<String> updateResponse = restTemplate.exchange(
                baseUrl() + "/users/" + userId,
                HttpMethod.PUT,
                new HttpEntity<>(updateJson, putHeaders),
                String.class
            );
            assertEquals(HttpStatus.OK, updateResponse.getStatusCode(), "PUT /users/{id} should return 200");
        }

        // Test: POST /api/v1/users/{id}/reset-password
        if (usersBody.get("content").size() > 0) {
            Long userId = usersBody.get("content").get(0).get("id").asLong();
            ResponseEntity<String> resetResponse = tryPost("/users/" + userId + "/reset-password", null);
            assertEquals(HttpStatus.OK, resetResponse.getStatusCode(), "POST /users/{id}/reset-password should return 200");
            JsonNode resetBody = objectMapper.readTree(resetResponse.getBody());
            assertNotNull(resetBody.get("newPin"), "Reset password response should have newPin field");
        }

        // Test: GET /api/v1/containers
        ResponseEntity<String> containersResponse = get("/containers");
        assertEquals(HttpStatus.OK, containersResponse.getStatusCode(), "GET /containers should return 200");
        JsonNode containersBody = objectMapper.readTree(containersResponse.getBody());
        assertNotNull(containersBody.get("content"), "Containers response should have content array");

        // Test: GET /api/v1/devices
        ResponseEntity<String> devicesResponse = get("/devices");
        assertEquals(HttpStatus.OK, devicesResponse.getStatusCode(), "GET /devices should return 200");
        // Devices response is a list, not a page
        JsonNode devicesBody = objectMapper.readTree(devicesResponse.getBody());
        assertTrue(devicesBody.isArray() || devicesBody.has("content"),
            "Devices response should be array or have content");

        // Test: GET /api/v1/dashboard/stats
        ResponseEntity<String> dashboardResponse = get("/dashboard/stats");
        assertEquals(HttpStatus.OK, dashboardResponse.getStatusCode(), "GET /dashboard/stats should return 200");
        JsonNode dashboardBody = objectMapper.readTree(dashboardResponse.getBody());
        assertNotNull(dashboardBody.get("operadoresActivos"), "Dashboard should have operadoresActivos");

        // Test: GET /api/v1/dashboard/ia/precision
        ResponseEntity<String> precisionResponse = get("/dashboard/ia/precision");
        assertEquals(HttpStatus.OK, precisionResponse.getStatusCode(), "GET /dashboard/ia/precision should return 200");
        assertNotNull(precisionResponse.getBody(), "Precision should return body");

        // Test: GET /api/v1/dashboard/ia/recomendaciones
        ResponseEntity<String> recomendacionesResponse = get("/dashboard/ia/recomendaciones");
        assertEquals(HttpStatus.OK, recomendacionesResponse.getStatusCode(), "GET /dashboard/ia/recomendaciones should return 200");
        JsonNode recomendacionesBody = objectMapper.readTree(recomendacionesResponse.getBody());
        assertTrue(recomendacionesBody.isArray(), "Recomendaciones should return array");

        // Test: GET /api/v1/alertas
        ResponseEntity<String> alertasResponse = get("/alertas");
        assertEquals(HttpStatus.OK, alertasResponse.getStatusCode(), "GET /alertas should return 200");

        // Test: GET /api/v1/alertas?limit=3&sort=reciente
        ResponseEntity<String> alertasLimitedResponse = get("/alertas?limit=3&sort=reciente");
        assertEquals(HttpStatus.OK, alertasLimitedResponse.getStatusCode(), "GET /alertas?limit=3&sort=reciente should return 200");

        // Test: GET /api/v1/routes
        ResponseEntity<String> routesResponse = get("/routes");
        assertEquals(HttpStatus.OK, routesResponse.getStatusCode(), "GET /routes should return 200");
        JsonNode routesBody = objectMapper.readTree(routesResponse.getBody());
        assertTrue(routesBody.isArray(), "Routes response should be array");

        // Test: GET /api/v1/viajes?estado=activo&limit=3
        ResponseEntity<String> viajesResponse = get("/viajes?estado=activo&limit=3");
        assertEquals(HttpStatus.OK, viajesResponse.getStatusCode(), "GET /viajes?estado=activo&limit=3 should return 200");

        // Test: POST /api/v1/routes (create route) - may fail validation but should return proper error
        String createRouteJson = "{\"name\":\"Test Route\",\"origin\":\"Lima\",\"destination\":\"Arequipa\"}";
        ResponseEntity<String> createRouteResponse = tryPost("/routes", createRouteJson);
        // Accept either success or validation error response
        assertTrue(
            createRouteResponse.getStatusCode() == HttpStatus.CREATED ||
            createRouteResponse.getStatusCode() == HttpStatus.BAD_REQUEST ||
            createRouteResponse.getStatusCode() == HttpStatus.CONFLICT,
            "POST /routes should return 201, 400, or 409, got: " + createRouteResponse.getStatusCode()
        );

        // Test: POST /api/v1/routes/{id}/complete (if we got a route back)
        if (createRouteResponse.getStatusCode() == HttpStatus.CREATED) {
            JsonNode createdRoute = objectMapper.readTree(createRouteResponse.getBody());
            if (createdRoute.has("id")) {
                Long routeId = createdRoute.get("id").asLong();
                ResponseEntity<String> completeResponse = tryPost("/routes/" + routeId + "/complete", null);
                assertEquals(HttpStatus.OK, completeResponse.getStatusCode(), "POST /routes/{id}/complete should return 200");
            }
        }
    }
}