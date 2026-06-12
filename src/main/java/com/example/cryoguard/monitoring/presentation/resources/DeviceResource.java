package com.example.cryoguard.monitoring.presentation.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DeviceResource - Vue shape for /devices endpoint.
 * <p>
 * Aggregates container data, latest telemetry, route info, and active alerts
 * into a single real-time device view for the Vue frontend.
 * </p>
 *
 * @param id containerId (String)
 * @param tripCode route code if container is on active route (String, nullable)
 * @param temperature latest temperature reading (BigDecimal, nullable)
 * @param humidity latest humidity reading (BigDecimal, nullable)
 * @param battery battery level percentage (Integer, nullable)
 * @param cooling cooling active status from latest telemetry (Boolean, nullable)
 * @param location GPS location (Location, nullable)
 * @param online derived: last telemetry< 5 min ago (Boolean)
 * @param locked container lock status (Boolean, nullable)
 * @param status derived: "normal" | "warning" | "offline" (String)
 * @param latitude current latitude (BigDecimal, nullable)
 * @param longitude current longitude (BigDecimal, nullable)
 * @param activeAlerts list of active alerts for this container (List<AlertInfo>)
 * @param lastSync last telemetry timestamp (LocalDateTime, nullable)
 * @param temperatureWarning derived: temperature out of acceptable range (Boolean, nullable)
 */
public record DeviceResource(
        String id,
        String tripCode,
        BigDecimal temperature,
        BigDecimal humidity,
        Integer battery,
        Boolean cooling,
        Location location,
        Boolean online,
        Boolean locked,
        String status,
        BigDecimal latitude,
        BigDecimal longitude,
        List<AlertInfo> activeAlerts,
        LocalDateTime lastSync,
        Boolean temperatureWarning
) {

    /**
     * GPS location sub-record.
     */
    public record Location(BigDecimal lat, BigDecimal lng) {}

    /**
     * Alert info sub-record for active alerts list.
     * T3.13 - Updated to use String id to match evaluation BC's AlertSummaryDto.
     */
    public record AlertInfo(String id, String severity, String message) {}
}
