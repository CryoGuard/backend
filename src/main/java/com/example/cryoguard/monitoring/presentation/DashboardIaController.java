package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.evaluation.application.IaMetricsService;
import com.example.cryoguard.evaluation.presentation.resources.RecommendationResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DashboardIaController - IA metrics endpoints for Vue dashboard.
 * <p>
 * Exposes synthetic IA precision and recommendations derived from alert patterns.
 * Lives in monitoring BC but consumes evaluation BC's IaMetricsService.
 * </p>
 *
 * SYNTHETIC: academic project, no real ML model. Rule-based pattern matching only.
 *
 * T3.13 - Create DashboardIaController with /precision and /recomendaciones
 */
@RestController
@RequestMapping("/api/v1/dashboard/ia")
@RequiredArgsConstructor
@Tag(name = "Dashboard IA", description = "Synthetic IA metrics for dashboard")
public class DashboardIaController {

    private final IaMetricsService iaMetricsService;

    /**
     * Get IA precision metric.
     * Formula: (confirmedCount / totalCount) * 100, rounded to nearest integer.
     * Returns 0 if no alerts exist.
     *
     * @return integer percentage (0-100) as plain text
     */
    @GetMapping("/precision")
    @Operation(summary = "Get IA precision", description = "Returns synthetic IA precision as integer percentage (0-100). Formula: confirmed/total * 100.")
    public ResponseEntity<String> getPrecision() {
        int precision = iaMetricsService.computePrecision();
        return ResponseEntity.ok(String.valueOf(precision));
    }

    /**
     * Get IA recommendations based on alert patterns.
     * Rules:
     * 1. High-Frequency Container: 3+ alerts in 7 days → Mantenimiento Preventivo
     * 2. Night Operations: 3+ alerts 22:00-05:59 → Optimización de Rutas
     * 3. Unconfirmed Accumulation: 5+ alerts >48h unconfirmed → Revisión de Alertas
     *
     * Maximum 5 recommendations per response.
     *
     * @return list of recommendation resources
     */
    @GetMapping("/recomendaciones")
    @Operation(summary = "Get IA recommendations", description = "Returns rule-based recommendations derived from alert patterns. Max 5 results.")
    public ResponseEntity<List<RecommendationResource>> getRecomendaciones() {
        List<RecommendationResource> recommendations = iaMetricsService.computeRecommendations();
        return ResponseEntity.ok(recommendations);
    }
}