package com.example.cryoguard.shared.domain.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a user lookup fails. Maps to HTTP 404 Not Found
 * with a descriptive message including the requested id or username.
 */
public class UserNotFoundException extends BusinessException {

    public UserNotFoundException(String identifier) {
        super(HttpStatus.NOT_FOUND, "User '" + identifier + "' not found", "id");
    }

    public UserNotFoundException(Long userId) {
        this(String.valueOf(userId));
    }
}
