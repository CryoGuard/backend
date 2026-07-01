package com.example.cryoguard.iam.application.internal.commandservices;

import com.example.cryoguard.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.cryoguard.iam.application.internal.outboundservices.tokens.TokenService;
import com.example.cryoguard.iam.application.internal.services.PinGenerator;
import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.CreateOperatorCommand;
import com.example.cryoguard.iam.domain.model.commands.SignInCommand;
import com.example.cryoguard.iam.domain.model.commands.SignInPinCommand;
import com.example.cryoguard.iam.domain.model.commands.SignUpCommand;
import com.example.cryoguard.iam.domain.model.commands.UpdateUserCommand;
import com.example.cryoguard.iam.domain.model.commands.DisableUserCommand;
import com.example.cryoguard.iam.domain.model.valueobjects.Roles;
import com.example.cryoguard.iam.domain.services.UserCommandService;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.example.cryoguard.shared.domain.exceptions.DuplicateUserException;
import com.example.cryoguard.shared.domain.exceptions.InvalidCredentialsException;
import com.example.cryoguard.shared.domain.exceptions.UserNotFoundException;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * User command service implementation
 * <p>
 *     This class implements the {@link UserCommandService} interface and provides the implementation for the
 *     {@link SignInCommand}, {@link SignUpCommand}, {@link UpdateUserCommand}, and {@link DisableUserCommand} commands.
 * </p>
 */
