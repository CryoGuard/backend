package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EdgeTelemetryResource {
    private String containerId;
    private BigDecimal temperature;
    private BigDecimal humidity;
    private Integer batteryLevel;
    private LocalDateTime timestamp;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Boolean coolingActive;
}
