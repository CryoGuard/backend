package com.example.cryoguard.logistics.domain.commands;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateRouteCommand(
    String name,
    Long containerId,
    String origin,
    String destination,
    BigDecimal distanceKm,
    Integer estimatedDurationMinutes,
    Integer checkpoints,
    LocalDateTime startTime,
    LocalDateTime estimatedArrival,
    Long authorizedOperatorId,
    List<Long> containerIds
) {}