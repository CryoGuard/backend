package com.example.cryoguard.logistics.presentation.controllers;

import com.example.cryoguard.logistics.application.RouteCommandService;
import com.example.cryoguard.logistics.application.RouteQueryService;
import com.example.cryoguard.logistics.domain.commands.CompleteRouteCommand;
import com.example.cryoguard.logistics.domain.commands.CreateRouteCommand;
import com.example.cryoguard.logistics.domain.commands.RecordRouteLocationCommand;
import com.example.cryoguard.logistics.domain.commands.UpdateRouteCommand;
import com.example.cryoguard.logistics.domain.entities.RouteLocationHistory;
import com.example.cryoguard.logistics.domain.queries.GetActiveRoutesQuery;
import com.example.cryoguard.logistics.domain.queries.GetRouteByIdQuery;
import com.example.cryoguard.logistics.domain.queries.GetRouteHistoryQuery;
import com.example.cryoguard.logistics.domain.queries.GetRoutesByContainerQuery;
import com.example.cryoguard.logistics.domain.valueobjects.RouteStatus;
import com.example.cryoguard.logistics.presentation.assemblers.RouteAssembler;
import com.example.cryoguard.logistics.presentation.assemblers.RouteAssemblerImpl;
import com.example.cryoguard.logistics.presentation.resources.CreateRouteResource;
import com.example.cryoguard.logistics.presentation.resources.RouteLocationResource;
import com.example.cryoguard.logistics.presentation.resources.RouteResource;
import com.example.cryoguard.logistics.presentation.resources.UpdateRouteResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/routes")
@Tag(name = "Routes", description = "Route and logistics management operations")
public class RoutesController {

    private final RouteCommandService commandService;
    private final RouteQueryService queryService;
    private final RouteAssemblerImpl routeAssembler;

    public RoutesController(RouteCommandService commandService, RouteQueryService queryService, RouteAssemblerImpl routeAssembler) {
        this.commandService = commandService;
        this.queryService = queryService;
        this.routeAssembler = routeAssembler;
    }

    @GetMapping
    @Operation(summary = "Get all routes", description = "Retrieves all routes with optional filtering by container ID or status.")
    public ResponseEntity<List<RouteResource>> getRoutes(
            @RequestParam(required = false) Long containerId,
            @RequestParam(required = false) RouteStatus status) {
        List<RouteResource> routes;
        if (containerId != null) {
            routes = routeAssembler.toResourceList(queryService.getByContainer(new GetRoutesByContainerQuery(containerId)));
        } else if (status != null) {
            routes = routeAssembler.toResourceList(queryService.getActiveRoutes(new GetActiveRoutesQuery()));
        } else {
            routes = routeAssembler.toResourceList(queryService.getAllRoutes());
        }
        return ResponseEntity.ok(routes);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get route by ID", description = "Retrieves a specific route by its unique identifier.")
    public ResponseEntity<RouteResource> getRoute(@PathVariable Long id) {
        RouteResource route = routeAssembler.toResource(queryService.getById(new GetRouteByIdQuery(id)));
        return ResponseEntity.ok(route);
    }

    @PostMapping
    @Operation(summary = "Create new route", description = "Creates a new logistics route for container transportation.")
    public ResponseEntity<RouteResource> createRoute(@Valid @RequestBody CreateRouteResource resource) {
        CreateRouteCommand command = RouteAssembler.toCreateCommand(resource);
        RouteResource created = routeAssembler.toResource(commandService.createRoute(command));
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update route", description = "Updates an existing route with new details.")
    public ResponseEntity<RouteResource> updateRoute(@PathVariable Long id, @Valid @RequestBody UpdateRouteResource resource) {
        UpdateRouteCommand command = RouteAssembler.toUpdateCommand(resource);
        RouteResource updated = routeAssembler.toResource(commandService.updateRoute(id, command));
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete route", description = "Removes a route by its unique identifier.")
    public ResponseEntity<Void> deleteRoute(@PathVariable Long id) {
        commandService.deleteRoute(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/complete")
    @Operation(summary = "Complete route", description = "Marks a route as completed.")
    public ResponseEntity<RouteResource> completeRoute(@PathVariable Long id) {
        RouteResource completed = routeAssembler.toResource(commandService.completeRoute(id, new CompleteRouteCommand()));
        return ResponseEntity.ok(completed);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "Start route", description = "Starts a planned route, changing status to IN_PROGRESS.")
    public ResponseEntity<Void> startRoute(@PathVariable Long id) {
        commandService.startRoute(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel route", description = "Cancels a route that is not yet completed.")
    public ResponseEntity<Void> cancelRoute(@PathVariable Long id) {
        commandService.cancelRoute(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/history")
    @Operation(summary = "Get route location history", description = "Retrieves the location history for a specific route.")
    public ResponseEntity<List<RouteLocationResource>> getRouteHistory(@PathVariable Long id) {
        List<RouteLocationResource> history = routeAssembler.toLocationResourceList(queryService.getRouteHistory(new GetRouteHistoryQuery(id)));
        return ResponseEntity.ok(history);
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Record route location", description = "Records a new GPS location point for an active route.")
    public ResponseEntity<RouteLocationResource> recordLocation(@PathVariable Long id, @RequestBody RouteLocationResource resource) {
        RecordRouteLocationCommand command = new RecordRouteLocationCommand(
            resource.timestamp(),
            resource.latitude(),
            resource.longitude(),
            resource.speed(),
            resource.heading()
        );
        RouteLocationHistory recorded = commandService.recordLocation(id, command);
        return ResponseEntity.status(HttpStatus.CREATED).body(routeAssembler.toLocationResource(recorded));
    }
}