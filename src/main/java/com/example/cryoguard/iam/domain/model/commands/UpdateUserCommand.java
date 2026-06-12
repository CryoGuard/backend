package com.example.cryoguard.iam.domain.model.commands;

import com.example.cryoguard.iam.domain.model.aggregates.User;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Update user command
 * <p>
 *     This class represents the command to update a user.
 * </p>
 * @param userId the id of the user to update
 * @param username the new username
 * @param email the new email
 * @param role the new role
 * @param status the new status
 * @param telefono the optional phone number (null means don't update phone)
 */
public record UpdateUserCommand(
    Long userId,
    String username,
    String email,
    String role,
    User.UserStatus status,
    String telefono
) {
}