package com.example.cryoguard.monitoring.interfaces.acl;

/**
 * RouteInfoDto - data transfer object for route info by container code.
 * <p>
 * Used by monitoring BC to get trip code and location for a container's active route.
 * </p>
 */
public record RouteInfoDto(String tripCode, Double latitude, Double longitude) {

    public static RouteInfoDto withoutLocation(String tripCode) {
        return new RouteInfoDto(tripCode, null, null);
    }
}
