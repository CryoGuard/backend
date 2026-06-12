package com.example.cryoguard.shared.domain.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when authentication fails (invalid credentials, locked account, etc.).
 * Maps to HTTP 401 Unauthorized with a generic message that does NOT
 * leak whether the username or the password was wrong.
 */
public class InvalidCredentialsException extends BusinessException {

    public InvalidCredentialsException() {
        super(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
}
