package com.example.cryoguard.logistics.presentation.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateRouteResource(
    @NotBlank String name,
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