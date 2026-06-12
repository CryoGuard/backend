package com.example.cryoguard.logistics.interfaces.acl;

/**
 * RouteStatsDto - data transfer object for route statistics per operator.
 * <p>
 * Used by cross-BC queries (IAM BC consuming logistics).
 * Contains active and completed trip counts for a given operator.
 * </p>
 */
public record RouteStatsDto(int activeCount, int completedCount) {

    public static RouteStatsDto zero() {
        return new RouteStatsDto(0, 0);
    }
}