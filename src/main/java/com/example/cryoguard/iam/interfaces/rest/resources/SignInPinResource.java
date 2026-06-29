package com.example.cryoguard.iam.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Login request body using a 4-digit operator PIN (no email required).
 */
public record SignInPinResource(
    @NotBlank @Pattern(regexp = "\\d{4}", message = "PIN must be exactly 4 digits")
    String pin
) {}
