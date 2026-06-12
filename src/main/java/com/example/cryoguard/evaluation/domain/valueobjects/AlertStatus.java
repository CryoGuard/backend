package com.example.cryoguard.evaluation.domain.valueobjects;

/**
 * AlertStatus - represents the lifecycle state of an alert.
 *
 * Replaced values per cryoguard-vue-backend-mvp design decision:
 * - ACTIVA (default on new alerts) — alert is active and unacknowledged
 * - PENDIENTE — alert has been acknowledged but not resolved
 * - CONFIRMADA — alert has been resolved
 *
 * Note: Alert entity uses Boolean acknowledged/resolved fields (not this enum directly).
 * The AlertAssembler derives status from those Booleans.
 */
public enum AlertStatus {
    ACTIVA,
    PENDIENTE,
    CONFIRMADA
}