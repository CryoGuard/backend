package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response DTO for edge config endpoint.
 * Returns the static configuration parameters for a container.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeConfigResource {
    private String containerId;
    private String name;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private BigDecimal humidityMin;
    private BigDecimal humidityMax;
    private String firmwareVersion;
}