package com.example.cryoguard.logistics.interfaces.acl;

/**
 * RouteInfoDto - data transfer object for route info by container code.
 * <p>
 * Used by cross-BC queries (monitoring BC consuming logistics).
 * Contains trip code and current location coordinates for a container's active route.
 * </p>
 */
public record RouteInfoDto(String tripCode, Double latitude, Double longitude) {

    public static RouteInfoDto withoutLocation(String tripCode) {
        return new RouteInfoDto(tripCode, null, null);
    }
}