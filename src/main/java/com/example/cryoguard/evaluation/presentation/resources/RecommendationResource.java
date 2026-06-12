package com.example.cryoguard.evaluation.presentation.resources;

/**
 * RecommendationResource - IA recommendations DTO for dashboard.
 *
 * Used by US05: GET /api/v1/dashboard/ia/recomendaciones
 *
 * Rule-based recommendations derived from alert patterns.
 * Maximum 5 recommendations per response.
 *
 * Fields:
 * - titulo: String (e.g. "Mantenimiento Preventivo: CG-047")
 * - descripcion: String (detailed explanation)
 * - prioridad: String ("alta" or "media")
 * - confianza: int (0-100, capped at 95)
 *
 * T2.5 - Create RecommendationResource
 */
public record RecommendationResource(
    String titulo,
    String descripcion,
    String prioridad,
    int confianza
) {
}