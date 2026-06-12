package com.example.cryoguard.monitoring.domain.aggregates;

import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import com.example.cryoguard.monitoring.domain.valueobjects.GpsCoordinates;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "containers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Container {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "container_id", unique = true, nullable = false)
    private String containerId;

    @Column(nullable = false)
    private String name;

    @Column(name = "device_id")
    private String deviceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContainerStatus status = ContainerStatus.ACTIVE;

    @Column(name = "current_temperature", precision = 5, scale = 2)
    private BigDecimal currentTemperature;

    @Column(name = "current_humidity", precision = 5, scale = 2)
    private BigDecimal currentHumidity;

    @Column(name = "battery_level")
    private Integer batteryLevel;

    @Embedded
    @AttributeOverride(name = "latitude", column = @Column(name = "gps_latitude", precision = 10, scale = 6))
    @AttributeOverride(name = "longitude", column = @Column(name = "gps_longitude", precision = 10, scale = 6))
    private GpsCoordinates currentLocation;

    @Column(name = "last_update")
    private LocalDateTime lastUpdate;

    @Column(name = "product_type")
    private String productType;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "firmware_version")
    private String firmwareVersion;

    @Column
    private Boolean locked;

    @Column(name = "cooling_active")
    private Boolean coolingActive;
}
