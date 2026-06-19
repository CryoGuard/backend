package com.example.cryoguard.logistics.application;

import com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.infrastructure.persistence.RouteRepository;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * LogisticsContextFacade - cross-BC ACL facade implementing LogisticsQueryService.
 * <p>
 * This component provides IAM BC with access to logistics route statistics.
 * Implements the {@link LogisticsQueryService} interface defined by IAM.
 * </p>
 */
@Component
public class LogisticsContextFacade implements LogisticsQueryService {

    private final RouteRepository routeRepository;

    public LogisticsContextFacade(RouteRepository routeRepository) {
        this.routeRepository = routeRepository;
    }

    @Override
    public RouteStatsDto getStatsByOperator(Long operatorId) {
        long activeCount = routeRepository.countByAuthorizedOperatorIdAndStatusIn(
                operatorId, List.of(RouteStatus.INITIATED, RouteStatus.IN_PROGRESS));
        long completedCount = routeRepository.countByAuthorizedOperatorIdAndStatus(
                operatorId, RouteStatus.completed);
        return new RouteStatsDto((int) activeCount, (int) completedCount);
    }
}