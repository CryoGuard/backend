package com.example.cryoguard.logistics.domain.entities;

import com.example.cryoguard.logistics.domain.aggregates.Route;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * RouteContainerAssignment entity.
 * <p>
 * Represents the many-to-many relationship between Route and Container.
 * This replaces the single-container Route.containerId field.
 * Each assignment records when a container was assigned to a route.
 * </p>
 */
@Entity
@Table(name = "route_container_assignments")
@EntityListeners(AuditingEntityListener.class)
public class RouteContainerAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(name = "container_id", nullable = false)
    private Long containerId;

    @CreatedDate
    @Column(name = "assigned_at", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    public RouteContainerAssignment() {}

    public RouteContainerAssignment(Route route, Long containerId) {
        this.route = route;
        this.containerId = containerId;
        this.assignedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Route getRoute() { return route; }
    public void setRoute(Route route) { this.route = route; }
    public Long getContainerId() { return containerId; }
    public void setContainerId(Long containerId) { this.containerId = containerId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
}