package com.example.cryoguard.logistics.presentation.resources;

/**
 * ViajeResource - compact trip card for dashboard.
 * <p>
 * Used by GET /api/v1/viajes endpoint for dashboard-optimized trip display.
 * </p>
 */
public record ViajeResource(
    String codigo,
    String operador,
    String estado,
    Integer progreso,
    Integer cajasAsignadas,
    String origin,
    String destination
) {
    public static ViajeResource empty(String codigo) {
        return new ViajeResource(codigo, null, null, 0, 0, null, null);
    }
}