package com.example.cryoguard.evaluation.application;

import com.example.cryoguard.evaluation.infrastructure.persistence.AlertRepository;
import com.example.cryoguard.evaluation.presentation.resources.RecommendationResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Tests for IaMetricsService - synthetic IA metrics for precision and recommendations.
 * T2.15/T2.16 - Create IaMetricsService for IA precision and recommendations
 *
 * SYNTHETIC: academic project, no real ML model. Rule-based pattern matching for demonstration only.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IaMetricsServiceTest {

    @Mock
    private AlertRepository alertRepository;

    private IaMetricsService service;

    @BeforeEach
    void setUp() {
        service = new IaMetricsService(alertRepository);
    }

    // ========== IA Precision Tests ==========

    @Test
    @DisplayName("should return 80 percent precision when 8 of 10 confirmed")
    void shouldReturn80PercentPrecisionWhen8Of10Confirmed() {
        // GIVEN 10 total alerts, 8 confirmed (resolved=true)
        when(alertRepository.count()).thenReturn(10L);
        when(alertRepository.countByResolvedTrue()).thenReturn(8L);

        // WHEN computing precision
        int precision = service.computePrecision();

        // THEN precision should be 80
        assertEquals(80, precision);
    }

    @Test
    @DisplayName("should return 0 when no alerts exist")
    void shouldReturn0WhenNoAlertsExist() {
        // GIVEN no alerts exist
        when(alertRepository.count()).thenReturn(0L);
        when(alertRepository.countByResolvedTrue()).thenReturn(0L);

        // WHEN computing precision
        int precision = service.computePrecision();

        // THEN precision should be 0
        assertEquals(0, precision);
    }

    @Test
    @DisplayName("should return 100 when all alerts confirmed")
    void shouldReturn100WhenAllAlertsConfirmed() {
        // GIVEN 5 total alerts, all 5 confirmed
        when(alertRepository.count()).thenReturn(5L);
        when(alertRepository.countByResolvedTrue()).thenReturn(5L);

        // WHEN computing precision
        int precision = service.computePrecision();

        // THEN precision should be 100
        assertEquals(100, precision);
    }

    @Test
    @DisplayName("should return 0 when all alerts unconfirmed")
    void shouldReturn0WhenAllAlertsUnconfirmed() {
        // GIVEN 5 total alerts, 0 confirmed
        when(alertRepository.count()).thenReturn(5L);
        when(alertRepository.countByResolvedTrue()).thenReturn(0L);

        // WHEN computing precision
        int precision = service.computePrecision();

        // THEN precision should be 0
        assertEquals(0, precision);
    }

    // ========== IA Recommendations Tests ==========

    @Test
    @DisplayName("should return maintenance recommendation for container with 4 alerts")
    void shouldReturnMaintenanceRecommendationForContainerWith4Alerts() {
        // GIVEN container CG-047 has 4 active alerts in last 7 days
        when(alertRepository.countByContainerIdAndTimestampAfter(eq(47L), any(LocalDateTime.class)))
            .thenReturn(4L);
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(4L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(0L);
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class))).thenReturn(0L);
        // Stub other container IDs to return 0 to avoid null issues
        for (long id : new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99}) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(id), any(LocalDateTime.class)))
                .thenReturn(0L);
        }

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN should return 1 recommendation
        assertEquals(1, recommendations.size());
        RecommendationResource rec = recommendations.get(0);
        assertTrue(rec.titulo().contains("CG-047"));
        assertEquals("alta", rec.prioridad());
        assertEquals(95, rec.confianza()); // min(95, 50 + 4*15) = min(95, 110) = 95
    }

    @Test
    @DisplayName("should return night operations recommendation when 8 night alerts")
    void shouldReturnNightOperationsRecommendationWhen8NightAlerts() {
        // GIVEN 8 alerts occurred during night hours (22:00-05:59) in last 7 days
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class)))
            .thenReturn(8L);
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(10L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(0L);
        // Stub all container IDs to return 0
        for (long id : new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 47, 99}) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(id), any(LocalDateTime.class)))
                .thenReturn(0L);
        }

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN should return 1 recommendation for night operations
        assertEquals(1, recommendations.size());
        RecommendationResource rec = recommendations.get(0);
        assertTrue(rec.titulo().contains("Horarios Diurnos"));
        assertEquals("media", rec.prioridad());
        assertEquals(90, rec.confianza()); // 50 + 8*5 = 90
    }

    @Test
    @DisplayName("should return empty array when no patterns match")
    void shouldReturnEmptyArrayWhenNoPatternsMatch() {
        // GIVEN no alerts in last 7 days
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(0L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(0L);
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class))).thenReturn(0L);
        // Stub all container IDs to return 0
        for (long id : new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 47, 99}) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(id), any(LocalDateTime.class)))
                .thenReturn(0L);
        }

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN should return empty array
        assertTrue(recommendations.isEmpty());
    }

    @Test
    @DisplayName("should limit recommendations to maximum 5")
    void shouldLimitRecommendationsToMaximum5() {
        // GIVEN 10 containers each have 3+ alerts (triggering Rule 1 multiple times)
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(30L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(0L);
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class)))
            .thenReturn(0L);

        // Simulate 10 containers with 3+ alerts each
        for (long i = 1; i <= 10; i++) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(i), any(LocalDateTime.class)))
                .thenReturn(3L);
        }
        // Stub remaining container IDs
        when(alertRepository.countByContainerIdAndTimestampAfter(eq(47L), any(LocalDateTime.class)))
            .thenReturn(3L);
        when(alertRepository.countByContainerIdAndTimestampAfter(eq(99L), any(LocalDateTime.class)))
            .thenReturn(3L);

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN should return at most 5 recommendations
        assertTrue(recommendations.size() <= 5);
    }

    @Test
    @DisplayName("should return multiple recommendations when multiple rules match")
    void shouldReturnMultipleRecommendationsWhenMultipleRulesMatch() {
        // GIVEN container CG-047 has 5 alerts, 10 night alerts, 6 unconfirmed older than 48h
        when(alertRepository.countByContainerIdAndTimestampAfter(eq(47L), any(LocalDateTime.class)))
            .thenReturn(5L);
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class)))
            .thenReturn(10L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(6L);
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(20L);
        // Stub other container IDs to return 0
        for (long id : new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99}) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(id), any(LocalDateTime.class)))
                .thenReturn(0L);
        }

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN should return 3 recommendations (one per rule)
        assertEquals(3, recommendations.size());
    }

    @Test
    @DisplayName("should cap confianza at 95")
    void shouldCapConfianzaAt95() {
        // GIVEN container has 10 alerts (would give 50 + 10*15 = 200, capped at 95)
        when(alertRepository.countByContainerIdAndTimestampAfter(eq(47L), any(LocalDateTime.class)))
            .thenReturn(10L);
        when(alertRepository.countByTimestampAfter(any(LocalDateTime.class))).thenReturn(10L);
        when(alertRepository.countUnconfirmedOlderThanHours(any(LocalDateTime.class))).thenReturn(0L);
        when(alertRepository.countAlertsByHourRange(anyInt(), anyInt(), any(LocalDateTime.class)))
            .thenReturn(0L);
        // Stub other container IDs to return 0
        for (long id : new long[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 99}) {
            when(alertRepository.countByContainerIdAndTimestampAfter(eq(id), any(LocalDateTime.class)))
                .thenReturn(0L);
        }

        // WHEN computing recommendations
        List<RecommendationResource> recommendations = service.computeRecommendations();

        // THEN confianza should be capped at 95
        assertEquals(95, recommendations.get(0).confianza());
    }
}