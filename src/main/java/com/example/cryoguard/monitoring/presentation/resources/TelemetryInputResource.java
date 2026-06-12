package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryInputResource {
    private BigDecimal temperature;
    private BigDecimal humidity;
    private BigDecimal vibration;
    private Boolean doorOpen;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer batteryLevel;
    private LocalDateTime timestamp;
    private Boolean coolingActive; // new field
}
