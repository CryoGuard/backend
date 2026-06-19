package com.example.cryoguard.monitoring.application;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.commands.CreateContainerCommand;
import com.example.cryoguard.monitoring.domain.commands.SyncContainerCommand;
import com.example.cryoguard.monitoring.domain.entities.TelemetryReading;

import com.example.cryoguard.monitoring.domain.commands.UpdateContainerTelemetryCommand;
import java.time.LocalDateTime;
import java.util.List;

public interface ContainerCommandService {
    Container createContainer(CreateContainerCommand command);
    Container updateContainer(Long id, CreateContainerCommand command);
    void deleteContainer(Long id);
    TelemetryReading recordTelemetry(UpdateContainerTelemetryCommand command);
    Container syncContainer(SyncContainerCommand command);
    Container resetApiKey(Long id);
    Container recordHeartbeat(String containerId, LocalDateTime seenAt);
}