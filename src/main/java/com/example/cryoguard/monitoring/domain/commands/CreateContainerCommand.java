package com.example.cryoguard.monitoring.domain.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContainerCommand {
    private String containerId;
    private String name;
    private String deviceId;
    private String productType;
    private Long operatorId;
    private BigDecimal temperatureMin;
    private BigDecimal temperatureMax;
    private BigDecimal humidityMin;
    private BigDecimal humidityMax;
    private String firmwareVersion; // new field
    private Boolean locked;           // new field
}
