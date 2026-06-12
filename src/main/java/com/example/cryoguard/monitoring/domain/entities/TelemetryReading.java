package com.example.cryoguard.monitoring.domain.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry_readings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TelemetryReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(precision = 5, scale = 2)
    private BigDecimal vibration;

    @Column(name = "door_open")
    private Boolean doorOpen;

    @Column(precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Column(name = "cooling_active")
    private Boolean coolingActive;
}
