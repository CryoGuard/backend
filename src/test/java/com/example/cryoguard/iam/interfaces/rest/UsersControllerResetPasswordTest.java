package com.example.cryoguard.iam.interfaces.rest;

import com.example.cryoguard.iam.domain.model.commands.ResetUserPinCommand;
import com.example.cryoguard.iam.domain.services.ResetUserPinCommandService;
import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.domain.services.UserQueryService;
import com.example.cryoguard.iam.interfaces.rest.resources.ResetPinResponseResource;
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
 * Tests for reset-password endpoint in UsersController.
 */
@ExtendWith(MockitoExtension.class)
class UsersControllerResetPasswordTest {

    @Mock
    private UserCommandService userCommandService;

    @Mock
    private UserQueryService userQueryService;

    @Mock
    private ResetUserPinCommandService resetUserPinCommandService;

    private UsersController controller;

    @BeforeEach
    void setUp() {
        controller = new UsersController(userCommandService, userQueryService, resetUserPinCommandService);
    }

    @Test
    void should_reset_password_returns_new_pin() {
        // GIVEN the reset service returns a new PIN
        when(resetUserPinCommandService.handle(any(ResetUserPinCommand.class))).thenReturn("1234");

        // WHEN calling resetPassword for user 5
        ResponseEntity<ResetPinResponseResource> response = controller.resetPassword(5L);

        // THEN response should be 200 with newPin
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("1234", response.getBody().newPin());
    }

    @Test
    void should_return_404_when_user_not_found() {
        // GIVEN the reset service throws for non-existent user
        when(resetUserPinCommandService.handle(any(ResetUserPinCommand.class)))
                .thenThrow(new RuntimeException("User not found"));

        // WHEN calling resetPassword for user 9999
        ResponseEntity<ResetPinResponseResource> response = controller.resetPassword(9999L);

        // THEN response should be 404
        assertEquals(404, response.getStatusCode().value());
        assertNull(response.getBody());
    }
}