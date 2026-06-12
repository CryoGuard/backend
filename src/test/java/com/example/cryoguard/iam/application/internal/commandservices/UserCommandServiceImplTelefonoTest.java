package com.example.cryoguard.iam.application.internal.commandservices;

import com.example.cryoguard.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.cryoguard.iam.application.internal.outboundservices.tokens.TokenService;
import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.SignUpCommand;
import com.example.cryoguard.iam.domain.model.commands.UpdateUserCommand;
import com.example.cryoguard.iam.domain.model.entities.Role;
import com.example.cryoguard.iam.domain.model.valueobjects.Roles;
import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests for UserCommandServiceImpl telefono handling.
 */
@ExtendWith(MockitoExtension.class)
class UserCommandServiceImplTelefonoTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private TokenService tokenService;

    @Mock
    private RoleRepository roleRepository;

    private UserCommandServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new UserCommandServiceImpl(userRepository, hashingService, tokenService, roleRepository);
    }

    @Test
    void should_persist_telefono_on_signup() {
        // GIVEN a role
        Role operatorRole = new Role(Roles.ROLE_OPERATOR);
        when(roleRepository.findByName(Roles.ROLE_OPERATOR)).thenReturn(Optional.of(operatorRole));
        when(hashingService.encode("Pin1234")).thenReturn("hashedPin");

        // GIVEN no existing user
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@cryoguard.com")).thenReturn(false);

        // GIVEN saved user with telefono set
        User savedUser = new User("newuser", "hashedPin", "new@cryoguard.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(savedUser));

        // WHEN signing up with telefono
        SignUpCommand command = new SignUpCommand("newuser", "new@cryoguard.com", "Pin1234", operatorRole, "+51 987 654 321");
        Optional<User> result = service.handle(command);

        // THEN telefono was persisted
        verify(userRepository).save(argThat(user -> "+51 987 654 321".equals(user.getTelefono())));
    }

    @Test
    void should_update_telefono_on_update() {
        // GIVEN existing user
        User existingUser = new User("testuser", "password", "test@cryoguard.com");
        existingUser.setId(1L);
        existingUser.setTelefono("+51 111 222 333");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // WHEN updating with new telefono
        UpdateUserCommand command = new UpdateUserCommand(1L, "testuser", "test@cryoguard.com",
                null, null, "+51 999 888 777");
        Optional<User> result = service.handle(command);

        // THEN telefono was updated
        verify(userRepository).save(argThat(user -> "+51 999 888 777".equals(user.getTelefono())));
    }

    @Test
    void should_preserve_existing_telefono_when_not_sent_in_update() {
        // GIVEN existing user with telefono
        User existingUser = new User("testuser", "password", "test@cryoguard.com");
        existingUser.setId(1L);
        existingUser.setTelefono("+51 111 222 333");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        // WHEN updating without telefono (null)
        UpdateUserCommand command = new UpdateUserCommand(1L, "testuser", "test@cryoguard.com",
                null, null, null);
        Optional<User> result = service.handle(command);

        // THEN existing telefono was preserved
        verify(userRepository).save(argThat(user -> "+51 111 222 333".equals(user.getTelefono())));
    }
}