@Service
public class UserCommandServiceImpl implements UserCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleRepository roleRepository;
    private final PinGenerator pinGenerator;

    public UserCommandServiceImpl(UserRepository userRepository, HashingService hashingService, TokenService tokenService, RoleRepository roleRepository, PinGenerator pinGenerator) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleRepository = roleRepository;
        this.pinGenerator = pinGenerator;
    }

    /**
     * Handle the sign-in command
     * <p>
     *     This method handles the {@link SignInCommand} command and returns the user and the token.
     * </p>
     * @param command the sign-in command containing the email and password
     * @return and optional containing the user matching the email and the generated token
     * @throws RuntimeException if the user is not found or the password is invalid
     */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInCommand command) {
        var user = userRepository.findByEmailIgnoreCase(command.email());
        if (user.isEmpty() || !hashingService.matches(command.password(), user.get().getPassword()))
            throw new InvalidCredentialsException();
        if (user.get().isLocked())
            throw new LockedUserException("User account is locked");

        // Record last login
        user.get().recordLogin();
        userRepository.save(user.get());

        var token = tokenService.generateToken(user.get().getUsername());
        return Optional.of(ImmutablePair.of(user.get(), token));
    }

    /**
     * Handle the sign-in with PIN command
     * <p>
     *     Iterates all users to find one whose password hash matches the provided PIN.
     *     Only OPERATOR role users are considered.
     * </p>
     * @param command the sign-in PIN command containing the 4-digit PIN
     * @return an optional containing the user and generated token if PIN matches
     */
    @Override
    public Optional<ImmutablePair<User, String>> handle(SignInPinCommand command) {
        var allUsers = userRepository.findAll();
        for (var user : allUsers) {
            // Check if user has OPERATOR role
            boolean isOperator = user.getRoles().stream()
                .anyMatch(role -> role.getName().equals(com.example.cryoguard.iam.domain.model.valueobjects.Roles.ROLE_OPERATOR));
            if (!isOperator) {
                continue;
            }
            if (hashingService.matches(command.pin(), user.getPassword())) {
                if (user.isLocked()) {
                    throw new LockedUserException("User account is locked");
                }
                user.recordLogin();
                userRepository.save(user);
                var token = tokenService.generateToken(user.getUsername());
                return Optional.of(ImmutablePair.of(user, token));
            }
        }
        return Optional.empty();
    }

    /**
     * Exception for locked user accounts
     */
    public static class LockedUserException extends com.example.cryoguard.shared.domain.exceptions.BusinessException {
        public LockedUserException(String message) {
            super(org.springframework.http.HttpStatus.FORBIDDEN, message, "status");
        }
    }

    /**
     * Handle the sign-up command
     * <p>
     *     This method handles the {@link SignUpCommand} command and returns the user.
     * </p>
     * @param command the sign-up command containing the username, email, and password
     * @return the created user
     */
    @Override
    public Optional<User> handle(SignUpCommand command) {
        if (userRepository.existsByUsername(command.username()))
            throw new DuplicateUserException(DuplicateUserException.Field.USERNAME, command.username());
        if (userRepository.existsByEmail(command.email()))
            throw new DuplicateUserException(DuplicateUserException.Field.EMAIL, command.email());

        var role = (command.role() == null)
            ? roleRepository.findByName(com.example.cryoguard.iam.domain.model.valueobjects.Roles.ROLE_OPERATOR).orElseThrow(() -> new UserNotFoundException("default role not seeded"))
            : roleRepository.findByName(command.role().getName()).orElseThrow(() -> new IllegalArgumentException("Role not found: " + command.role().getName()));

        var user = new User(command.username(), hashingService.encode(command.password()), command.email(), List.of(role));
        user.setTelefono(command.telefono());
        userRepository.save(user);
        return userRepository.findByUsername(command.username());
    }

    /**
     * Handle the create operator command with auto-generated PIN.
     * <p>
     *     This method handles the {@link CreateOperatorCommand} command and returns the created user
     *     along with the auto-generated 4-digit PIN.
     * </p>
     * @param command the create operator command containing username, email, and telefono
     * @return an {@link Optional} of {@link ImmutablePair} of {@link User} and the plain PIN
     */
    @Override
    @Transactional
    public Optional<ImmutablePair<User, String>> handle(CreateOperatorCommand command) {
        if (userRepository.existsByUsername(command.username())) {
            throw new DuplicateUserException(DuplicateUserException.Field.USERNAME, command.username());
        }
        if (userRepository.existsByEmail(command.email())) {
            throw new DuplicateUserException(DuplicateUserException.Field.EMAIL, command.email());
        }

        var role = roleRepository.findByName(Roles.ROLE_OPERATOR)
                .orElseThrow(() -> new UserNotFoundException("operator role not seeded"));

        String plainPin = pinGenerator.generateUniquePin(null);
        String hashed = hashingService.encode(plainPin);

        var user = new User(command.username(), hashed, command.email(), List.of(role));
        user.setTelefono(command.telefono());
        userRepository.save(user);

        var saved = userRepository.findByUsername(command.username())
                .orElseThrow(() -> new IllegalStateException("Failed to create operator"));
        return Optional.of(ImmutablePair.of(saved, plainPin));
    }

    /**
     * Handle the update user command
     * <p>
     *     This method handles the {@link UpdateUserCommand} command and returns the updated user.
     * </p>
     * @param command the update user command
     * @return the updated user
     */
    @Override
    public Optional<User> handle(UpdateUserCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty())
            throw new UserNotFoundException(command.userId());

        var user = userOpt.get();

        // Update fields
        if (command.username() != null && !command.username().isBlank()) {
            user.setUsername(command.username());
        }
        if (command.email() != null && !command.email().isBlank()) {
            user.setEmail(command.email());
        }
        if (command.status() != null) {
            user.setStatus(command.status());
        }
        if (command.role() != null && !command.role().isBlank()) {
            var role = roleRepository.findByName(com.example.cryoguard.iam.domain.model.valueobjects.Roles.valueOf(command.role().trim().toUpperCase()))
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + command.role()));
            user.setRoles(new java.util.HashSet<>(List.of(role)));
        }
        // Update telefono only if provided (null means preserve existing)
        if (command.telefono() != null) {
            user.setTelefono(command.telefono());
        }

        User savedUser = userRepository.save(user);
        return Optional.of(savedUser);
    }

    /**
     * Handle the disable user command
     * <p>
     *     This method handles the {@link DisableUserCommand} command.
     * </p>
     * @param command the disable user command
     */
    @Override
    public void handle(DisableUserCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty())
            throw new UserNotFoundException(command.userId());

        var user = userOpt.get();
        user.disable();
        userRepository.save(user);
    }
}