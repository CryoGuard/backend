package com.example.cryoguard.iam.domain.model.commands;

/**
 * Reset user PIN command
 * <p>
 *     This command instructs the system to reset a user's PIN (password) to a new random 4-digit value.
 * </p>
 * @param userId the id of the user whose PIN should be reset
 */
public record ResetUserPinCommand(Long userId) {
}