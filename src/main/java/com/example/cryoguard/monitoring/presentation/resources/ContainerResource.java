package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContainerResource {
    private Long id;                // database ID -> Vue: apiId
    private String containerId;     // business ID -> Vue: id
    private String name;            // name -> Vue: nombre
    private String estado;           // status -> Vue: estado (lowercase)
    private BigDecimal temperature;  // currentTemperature -> Vue: temperature
    private BigDecimal humidity;     // currentHumidity -> Vue: humidity
    private Integer batteryLevel;   // batteryLevel -> Vue: batteryLevel
    private Boolean coolingActive;  // new: coolingActive
    private String firmware;         // new: firmwareVersion -> Vue: firmware
    private Boolean locked;         // new: locked
    private Boolean connected;      // derived: lastUpdate < 5 min ago
    private GpsLocationDTO location; // GpsCoordinates -> Vue: location
    private String productType;     // productType -> Vue: productType
    private String deviceId;        // hardware device ID -> Vue: dispositivoId
    private LocalDateTime ultimaSync; // lastUpdate -> Vue: ultimaSync

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GpsLocationDTO {
        private BigDecimal lat;
        private BigDecimal lng;
    }
}
