package com.example.cryoguard.iam.interfaces.rest;

import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.interfaces.acl.LogisticsQueryService;
import com.example.cryoguard.iam.interfaces.rest.resources.LoginResponseResource;
import com.example.cryoguard.iam.interfaces.rest.resources.SignInResource;
import com.example.cryoguard.iam.interfaces.rest.resources.UserResource;
import com.example.cryoguard.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;
import com.example.cryoguard.logistics.interfaces.acl.RouteStatsDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for AuthenticationController login response with computed fields.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationControllerLoginFieldsTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private LogisticsQueryService logisticsQueryService;

    private AuthenticationController controller;

    @BeforeEach
    void setUp() {
        // Set up the static assembler with the mock service
        UserResourceFromEntityAssembler.setLogisticsQueryService(logisticsQueryService);
        controller = new AuthenticationController(userCommandService, logisticsQueryService);
    }

    @Test
    void should_return_user_with_computed_fields_on_successful_login() {
        // GIVEN a user with status ACTIVE and no lastLogin
        com.example.cryoguard.iam.domain.model.aggregates.User user =
                new com.example.cryoguard.iam.domain.model.aggregates.User("operator", "hashedpassword", "operator@cryoguard.com");
        user.setId(5L);
        user.setStatus(com.example.cryoguard.iam.domain.model.aggregates.User.UserStatus.ACTIVE);
        user.setRoles(java.util.Set.of(new com.example.cryoguard.iam.domain.model.entities.Role(
                com.example.cryoguard.iam.domain.model.valueobjects.Roles.ROLE_OPERATOR)));

        // GIVEN logistics returns stats
        when(logisticsQueryService.getStatsByOperator(5L)).thenReturn(new RouteStatsDto(2, 10));

        // GIVEN login succeeds - use specific SignInCommand type
        com.example.cryoguard.iam.domain.model.commands.SignInCommand signInCommand =
                new com.example.cryoguard.iam.domain.model.commands.SignInCommand("operator@cryoguard.com", "password");
        when(userCommandService.handle(signInCommand))
                .thenReturn(java.util.Optional.of(new org.apache.commons.lang3.tuple.ImmutablePair<>(user, "jwt-token")));

        // WHEN logging in
        SignInResource signInResource = new SignInResource("operator@cryoguard.com", "password");
        ResponseEntity<LoginResponseResource> response = controller.login(signInResource);

        // THEN response has computed fields
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().user());

        // Verify computed fields are present
        UserResource userResource = response.getBody().user();
        assertEquals(5L, userResource.id());
        assertEquals("operator", userResource.name());
        assertEquals("operator@cryoguard.com", userResource.email());
        assertEquals("operator", userResource.role());
        assertEquals("active", userResource.status());
        assertEquals(false, userResource.pinBloqueado());
        assertEquals(2, userResource.viajesAsignados());
        assertEquals(10, userResource.viajesCompletados());
        assertEquals("Sin actividad", userResource.ultimaActividad());
    }

    @Test
    void should_return_pinBloqueado_true_when_user_status_is_locked() {
        // GIVEN a locked user
        com.example.cryoguard.iam.domain.model.aggregates.User user =
                new com.example.cryoguard.iam.domain.model.aggregates.User("locked", "hashedpassword", "locked@cryoguard.com");
        user.setId(10L);
        user.setStatus(com.example.cryoguard.iam.domain.model.aggregates.User.UserStatus.LOCKED);
        user.setRoles(java.util.Set.of(new com.example.cryoguard.iam.domain.model.entities.Role(
                com.example.cryoguard.iam.domain.model.valueobjects.Roles.ROLE_OPERATOR)));

        when(logisticsQueryService.getStatsByOperator(10L)).thenReturn(new RouteStatsDto(0, 0));

        com.example.cryoguard.iam.domain.model.commands.SignInCommand signInCommand =
                new com.example.cryoguard.iam.domain.model.commands.SignInCommand("locked@cryoguard.com", "password");
        when(userCommandService.handle(signInCommand))
                .thenReturn(java.util.Optional.of(new org.apache.commons.lang3.tuple.ImmutablePair<>(user, "jwt-token")));

        // WHEN logging in
        SignInResource signInResource = new SignInResource("locked@cryoguard.com", "password");
        ResponseEntity<LoginResponseResource> response = controller.login(signInResource);

        // THEN pinBloqueado is true
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().user().pinBloqueado());
    }
}