package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.monitoring.application.DashboardStatsService;
import com.example.cryoguard.monitoring.application.DashboardStatsService.DashboardStats;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardController - REST controller for dashboard statistics.
 * <p>
 * Provides aggregated KPIs for the Vue frontend dashboard.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "Dashboard statistics and KPIs")
public class DashboardController {

    private final DashboardStatsService dashboardStatsService;

    @GetMapping("/stats")
    @Operation(summary = "Get dashboard statistics", description = "Returns 4 KPI blocks: operadores activos, cajas IoT, viajes activos, alertas activas.")
    public ResponseEntity<DashboardStats> getStats() {
        DashboardStats stats = dashboardStatsService.getStats();
        return ResponseEntity.ok(stats);
    }
}
