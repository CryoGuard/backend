package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for edge heartbeat endpoint.
 * The containerId field is optional — if absent, falls back to X-Edge-Id header.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeHeartbeatResource {
    private String containerId;
}