package com.example.cryoguard.shared.domain.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Thrown when a sign-up request targets a username or email that
 * is already taken. Maps to HTTP 409 Conflict with a descriptive
 * message including the offending value and field.
 */
public class DuplicateUserException extends BusinessException {

    public enum Field { USERNAME, EMAIL }

    private final Field field;

    public DuplicateUserException(Field field, String value) {
        super(
            HttpStatus.CONFLICT,
            String.format("%s '%s' already exists", field.name().charAt(0) + field.name().substring(1).toLowerCase(), value),
            field.name().toLowerCase()
        );
        this.field = field;
    }

    public Field getDuplicateField() {
        return field;
    }
}
