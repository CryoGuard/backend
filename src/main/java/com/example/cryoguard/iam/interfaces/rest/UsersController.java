package com.example.cryoguard.iam.interfaces.rest;

import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.CreateOperatorCommand;
import com.example.cryoguard.iam.domain.model.commands.ResetUserPinCommand;
import com.example.cryoguard.iam.domain.model.commands.SignUpCommand;
import com.example.cryoguard.iam.domain.model.commands.UpdateUserCommand;
import com.example.cryoguard.iam.domain.model.queries.GetAllUsersQuery;
import com.example.cryoguard.iam.domain.model.queries.GetUserByIdQuery;
import com.example.cryoguard.iam.domain.services.ResetUserPinCommandService;
import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.domain.services.UserQueryService;
import com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService;
import com.example.cryoguard.iam.interfaces.rest.resources.CreateOperatorResource;
import com.example.cryoguard.iam.interfaces.rest.resources.CreateOperatorResponseResource;
import com.example.cryoguard.iam.interfaces.rest.resources.ResetPinResponseResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignUpResource;
import com.example.cryoguard.iam.interfaces.rest.resources.UpdateUserResource;
import com.example.cryoguard.iam.interfaces.rest.resources.UserResource;
import com.example.cryoguard.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.example.cryoguard.iam.interfaces.rest.transform.UpdateUserCommandFromResourceAssembler;
import com.example.cryoguard.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * UsersController
 * <p>
 *     This controller is responsible for handling user management requests.
 *     It exposes CRUD endpoints for users.
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Users", description = "User Management Endpoints")
public class UsersController {
    private final UserCommandService userCommandService;
    private final UserQueryService userQueryService;
    private final ResetUserPinCommandService resetUserPinCommandService;
    private final LogisticsQueryService logisticsQueryService;

    public UsersController(UserCommandService userCommandService, UserQueryService userQueryService, ResetUserPinCommandService resetUserPinCommandService, LogisticsQueryService logisticsQueryService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.resetUserPinCommandService = resetUserPinCommandService;
        this.logisticsQueryService = logisticsQueryService;
    }

    /**
     * Get all users with pagination, search, and filtering
     * @param search optional search term for name or email
     * @param role optional role filter (lowercase)
     * @param status optional status filter (lowercase)
     * @param page page number (0-indexed, default 0)
     * @param size page size (default 20)
     * @return paginated list of users
     */
    @GetMapping
    @Operation(summary = "Get all users", description = "Get all users in the system with pagination, search, and filtering.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<Page<UserResource>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var getAllUsersQuery = new GetAllUsersQuery(search, role, status, page, size);
        var usersPage = userQueryService.handle(getAllUsersQuery);
        var userResources = usersPage.map(UserResourceFromEntityAssembler::toResourceFromEntity);
        return ResponseEntity.ok(userResources);
    }

    /**
     * Get user by id
     * @param userId the user id
     * @return the user resource
     */
    @GetMapping(value = "/{userId}")
    @Operation(summary = "Get user by id", description = "Get the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<UserResource> getUserById(@PathVariable Long userId) {
        var getUserByIdQuery = new GetUserByIdQuery(userId);
        var user = userQueryService.handle(getUserByIdQuery);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * Create a new user (POST /users)
     * @param signUpResource the user creation request
     * @return the created user resource
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create user", description = "Create a new user in the system.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<UserResource> createUser(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }

    /**
     * Create a new operator with auto-generated PIN (POST /users/operators)
     * @param resource the operator creation request
     * @return the created user resource and the auto-generated PIN
     */
    @PostMapping(value = "/operators", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create operator with auto-generated PIN",
            description = "Creates a new operator (role=OPERATOR) and returns a unique 4-digit PIN. The PIN is shown to the admin and must be shared with the operator for mobile login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Operator created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<CreateOperatorResponseResource> createOperator(@Valid @RequestBody CreateOperatorResource resource) {
        var command = new CreateOperatorCommand(resource.username(), resource.email(), resource.telefono());
        var result = userCommandService.handle(command);
        if (result.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var user = result.get().getLeft();
        var pin = result.get().getRight();
        RouteStatsDto stats = RouteStatsDto.zero();
        try {
            stats = logisticsQueryService.getStatsByOperator(user.getId());
        } catch (Exception ignored) {}
        UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user, stats);
        return new ResponseEntity<>(new CreateOperatorResponseResource(userResource, pin), HttpStatus.CREATED);
    }

    /**
     * Update user
     * @param userId the user id
     * @param updateUserResource the update request
     * @return the updated user resource
     */
    @PutMapping(value = "/{userId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update user", description = "Update the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<UserResource> updateUser(@PathVariable Long userId, @RequestBody UpdateUserResource updateUserResource) {
        var updateUserCommand = UpdateUserCommandFromResourceAssembler.toCommandFromResource(userId, updateUserResource);
        var updatedUser = userCommandService.handle(updateUserCommand);
        if (updatedUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(updatedUser.get());
        return ResponseEntity.ok(userResource);
    }

    /**
     * Delete (soft) user
     * @param userId the user id
     * @return no content
     */
    @DeleteMapping(value = "/{userId}")
    @Operation(summary = "Delete user", description = "Soft delete the user with the given id.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        var disableUserCommand = new com.example.cryoguard.iam.domain.model.commands.DisableUserCommand(userId);
        userCommandService.handle(disableUserCommand);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reset user PIN (password) to a new random 4-digit value
     * @param userId the user id
     * @return the new plain-text PIN
     */
    @PostMapping(value = "/{userId}/reset-password")
    @Operation(summary = "Reset user PIN", description = "Resets the user's PIN to a new random 4-digit value.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "PIN reset successfully."),
            @ApiResponse(responseCode = "404", description = "User not found."),
            @ApiResponse(responseCode = "401", description = "Unauthorized.")})
    public ResponseEntity<ResetPinResponseResource> resetPassword(@PathVariable Long userId) {
        try {
            var command = new ResetUserPinCommand(userId);
            var newPin = resetUserPinCommandService.handle(command);
            return ResponseEntity.ok(new ResetPinResponseResource(newPin));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}