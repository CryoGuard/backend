package com.example.cryoguard.iam.domain.services;

import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.SignInCommand;
import com.example.cryoguard.iam.domain.model.commands.SignInPinCommand;
import com.example.cryoguard.iam.domain.model.commands.SignUpCommand;
import com.example.cryoguard.iam.domain.model.commands.UpdateUserCommand;
import com.example.cryoguard.iam.domain.model.commands.DisableUserCommand;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.Optional;

/**
 * User command service
 * <p>
 *     This interface represents the service to handle user commands.
 * </p>
 */
public interface UserCommandService {
    /**
     * Handle sign in command
     * @param command the {@link SignInCommand} command
     * @return an {@link Optional} of {@link ImmutablePair} of {@link User} and {@link String} (token)
     */
    Optional<ImmutablePair<User, String>> handle(SignInCommand command);

    /**
     * Handle sign in with PIN command
     * @param command the {@link SignInPinCommand} command
     * @return an {@link Optional} of {@link ImmutablePair} of {@link User} and {@link String} (token)
     */
    Optional<ImmutablePair<User, String>> handle(SignInPinCommand command);

    /**
     * Handle sign up command
     * @param command the {@link SignUpCommand} command
     * @return an {@link Optional} of {@link User} entity
     */
    Optional<User> handle(SignUpCommand command);

    /**
     * Handle update user command
     * @param command the {@link UpdateUserCommand} command
     * @return an {@link Optional} of {@link User} entity
     */
    Optional<User> handle(UpdateUserCommand command);

    /**
     * Handle disable user command
     * @param command the {@link DisableUserCommand} command
     */
    void handle(DisableUserCommand command);
}