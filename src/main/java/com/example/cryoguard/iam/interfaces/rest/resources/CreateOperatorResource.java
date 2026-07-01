package com.example.cryoguard.iam.interfaces.rest.resources;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Create-operator request body. Password is OMITTED — the server generates
 * a unique 4-digit PIN and returns it in the response.
 */
public record CreateOperatorResource(
        @NotBlank String username,
        @NotBlank @Email String email,
        String telefono
) {}
