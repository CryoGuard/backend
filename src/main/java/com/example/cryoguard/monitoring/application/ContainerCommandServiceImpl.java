package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.evaluation.application.EvaluationService;
import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.commands.CreateContainerCommand;
import com.example.cryoguard.monitoring.domain.commands.SyncContainerCommand;
import com.example.cryoguard.monitoring.domain.commands.UpdateContainerTelemetryCommand;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import com.example.cryoguard.monitoring.domain.valueobjects.GpsCoordinates;
import com.example.cryoguard.monitoring.infrastructure.persistence.ContainerRepository;
import com.example.cryoguard.monitoring.infrastructure.persistence.TelemetryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ContainerCommandServiceImpl implements ContainerCommandService {

    private final ContainerRepository containerRepository;
    private final TelemetryRepository telemetryRepository;

    @Autowired
    private EvaluationService evaluationService;

    @Override
    @Transactional
    public Container createContainer(CreateContainerCommand command) {
        Container container = new Container();
        container.setContainerId(command.getContainerId());
        container.setName(command.getName());
        container.setDeviceId(command.getDeviceId());
        container.setProductType(command.getProductType());
        container.setOperatorId(command.getOperatorId());
        container.setStatus(ContainerStatus.ACTIVE);
        container.setBatteryLevel(100);
        // New fields
        if (command.getFirmwareVersion() != null) {
            container.setFirmwareVersion(command.getFirmwareVersion());
        }
        if (command.getLocked() != null) {
            container.setLocked(command.getLocked());
        }
        return containerRepository.save(container);
    }

    @Override
    @Transactional
    public Container updateContainer(Long id, CreateContainerCommand command) {
        Container container = containerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + id));

        if (command.getContainerId() != null) {
            container.setContainerId(command.getContainerId());
        }
        if (command.getName() != null) {
            container.setName(command.getName());
        }
        if (command.getDeviceId() != null) {
            container.setDeviceId(command.getDeviceId());
        }
        if (command.getProductType() != null) {
            container.setProductType(command.getProductType());
        }
        if (command.getOperatorId() != null) {
            container.setOperatorId(command.getOperatorId());
        }
        // New fields
        if (command.getFirmwareVersion() != null) {
            container.setFirmwareVersion(command.getFirmwareVersion());
        }
        if (command.getLocked() != null) {
            container.setLocked(command.getLocked());
        }

        return containerRepository.save(container);
    }

    @Override
    @Transactional
    public void deleteContainer(Long id) {
        if (!containerRepository.existsById(id)) {
            throw new IllegalArgumentException("Container not found: " + id);
        }
        containerRepository.deleteById(id);
    }

    @Override
    @Transactional
    public TelemetryReading recordTelemetry(UpdateContainerTelemetryCommand command) {
        Container container = containerRepository.findById(command.getContainerId())
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + command.getContainerId()));

        TelemetryReading reading = new TelemetryReading();
        reading.setContainerId(command.getContainerId());
        reading.setTimestamp(command.getTimestamp() != null ? command.getTimestamp() : LocalDateTime.now());
        reading.setTemperature(command.getTemperature());
        reading.setHumidity(command.getHumidity());
        reading.setVibration(command.getVibration());
        reading.setDoorOpen(command.getDoorOpen());
        reading.setLatitude(command.getLatitude());
        reading.setLongitude(command.getLongitude());
        reading.setBatteryLevel(command.getBatteryLevel());
        reading.setCoolingActive(command.getCoolingActive());

        // Update container with latest telemetry data
        container.setCurrentTemperature(command.getTemperature());
        container.setCurrentHumidity(command.getHumidity());
        if (command.getLatitude() != null && command.getLongitude() != null) {
            container.setCurrentLocation(new GpsCoordinates(command.getLatitude(), command.getLongitude()));
        }
        container.setBatteryLevel(command.getBatteryLevel());
        container.setLastUpdate(LocalDateTime.now());
        container.setCoolingActive(command.getCoolingActive());

        containerRepository.save(container);
        TelemetryReading savedReading = telemetryRepository.save(reading);

        // Evaluate telemetry for alert rules
        EvaluationService.TelemetryReading evalReading = mapToReading(command);
        evaluationService.evaluateTelemetry(command.getContainerId(), evalReading);

        return savedReading;
    }

    private EvaluationService.TelemetryReading mapToReading(UpdateContainerTelemetryCommand command) {
        EvaluationService.TelemetryReading reading = new EvaluationService.TelemetryReading();
        reading.setContainerId(command.getContainerId());
        reading.setTemperature(command.getTemperature());
        reading.setHumidity(command.getHumidity());
        reading.setVibration(command.getVibration());
        reading.setDoorOpen(command.getDoorOpen());
        // doorOpenDurationMinutes not available in command - will be null
        return reading;
    }

    @Override
    @Transactional
    public Container syncContainer(SyncContainerCommand command) {
        Container container = containerRepository.findById(command.getContainerId())
                .orElseThrow(() -> new IllegalArgumentException("Container not found: " + command.getContainerId()));

        if (command.getLatitude() != null && command.getLongitude() != null) {
            container.setCurrentLocation(new GpsCoordinates(command.getLatitude(), command.getLongitude()));
        }
        container.setLastUpdate(LocalDateTime.now());

        return containerRepository.save(container);
    }
}
