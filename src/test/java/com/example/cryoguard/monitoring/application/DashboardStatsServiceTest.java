package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.monitoring.application.DashboardStatsService;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

/**
 * Tests for DashboardStatsService.
 * T3.12 - Create DashboardStatsService
 */
@ExtendWith(MockitoExtension.class)
class DashboardStatsServiceTest {

    @Mock
    private ContainerRepository containerRepository;

    private DashboardStatsService service;

    @BeforeEach
    void setUp() {
        service = new DashboardStatsService(containerRepository);
    }

    @Test
    void shouldReturnCajasIoTFromRepository() {
        // GIVEN container counts
        when(containerRepository.count()).thenReturn(20L);
        when(containerRepository.countByLastUpdateAfter(any(LocalDateTime.class))).thenReturn(15L);

        // WHEN calling getStats
        DashboardStatsService.DashboardStats stats = service.getStats();

        // THEN cajasIoT should be populated from repository
        assertNotNull(stats);
        assertEquals(20, stats.cajasIoT().total());
        assertEquals(15, stats.cajasIoT().conectadas());
    }

    @Test
    void shouldReturnCajasSubtexto() {
        // GIVEN container counts
        when(containerRepository.count()).thenReturn(10L);
        when(containerRepository.countByLastUpdateAfter(any(LocalDateTime.class))).thenReturn(7L);

        // WHEN calling getStats
        DashboardStatsService.DashboardStats stats = service.getStats();

        // THEN subtext should be populated
        assertNotNull(stats.cajasSubtexto());
        assertEquals("7 conectadas", stats.cajasSubtexto());
    }

    @Test
    void shouldHandleZeroCounts() {
        // GIVEN all zero counts
        when(containerRepository.count()).thenReturn(0L);
        when(containerRepository.countByLastUpdateAfter(any(LocalDateTime.class))).thenReturn(0L);

        // WHEN calling getStats
        DashboardStatsService.DashboardStats stats = service.getStats();

        // THEN should return zeros without errors
        assertEquals(0, stats.cajasIoT().total());
        assertEquals(0, stats.cajasIoT().conectadas());
    }

    @Test
    void shouldHaveAllFourKpiBlocks() {
        // GIVEN container counts
        when(containerRepository.count()).thenReturn(5L);
        when(containerRepository.countByLastUpdateAfter(any(LocalDateTime.class))).thenReturn(3L);

        // WHEN calling getStats
        DashboardStatsService.DashboardStats stats = service.getStats();

        // THEN all 4 KPI blocks should be present
        assertNotNull(stats.operadoresActivos());
        assertNotNull(stats.operadoresSubtexto());
        assertNotNull(stats.cajasIoT());
        assertNotNull(stats.cajasSubtexto());
        assertNotNull(stats.viajesActivos());
        assertNotNull(stats.viajesSubtexto());
        assertNotNull(stats.alertasActivas());
        assertNotNull(stats.alertasSubtexto());
    }
}
