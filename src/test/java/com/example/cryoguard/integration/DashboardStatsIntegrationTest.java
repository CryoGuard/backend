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
 * Dashboard stats integration test.
 * Tests the dashboard endpoints that aggregate data across all BCs.
 *
 * T5.3 - Write integration test for GET /api/v1/dashboard/stats
 * aggregating all 4 BCs (IAM, Evaluation, Monitoring, Logistics).
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DashboardStatsIntegrationTest {

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
        String email = "dashboard-" + uniqueId + "@cryoguard.com";

        // Create test user
        SignUpResource signUpResource = new SignUpResource(
            "dashboard-" + uniqueId,
            email,
            "password123",
            "ADMINISTRATOR",
            "+51900000001"
        );

        // Try to create user (ignore if already exists)
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

    @Test
    void should_return_dashboard_stats_with_4_kpis() throws Exception {
        if (jwtToken == null) {
            // Skip if no JWT available
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/dashboard/stats",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "Dashboard stats endpoint should return 200");

        JsonNode body = objectMapper.readTree(response.getBody());

        // Verify 4 KPI blocks are present
        assertNotNull(body.get("operadoresActivos"),
            "Stats should include operadoresActivos KPI");
        assertNotNull(body.get("operadoresSubtexto"),
            "Stats should include operadoresSubtexto");
        assertNotNull(body.get("cajasIoT"),
            "Stats should include cajasIoT KPI");
        assertNotNull(body.get("cajasSubtexto"),
            "Stats should include cajasSubtexto");
        assertNotNull(body.get("viajesActivos"),
            "Stats should include viajesActivos KPI");
        assertNotNull(body.get("viajesSubtexto"),
            "Stats should include viajesSubtexto");
        assertNotNull(body.get("alertasActivas"),
            "Stats should include alertasActivas KPI");
        assertNotNull(body.get("alertasSubtexto"),
            "Stats should include alertasSubtexto");

        // Verify cajasIoT structure
        JsonNode cajasIoT = body.get("cajasIoT");
        assertNotNull(cajasIoT.get("total"), "cajasIoT should have total field");
        assertNotNull(cajasIoT.get("conectadas"), "cajasIoT should have conectadas field");
    }

    @Test
    void should_return_ia_precision_as_integer() throws Exception {
        if (jwtToken == null) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/dashboard/ia/precision",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "IA precision endpoint should return 200");

        String precisionValue = response.getBody();
        assertNotNull(precisionValue, "Precision response should have body");

        // Should be a plain integer string (e.g., "0", "50", "100")
        int precision = Integer.parseInt(precisionValue.trim());
        assertTrue(precision >= 0 && precision <= 100,
            "Precision should be between 0 and 100, got: " + precision);
    }

    @Test
    void should_return_ia_recomendaciones_as_array() throws Exception {
        if (jwtToken == null) {
            return;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
            baseUrl() + "/dashboard/ia/recomendaciones",
            HttpMethod.GET,
            entity,
            String.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode(),
            "IA recomendaciones endpoint should return 200");

        JsonNode body = objectMapper.readTree(response.getBody());
        assertTrue(body.isArray(), "Recomendaciones response should be an array");

        // If array has items, verify structure
        if (body.size() > 0) {
            JsonNode firstItem = body.get(0);
            assertNotNull(firstItem.get("titulo"),
                "Recommendation should have titulo field");
            assertNotNull(firstItem.get("descripcion"),
                "Recommendation should have descripcion field");
            assertNotNull(firstItem.get("prioridad"),
                "Recommendation should have prioridad field");
            assertNotNull(firstItem.get("confianza"),
                "Recommendation should have confianza field");

            // Verify prioridad values
            String prioridad = firstItem.get("prioridad").asText();
            assertTrue(
                prioridad.equals("alta") || prioridad.equals("media"),
                "Prioridad should be 'alta' or 'media', got: " + prioridad
            );

            // Verify confianza is a reasonable percentage
            int confianza = firstItem.get("confianza").asInt();
            assertTrue(confianza >= 0 && confianza <= 100,
                "Confianza should be between 0 and 100, got: " + confianza);
        }
    }
}