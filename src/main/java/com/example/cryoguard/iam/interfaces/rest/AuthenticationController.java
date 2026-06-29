package com.example.cryoguard.iam.interfaces.rest;

import com.example.cryoguard.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.interfaces.rest.resources.LoginResponseResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignInPinResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignInResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignUpResource;
import com.example.cryoguard.iam.interfaces.rest.resources.UserResource;
import com.example.cryoguard.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.example.cryoguard.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.example.cryoguard.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AuthenticationController
 * <p>
 *     This controller is responsible for handling authentication requests.
 *     It exposes two endpoints:
 *     <ul>
 *         <li>POST /api/v1/auth/login</li>
 *         <li>POST /api/v1/auth/sign-up</li>
 *     </ul>
 * </p>
 */
@RestController
@RequestMapping(value = "/api/v1/auth")
@Tag(name = "Authentication", description = "Available Authentication Endpoints")
public class AuthenticationController {
    private final UserCommandService userCommandService;
    private final com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService logisticsQueryService;

    public AuthenticationController(UserCommandService userCommandService,
            @Autowired(required = false) com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService logisticsQueryService) {
        this.userCommandService = userCommandService;
        this.logisticsQueryService = logisticsQueryService;
    }

    /**
     * Handles the login request (POST /auth/login).
     * @param signInResource the sign-in request body with email and password.
     * @return the login response with JWT token and user info.
     */
    @PostMapping(value = "/login", consumes = {"application/json"})
    @Operation(summary = "Login", description = "Authenticate with email and password to receive JWT token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials."),
            @ApiResponse(responseCode = "403", description = "Account locked.")})
    public ResponseEntity<LoginResponseResource> login(@RequestBody SignInResource signInResource) {
        try {
            var signInCommand = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
            var result = userCommandService.handle(signInCommand);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            var user = result.get().getLeft();
            var token = result.get().getRight();

            // Get trip stats from logistics (or fallback to zero)
            RouteStatsDto stats = RouteStatsDto.zero();
            if (logisticsQueryService != null) {
                try {
                    stats = logisticsQueryService.getStatsByOperator(user.getId());
                } catch (Exception e) {
                    // Fallback to zero if logistics is unavailable
                }
            }

            // Build UserResource with computed fields using the assembler
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user, stats);

            var loginResponse = new LoginResponseResource(token, userResource);
            return ResponseEntity.ok(loginResponse);
        } catch (UserCommandServiceImpl.LockedUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Handles the login with PIN request (POST /auth/login-pin).
     * @param resource the sign-in request body with 4-digit PIN.
     * @return the login response with JWT token and user info.
     */
    @PostMapping(value = "/login-pin", consumes = {"application/json"})
    @Operation(summary = "Login with operator PIN",
               description = "Authenticate using a 4-digit operator PIN (no email). Returns JWT if PIN matches any operator's stored password hash.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful."),
        @ApiResponse(responseCode = "401", description = "Invalid PIN."),
        @ApiResponse(responseCode = "403", description = "Account locked.")})
    public ResponseEntity<LoginResponseResource> loginPin(@Valid @RequestBody SignInPinResource resource) {
        try {
            var command = new com.example.cryoguard.iam.domain.model.commands.SignInPinCommand(resource.pin());
            var result = userCommandService.handle(command);
            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            var user = result.get().getLeft();
            var token = result.get().getRight();

            // Get trip stats from logistics (or fallback to zero)
            RouteStatsDto stats = RouteStatsDto.zero();
            if (logisticsQueryService != null) {
                try {
                    stats = logisticsQueryService.getStatsByOperator(user.getId());
                } catch (Exception e) {
                    // Fallback to zero if logistics is unavailable
                }
            }

            // Build UserResource with computed fields using the assembler
            UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user, stats);

            var loginResponse = new LoginResponseResource(token, userResource);
            return ResponseEntity.ok(loginResponse);
        } catch (UserCommandServiceImpl.LockedUserException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Handles the sign-up request.
     * @param signUpResource the sign-up request body.
     * @return the created user resource.
     */
    @PostMapping(value = "/sign-up", consumes = {"application/json"})
    @Operation(summary = "Sign-up", description = "Sign-up with the provided credentials.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully."),
            @ApiResponse(responseCode = "400", description = "Bad request.")})
    public ResponseEntity<UserResource> signUp(@RequestBody SignUpResource signUpResource) {
        var signUpCommand = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
        var user = userCommandService.handle(signUpCommand);
        if (user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user.get());
        return new ResponseEntity<>(userResource, HttpStatus.CREATED);
    }
}