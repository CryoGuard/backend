package com.example.cryoguard.logistics.domain.aggregates;

import com.example.cryoguard.logistics.domain.entities.RouteCheckpoint;
import com.example.cryoguard.logistics.domain.entities.RouteContainerAssignment;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.valueobjects.GpsCoordinates;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "routes")
public class Route extends AuditableAbstractAggregateRoot<Route> {

    @Column(name = "route_id", unique = true, nullable = false)
    private String routeId;

    @Column(nullable = false)
    private String name;

    /**
     * Dropped containerId field.
     * Container assignment is now managed via RouteContainerAssignment entity (many-to-many).
     * Use getContainerAssignments() to access assigned containers.
     */
    @Transient
    private Long containerId; // Kept for backward compatibility during transition, marked @Transient

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RouteStatus status;

    @Column(name = "origin", length = 200)
    private String origin;

    @Column(name = "destination", length = 200)
    private String destination;

    @Column(name = "department", length = 100)
    private String department;

    @Column(name = "distance_km", precision = 10, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "estimated_duration_minutes")
    private Integer estimatedDurationMinutes;

    @Column
    private Integer checkpoints;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Embedded
    private GpsCoordinates currentLocation;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteCheckpoint> checkpointsList = new ArrayList<>();

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteLocationHistory> locationHistory = new ArrayList<>();

    /**
     * Authorized operator ID - references User.id in IAM BC.
     * This field does NOT create a JPA foreign key (no @ManyToOne).
     * Cross-BC reference is managed at the application layer via ACL facades.
     */
    @Column(name = "authorized_operator_id")
    private Long authorizedOperatorId;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RouteContainerAssignment> containerAssignments = new ArrayList<>();

    public Route() {}

    // Constructor for backward compatibility (containerId is not persisted)
    public Route(String routeId, String name, Long containerId, RouteStatus status,
                 String origin, String destination, BigDecimal distanceKm,
                 Integer estimatedDurationMinutes, Integer checkpoints, LocalDateTime startTime,
                 LocalDateTime estimatedArrival) {
        this.routeId = routeId;
        this.name = name;
        this.containerId = containerId; // Stored but not persisted
        this.status = status;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.checkpoints = checkpoints;
        this.startTime = startTime;
        this.estimatedArrival = estimatedArrival;
    }

    // New constructor with authorizedOperatorId (without containerId)
    public Route(String routeId, String name, RouteStatus status,
                 String origin, String destination, BigDecimal distanceKm,
                 Integer estimatedDurationMinutes, Integer checkpoints, LocalDateTime startTime,
                 LocalDateTime estimatedArrival, Long authorizedOperatorId) {
        this.routeId = routeId;
        this.name = name;
        this.status = status;
        this.origin = origin;
        this.destination = destination;
        this.distanceKm = distanceKm;
        this.estimatedDurationMinutes = estimatedDurationMinutes;
        this.checkpoints = checkpoints;
        this.startTime = startTime;
        this.estimatedArrival = estimatedArrival;
        this.authorizedOperatorId = authorizedOperatorId;
    }

    public String getRouteId() { return routeId; }
    public void setRouteId(String routeId) { this.routeId = routeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // containerId getter kept for backward compatibility (returns stored value, not from DB)
    public Long getContainerId() { return containerId; }
    public void setContainerId(Long containerId) { this.containerId = containerId; }

    public RouteStatus getStatus() { return status; }
    public void setStatus(RouteStatus status) { this.status = status; }

    public String getOrigin() { return origin; }
    public void setOrigin(String origin) { this.origin = origin; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public BigDecimal getDistanceKm() { return distanceKm; }
    public void setDistanceKm(BigDecimal distanceKm) { this.distanceKm = distanceKm; }

    public Integer getEstimatedDurationMinutes() { return estimatedDurationMinutes; }
    public void setEstimatedDurationMinutes(Integer estimatedDurationMinutes) { this.estimatedDurationMinutes = estimatedDurationMinutes; }

    public Integer getCheckpoints() { return checkpoints; }
    public void setCheckpoints(Integer checkpoints) { this.checkpoints = checkpoints; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEstimatedArrival() { return estimatedArrival; }
    public void setEstimatedArrival(LocalDateTime estimatedArrival) { this.estimatedArrival = estimatedArrival; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public GpsCoordinates getCurrentLocation() { return currentLocation; }
    public void setCurrentLocation(GpsCoordinates currentLocation) { this.currentLocation = currentLocation; }

    public List<RouteCheckpoint> getCheckpointsList() { return checkpointsList; }
    public void setCheckpointsList(List<RouteCheckpoint> checkpointsList) { this.checkpointsList = checkpointsList; }

    public List<RouteLocationHistory> getLocationHistory() { return locationHistory; }
    public void setLocationHistory(List<RouteLocationHistory> locationHistory) { this.locationHistory = locationHistory; }

    public Long getAuthorizedOperatorId() { return authorizedOperatorId; }
    public void setAuthorizedOperatorId(Long authorizedOperatorId) { this.authorizedOperatorId = authorizedOperatorId; }

    public List<RouteContainerAssignment> getContainerAssignments() { return containerAssignments; }
    public void setContainerAssignments(List<RouteContainerAssignment> containerAssignments) { this.containerAssignments = containerAssignments; }

    public void addCheckpoint(RouteCheckpoint checkpoint) {
        checkpointsList.add(checkpoint);
        checkpoint.setRoute(this);
    }

    public void addLocationHistory(RouteLocationHistory location) {
        locationHistory.add(location);
        location.setRoute(this);
    }

    public void addContainerAssignment(RouteContainerAssignment assignment) {
        containerAssignments.add(assignment);
        assignment.setRoute(this);
    }

    public void complete() {
        this.status = RouteStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }
}