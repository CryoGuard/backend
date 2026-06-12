package com.example.cryoguard.logistics.presentation.controllers;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.presentation.assemblers.RouteAssembler;
import com.example.cryoguard.logistics.presentation.resources.ViajeResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ViajesControllerTest {

    @Mock
    private RouteRepository routeRepository;

    private ViajesController controller;

    @BeforeEach
    void setUp() {
        controller = new ViajesController(routeRepository);
    }

    @Test
    void shouldReturnActiveTripsWithLimit() {
        // Scenario: GET /api/v1/viajes?estado=activo&limit=3 returns active trips for dashboard
        // GIVEN 5 active routes exist
        Route r1 = new Route("V-2024-0001", "Viaje 1", RouteStatus.INITIATED,
                "Quito", "Hospital", BigDecimal.valueOf(25), 30, 8,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L);
        Route r2 = new Route("V-2024-0002", "Viaje 2", RouteStatus.IN_PROGRESS,
                "Guayaquil", "Airport", BigDecimal.valueOf(15), 20, 12,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 2L);
        Route r3 = new Route("V-2024-0003", "Viaje 3", RouteStatus.INITIATED,
                "Cuenca", "Terminal", BigDecimal.valueOf(30), 45, 6,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 3L);

        when(routeRepository.findByStatusIn(List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS)))
                .thenReturn(List.of(r1, r2, r3));

        ResponseEntity<List<ViajeResource>> response = controller.getViajes("activo", 3);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        assertEquals("V-2024-0001", response.getBody().get(0).codigo());
    }

    @Test
    void shouldReturnAllActiveTripsWithoutLimit() {
        Route r1 = new Route("V-2024-0001", "Viaje 1", RouteStatus.INITIATED,
                "Quito", "Hospital", BigDecimal.valueOf(25), 30, 8,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L);
        Route r2 = new Route("V-2024-0002", "Viaje 2", RouteStatus.IN_PROGRESS,
                "Guayaquil", "Airport", BigDecimal.valueOf(15), 20, 12,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 2L);

        when(routeRepository.findByStatusIn(List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS)))
                .thenReturn(List.of(r1, r2));

        ResponseEntity<List<ViajeResource>> response = controller.getViajes("activo", null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
    }

    @Test
    void shouldReturnEmptyArrayWhenNoActiveRoutes() {
        when(routeRepository.findByStatusIn(List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS)))
                .thenReturn(List.of());

        ResponseEntity<List<ViajeResource>> response = controller.getViajes("activo", null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void shouldFilterByInProgressStatus() {
        Route r1 = new Route("V-2024-0001", "Viaje 1", RouteStatus.IN_PROGRESS,
                "Quito", "Hospital", BigDecimal.valueOf(25), 30, 8,
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 1L);

        when(routeRepository.findByStatus(RouteStatus.IN_PROGRESS))
                .thenReturn(List.of(r1));

        ResponseEntity<List<ViajeResource>> response = controller.getViajes("IN_PROGRESS", null);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("en_ruta", response.getBody().get(0).estado());
    }
}