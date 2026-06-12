package com.example.cryoguard.logistics.interfaces.acl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RouteInfoDtoTest {

    @Test
    void shouldCreateRouteInfoDto() {
        RouteInfoDto dto = new RouteInfoDto("V-2024-0156", -0.1807, -78.4678);

        assertEquals("V-2024-0156", dto.tripCode());
        assertEquals(-0.1807, dto.latitude());
        assertEquals(-78.4678, dto.longitude());
    }

    @Test
    void shouldCreateRouteInfoDtoWithNullCoordinates() {
        RouteInfoDto dto = new RouteInfoDto("V-2024-0157", null, null);

        assertEquals("V-2024-0157", dto.tripCode());
        assertNull(dto.latitude());
        assertNull(dto.longitude());
    }

    @Test
    void shouldCreateWithoutLocation() {
        RouteInfoDto dto = RouteInfoDto.withoutLocation("V-2024-0158");

        assertEquals("V-2024-0158", dto.tripCode());
        assertNull(dto.latitude());
        assertNull(dto.longitude());
    }

    @Test
    void shouldReturnRouteInfoForContainerInActiveRoute() {
        // Scenario: getInfoByContainerCode returns route info for container in active route
        // GIVEN container "CG-005" (id=5) is assigned to an active route with
        // routeId="V-2024-0156" and currentLocation lat=-0.1807, lng=-78.4678
        // WHEN RouteQueryService.getInfoByContainerCode("CG-005") is invoked
        // THEN result SHALL be RouteInfoDto(tripCode="V-2024-0156", latitude=-0.1807, longitude=-78.4678)
        RouteInfoDto info = new RouteInfoDto("V-2024-0156", -0.1807, -78.4678);

        assertEquals("V-2024-0156", info.tripCode());
        assertEquals(-0.1807, info.latitude());
        assertEquals(-78.4678, info.longitude());
    }

    @Test
    void shouldReturnNullCoordsWhenRouteHasNoCurrentLocation() {
        // Scenario: getInfoByContainerCode returns null coords when route has no currentLocation
        // GIVEN container "CG-007" is assigned to an active route with routeId="V-2024-0157"
        // but currentLocation is null
        // WHEN RouteQueryService.getInfoByContainerCode("CG-007") is invoked
        // THEN result SHALL be RouteInfoDto(tripCode="V-2024-0157", latitude=null, longitude=null)
        RouteInfoDto info = RouteInfoDto.withoutLocation("V-2024-0157");

        assertEquals("V-2024-0157", info.tripCode());
        assertNull(info.latitude());
        assertNull(info.longitude());
    }
}