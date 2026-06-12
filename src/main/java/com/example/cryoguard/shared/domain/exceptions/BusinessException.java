package com.example.cryoguard.shared.domain.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Base class for business-rule violations that should be reported
 * to the API caller with a controlled, descriptive HTTP response
 * (4xx) rather than a generic 500 Internal Server Error.
 *
 * <p>Subclasses MUST provide a stable {@link HttpStatus} and a
 * human-readable message. The {@code field} is optional and used
 * to identify which request field caused the error (e.g. "username").
 */
public abstract class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String field;

    protected BusinessException(HttpStatus status, String message) {
        this(status, message, null);
    }

    protected BusinessException(HttpStatus status, String message, String field) {
        super(message);
        this.status = status;
        this.field = field;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }
}
