package com.example.cryoguard.monitoring.presentation.resources;

import java.time.LocalDateTime;

/**
 * A locally-evaluated alert produced by the edge gateway.
 * The edge forwards these in batches to /api/v1/edge/alerts.
 *
 * containerId is the business ID (e.g., "CG-001") — the edge uses this
 * stable identifier, not the numeric DB id.
 */
public record EdgeLocalAlertResource(
    String containerId,
    String alertType,         // TEMPERATURE_HIGH, TEMPERATURE_LOW, HUMIDITY_HIGH, HUMIDITY_LOW, BATTERY_LOW
    String severity,          // CRITICAL, WARNING, INFO (case-insensitive)
    Double measuredValue,
    Double thresholdValue,
    String message,
    LocalDateTime receivedAt  // when the edge detected it
) {}
