package com.example.cryoguard.logistics.presentation.resources;

import java.util.List;

/**
 * RouteResource - Vue-shaped route response for frontend consumption.
 * <p>
 * Replaces the old RouteResource with Vue-compatible field naming.
 * Field mapping: routeId→codigo, status→estado (Spanish values),
 * authorizedOperator.name→operador, computed progreso, cajasAsignadas, alertCount, assignedBoxes.
 * </p>
 */
public record RouteResource(
    Long id,
    String codigo,
    String estado,
    String operador,
    Integer progreso,
    Integer cajasAsignadas,
    Integer alertCount,
    List<String> assignedBoxes
) {
    /**
     * Creates a RouteResource with no operator assigned.
     */
    public static RouteResource withoutOperator(Long id, String codigo, String estado,
                                                  Integer progreso, Integer cajasAsignadas,
                                                  Integer alertCount, List<String> assignedBoxes) {
        return new RouteResource(id, codigo, estado, null, progreso, cajasAsignadas, alertCount, assignedBoxes);
    }

    /**
     * Creates a RouteResource with zero progress and no assigned boxes.
     */
    public static RouteResource empty(Long id, String codigo, String estado) {
        return new RouteResource(id, codigo, estado, null, 0, 0, 0, List.of());
    }
}