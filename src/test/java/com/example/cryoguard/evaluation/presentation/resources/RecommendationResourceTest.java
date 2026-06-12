package com.example.cryoguard.evaluation.presentation.resources;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RecommendationResource - IA recommendations DTO.
 * T2.5 - Create RecommendationResource: titulo, descripcion, prioridad, confianza
 *
 * Used by US05: GET /api/v1/dashboard/ia/recomendaciones
 *
 * Rule-based recommendations derived from alert patterns.
 * Maximum 5 recommendations per response.
 */
class RecommendationResourceTest {

    @Test
    @DisplayName("should create RecommendationResource with all fields")
    void shouldCreateRecommendationResourceWithAllFields() {
        // GIVEN RecommendationResource
        RecommendationResource resource = new RecommendationResource(
            "Mantenimiento Preventivo: CG-047",
            "Detectados 4 alertas en los últimos 7 días. Recomendamos revisar sistema de enfriamiento.",
            "alta",
            85
        );

        // THEN all fields should be accessible
        assertEquals("Mantenimiento Preventivo: CG-047", resource.titulo());
        assertEquals("Detectados 4 alertas en los últimos 7 días. Recomendamos revisar sistema de enfriamiento.", resource.descripcion());
        assertEquals("alta", resource.prioridad());
        assertEquals(85, resource.confianza());
    }

    @Test
    @DisplayName("should allow media prioridad")
    void shouldAllowMediaPrioridad() {
        // GIVEN RecommendationResource with media prioridad
        RecommendationResource resource = new RecommendationResource(
            "Optimización de Rutas: Horarios Diurnos",
            "8 alertas ocurrió durante horario nocturno (22:00-05:59). Considera ajustar horarios de operación.",
            "media",
            90
        );

        // THEN prioridad should be media
        assertEquals("media", resource.prioridad());
    }

    @Test
    @DisplayName("should allow confianza values 0-100")
    void shouldAllowConfianzaValues() {
        // GIVEN RecommendationResource with various confianza values
        RecommendationResource low = new RecommendationResource(
            "Test baja", "Descripción", "media", 50
        );
        RecommendationResource medium = new RecommendationResource(
            "Test media", "Descripción", "media", 75
        );
        RecommendationResource high = new RecommendationResource(
            "Test alta", "Descripción", "alta", 95
        );

        // THEN confianza should be preserved
        assertEquals(50, low.confianza());
        assertEquals(75, medium.confianza());
        assertEquals(95, high.confianza());
    }

    @Test
    @DisplayName("should allow unconfirmed alerts recommendation")
    void shouldAllowUnconfirmedAlertsRecommendation() {
        // GIVEN RecommendationResource for unconfirmed alerts
        RecommendationResource resource = new RecommendationResource(
            "Revisión de Alertas Pendientes",
            "6 alertas llevan más de 48 horas sin confirmación. Revisa y confirma las alertas resueltas.",
            "alta",
            85
        );

        // THEN all fields should be correct
        assertEquals("Revisión de Alertas Pendientes", resource.titulo());
        assertTrue(resource.descripcion().contains("48 horas"));
        assertEquals("alta", resource.prioridad());
    }

    @Test
    @DisplayName("should allow long titulo")
    void shouldAllowLongTitulo() {
        // GIVEN RecommendationResource with long titulo
        String longTitulo = "Mantenimiento Preventivo: CG-047 - Sistema de Enfriamiento - Revisión Requerida";
        RecommendationResource resource = new RecommendationResource(
            longTitulo, "Descripción", "alta", 80
        );

        // THEN titulo should be preserved
        assertEquals(longTitulo, resource.titulo());
    }

    @Test
    @DisplayName("should allow confidence capped at 95")
    void shouldAllowConfidenceCappedAt95() {
        // GIVEN RecommendationResource with confianza at cap
        RecommendationResource resource = new RecommendationResource(
            "Test cap", "Descripción", "alta", 95
        );

        // THEN confianza should be 95
        assertEquals(95, resource.confianza());
    }
}