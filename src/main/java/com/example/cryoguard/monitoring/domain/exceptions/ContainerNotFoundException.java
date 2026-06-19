package com.example.cryoguard.monitoring.domain.exceptions;

/**
 * Exception thrown when a container cannot be found.
 */
public class ContainerNotFoundException extends RuntimeException {
    public ContainerNotFoundException(String message) {
        super(message);
    }
}