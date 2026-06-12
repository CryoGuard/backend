package com.example.cryoguard.integration;

import com.example.cryoguard.iam.interfaces.rest.resources.SignInResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignUpResource;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Login flow integration test.
 * Tests the full authentication flow: sign-up -> login -> JWT auth -> user query.
 *
 * T5.2 - Write SpringBootTest integration test for full login -> JWT -> GET /users flow
 * with computed fields (telefono, pinBloqueado, viajesAsignados, viajesCompletados, ultimaActividad).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LoginFlowIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1";
    }

    @Test
    void should_login_and_get_users_with_computed_fields() throws Exception {
        // STEP 1: Create a test operator via sign-up
        SignUpResource signUpResource = new SignUpResource(
            "testoperator",
            "operator-login@cryoguard.com",
            "password123",
            "OPERATOR",
            "+51987654321"
        );

        ResponseEntity<String> signUpResponse = restTemplate.postForEntity(
            baseUrl() + "/auth/sign-up",
            signUpResource,
            String.class
        );

        assertEquals(HttpStatus.CREATED, signUpResponse.getStatusCode(),
            "Sign-up should create a new user");
        JsonNode signUpBody = objectMapper.readTree(signUpResponse.getBody());
        assertNotNull(signUpBody.get("id"), "Sign-up response should include user id");

        // STEP 2: Login with the created user
        SignInResource signInResource = new SignInResource(
            "operator-login@cryoguard.com",
            "password123"
        );

        ResponseEntity<String> loginResponse = restTemplate.postForEntity(
            baseUrl() + "/auth/login",
            signInResource,
            String.class
        );

        assertEquals(HttpStatus.OK, loginResponse.getStatusCode(),
            "Login should return 200 OK");
        JsonNode loginBody = objectMapper.readTree(loginResponse.getBody());
        String jwtToken = loginBody.get("token").asText();
        JsonNode userNode = loginBody.get("user");

        assertNotNull(jwtToken, "Login response should include JWT token");
        assertNotNull(userNode, "Login response should include user data");

        // STEP 3: Verify computed fields are present in login response
        assertNotNull(userNode.get("telefono"), "User should have telefono field");
        assertEquals("+51987654321", userNode.get("telefono").asText(),
            "Telefono should match sign-up value");
        assertNotNull(userNode.get("pinBloqueado"), "User should have pinBloqueado computed field");
        assertFalse(userNode.get("pinBloqueado").asBoolean(),
            "New user should not have blocked PIN");
        assertNotNull(userNode.get("viajesAsignados"),
            "User should have viajesAsignados computed field");
        assertNotNull(userNode.get("viajesCompletados"),
            "User should have viajesCompletados computed field");
        assertNotNull(userNode.get("ultimaActividad"),
            "User should have ultimaActividad computed field");

        // STEP 4: Use JWT to query /users?role=OPERATOR
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> usersResponse = restTemplate.exchange(
            baseUrl() + "/users?role=OPERATOR",
            HttpMethod.GET,
            requestEntity,
            String.class
        );

        assertEquals(HttpStatus.OK, usersResponse.getStatusCode(),
            "GET /users?role=OPERATOR should return 200");
        JsonNode usersBody = objectMapper.readTree(usersResponse.getBody());
        JsonNode content = usersBody.get("content");
        assertNotNull(content, "Users response should have content array");
        assertTrue(content.isArray(), "Users content should be an array");

        // Find our test operator in the list
        boolean foundTestOperator = false;
        for (JsonNode userItem : content) {
            if ("operator-login@cryoguard.com".equals(userItem.get("email").asText())) {
                foundTestOperator = true;
                assertNotNull(userItem.get("telefono"),
                    "User from list should have telefono field");
                assertNotNull(userItem.get("pinBloqueado"),
                    "User from list should have pinBloqueado field");
                assertNotNull(userItem.get("viajesAsignados"),
                    "User from list should have viajesAsignados field");
                assertNotNull(userItem.get("viajesCompletados"),
                    "User from list should have viajesCompletados field");
                assertNotNull(userItem.get("ultimaActividad"),
                    "User from list should have ultimaActividad field");
                break;
            }
        }
        assertTrue(foundTestOperator, "Test operator should appear in OPERATOR role list");
    }

    @Test
    void should_reject_login_with_invalid_credentials() {
        SignInResource invalidLogin = new SignInResource(
            "nonexistent@cryoguard.com",
            "wrongpassword"
        );

        // Use exchange with try-catch since 401 will throw an exception
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<SignInResource> entity = new HttpEntity<>(invalidLogin, headers);

        HttpStatus statusCode = null;
        try {
            restTemplate.exchange(baseUrl() + "/auth/login", HttpMethod.POST, entity, String.class);
        } catch (HttpClientErrorException e) {
            statusCode = HttpStatus.valueOf(e.getStatusCode().value());
        }

        assertNotNull(statusCode, "Should have received a 401 error response");
        assertTrue(
            statusCode == HttpStatus.UNAUTHORIZED || statusCode == HttpStatus.FORBIDDEN,
            "Invalid credentials should return 401 or 403, got: " + statusCode
        );
    }
}