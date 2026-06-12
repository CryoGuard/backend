package com.example.cryoguard.monitoring.infrastructure.persistence;

import com.example.cryoguard.monitoring.domain.aggregates.Container;
import com.example.cryoguard.monitoring.domain.valueobjects.ContainerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ContainerRepository extends JpaRepository<Container, Long> {
    Optional<Container> findByContainerId(String containerId);
    boolean existsByContainerId(String containerId);

    Page<Container> findByStatus(ContainerStatus status, Pageable pageable);

    Page<Container> findByProductType(String productType, Pageable pageable);

    Page<Container> findByStatusAndProductType(ContainerStatus status, String productType, Pageable pageable);

    @Query("SELECT c FROM Container c WHERE c.status IS NOT NULL")
    Page<Container> findAllActive(Pageable pageable);

    /**
     * Counts containers with lastUpdate after the given timestamp.
     * Used for dashboard KPIs (connected containers count).
     */
    long countByLastUpdateAfter(LocalDateTime timestamp);
}
