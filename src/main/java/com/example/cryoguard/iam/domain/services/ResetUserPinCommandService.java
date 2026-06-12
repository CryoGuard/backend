package com.example.cryoguard.iam.domain.services;

import com.example.cryoguard.iam.domain.model.commands.ResetUserPinCommand;

/**
 * Service interface for handling user PIN reset commands.
 */
public interface ResetUserPinCommandService {

    /**
     * Handles the reset PIN command.
     * Generates a new random 4-digit PIN, stores it hashed, and returns the plain PIN.
     *
     * @param command the reset PIN command containing the user id
     * @return the new plain-text PIN (4 digits)
     * @throws RuntimeException if user is not found
     */
    String handle(ResetUserPinCommand command);
}