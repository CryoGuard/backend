package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.evaluation.application.IaMetricsService;
import com.example.cryoguard.evaluation.presentation.resources.RecommendationResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Tests for DashboardIaController - IA metrics endpoints for Vue dashboard.
 *
 * T3.13 - Create DashboardIaController with /precision and /recomendaciones
 * Injects evaluation BC's IaMetricsService to expose IA metrics.
 *
 * SYNTHETIC: academic project, no real ML model.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardIaController Tests")
class DashboardIaControllerTest {

    @Mock
    private IaMetricsService iaMetricsService;

    private DashboardIaController controller;

    @BeforeEach
    void setUp() {
        controller = new DashboardIaController(iaMetricsService);
    }

    @Test
    @DisplayName("should return 80 as string for precision when 8 of 10 confirmed")
    void shouldReturn80PrecisionWhen8Of10Confirmed() {
        // GIVEN IaMetricsService.computePrecision() returns 80
        when(iaMetricsService.computePrecision()).thenReturn(80);

        // WHEN GET /api/v1/dashboard/ia/precision
        ResponseEntity<String> response = controller.getPrecision();

        // THEN response is 200 with body "80"
        assertEquals(200, response.getStatusCode().value());
        assertEquals("80", response.getBody());
    }

    @Test
    @DisplayName("should return 0 when no alerts exist")
    void shouldReturn0PrecisionWhenNoAlerts() {
        // GIVEN no alerts exist
        when(iaMetricsService.computePrecision()).thenReturn(0);

        // WHEN GET /api/v1/dashboard/ia/precision
        ResponseEntity<String> response = controller.getPrecision();

        // THEN response is 200 with body "0"
        assertEquals(200, response.getStatusCode().value());
        assertEquals("0", response.getBody());
    }

    @Test
    @DisplayName("should return 100 when all alerts confirmed")
    void shouldReturn100PrecisionWhenAllConfirmed() {
        // GIVEN all alerts confirmed
        when(iaMetricsService.computePrecision()).thenReturn(100);

        // WHEN GET /api/v1/dashboard/ia/precision
        ResponseEntity<String> response = controller.getPrecision();

        // THEN response is 200 with body "100"
        assertEquals(200, response.getStatusCode().value());
        assertEquals("100", response.getBody());
    }

    @Test
    @DisplayName("should return recommendations list with maintenance recommendation")
    void shouldReturnRecommendationsWithMaintenanceRule() {
        // GIVEN container CG-047 has 4 alerts triggering maintenance recommendation
        RecommendationResource maintenanceRec = new RecommendationResource(
            "Mantenimiento Preventivo: CG-047",
            "Detectados 4 alertas en los últimos 7 días. Recomendamos revisar sistema de enfriamiento.",
            "alta",
            85
        );
        when(iaMetricsService.computeRecommendations())
            .thenReturn(List.of(maintenanceRec));

        // WHEN GET /api/v1/dashboard/ia/recomendaciones
        ResponseEntity<List<RecommendationResource>> response = controller.getRecomendaciones();

        // THEN response is 200 with list containing recommendation
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Mantenimiento Preventivo: CG-047", response.getBody().get(0).titulo());
        assertEquals("alta", response.getBody().get(0).prioridad());
        assertEquals(85, response.getBody().get(0).confianza());
    }

    @Test
    @DisplayName("should return empty list when no patterns match")
    void shouldReturnEmptyListWhenNoPatternsMatch() {
        // GIVEN no alert patterns match
        when(iaMetricsService.computeRecommendations()).thenReturn(List.of());

        // WHEN GET /api/v1/dashboard/ia/recomendaciones
        ResponseEntity<List<RecommendationResource>> response = controller.getRecomendaciones();

        // THEN response is 200 with empty list
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    @DisplayName("should return multiple recommendations when multiple rules match")
    void shouldReturnMultipleRecommendationsWhenMultipleRulesMatch() {
        // GIVEN multiple rules match
        RecommendationResource maintenanceRec = new RecommendationResource(
            "Mantenimiento Preventivo: CG-047",
            "Detectados 5 alertas en los últimos 7 días.",
            "alta",
            95
        );
        RecommendationResource nightRec = new RecommendationResource(
            "Optimización de Rutas: Horarios Diurnos",
            "10 alertas ocurrió durante horario nocturno.",
            "media",
            90
        );
        RecommendationResource unconfirmedRec = new RecommendationResource(
            "Revisión de Alertas Pendientes",
            "6 alertas llevan más de 48 horas sin confirmación.",
            "alta",
            85
        );
        when(iaMetricsService.computeRecommendations())
            .thenReturn(List.of(maintenanceRec, nightRec, unconfirmedRec));

        // WHEN GET /api/v1/dashboard/ia/recomendaciones
        ResponseEntity<List<RecommendationResource>> response = controller.getRecomendaciones();

        // THEN response is 200 with 3 recommendations
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("Mantenimiento Preventivo: CG-047", response.getBody().get(0).titulo());
        assertEquals("Optimización de Rutas: Horarios Diurnos", response.getBody().get(1).titulo());
        assertEquals("Revisión de Alertas Pendientes", response.getBody().get(2).titulo());
    }
}