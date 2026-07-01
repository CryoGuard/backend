package com.example.cryoguard.iam.interfaces.rest.resources;

/**
 * Create-operator response body containing the created user and the auto-generated PIN.
 */
public record CreateOperatorResponseResource(UserResource user, String pin) {}
