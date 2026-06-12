package com.example.cryoguard.monitoring.presentation;

import com.example.cryoguard.monitoring.application.DeviceAggregationService;
import com.example.cryoguard.monitoring.presentation.resources.DeviceResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * DevicesController - REST controller for real-time device monitoring.
 * <p>
 * Provides aggregated device data including container info, latest telemetry,
 * route info, and online status for the Vue frontend.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/devices")
@RequiredArgsConstructor
@Tag(name = "Devices", description = "Real-time device monitoring")
public class DevicesController {

    private final DeviceAggregationService deviceAggregationService;

    @GetMapping
    @Operation(summary = "Get all devices", description = "Returns a list of all devices with real-time data including telemetry, location, online status, and active alerts.")
    public ResponseEntity<List<DeviceResource>> getAllDevices() {
        List<DeviceResource> devices = deviceAggregationService.getAllDevices();
        return ResponseEntity.ok(devices);
    }
}
