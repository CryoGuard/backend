package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * DashboardStatsService - aggregates 4 KPIs for the dashboard.
 * <p>
 * Produces dashboard statistics including:
 * - operadoresActivos: users created in last 7 days
 * - cajasIoT: total containers and connected count
 * - viajesActivos: active routes + completed today
 * - alertasActivas: unresolved alerts + critical count
 * </p>
 * <p>
 * Note: Cross-BC queries (IAM, Logistics, Evaluation) are accessed via ACL facades.
 * This service uses fallbacks when those facades are not available.
 * </p>
 */
@Service
public class DashboardStatsService {

    private final ContainerRepository containerRepository;

    public DashboardStatsService(ContainerRepository containerRepository) {
        this.containerRepository = containerRepository;
    }

    public DashboardStats getStats() {
        // Cajas IoT (total + conectadas) - from monitoring BC
        long totalCajas = containerRepository.count();
        long conectadas = containerRepository.countByLastUpdateAfter(
                LocalDateTime.now().minusMinutes(5));

        // Note: Cross-BC stats (operadoresActivos, viajesActivos, alertasActivas)
        // require ACL facades from IAM, Logistics, and Evaluation BCs.
        // This implementation provides the monitoring-based KPIs.
        // Full implementation requires T2.15/T2.16 (evaluation) and cross-BC facades.

        long operadoresActivos = 0; // Requires IAM UserRepository.countByCreatedAtAfter
        long viajesActivos = 0;    // Requires Logistics RouteRepository.countByStatusIn
        long alertasActivas = 0;   // Requires Evaluation AlertRepository counts

        // Build subtexts
        String operadoresSubtexto = operadoresActivos > 0 ? "+" + operadoresActivos + " esta semana" : "Sin datos";
        String cajasSubtexto = conectadas + " conectadas";
        String viajesSubtexto = viajesActivos > 0 ? viajesActivos + " activos" : "Sin datos";
        String alertasSubtexto = alertasActivas > 0 ? alertasActivas + " criticas" : "Sin datos";

        return new DashboardStats(
                operadoresActivos,
                operadoresSubtexto,
                new CajasIoT(totalCajas, conectadas),
                cajasSubtexto,
                viajesActivos,
                viajesSubtexto,
                alertasActivas,
                alertasSubtexto
        );
    }

    /**
     * Dashboard statistics record.
     */
    public record DashboardStats(
            long operadoresActivos,
            String operadoresSubtexto,
            CajasIoT cajasIoT,
            String cajasSubtexto,
            long viajesActivos,
            String viajesSubtexto,
            long alertasActivas,
            String alertasSubtexto
    ) {}

    /**
     * Cajas IoT sub-record.
     */
    public record CajasIoT(long total, long conectadas) {}
}
