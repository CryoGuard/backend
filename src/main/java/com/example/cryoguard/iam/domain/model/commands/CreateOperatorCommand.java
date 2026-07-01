package com.example.cryoguard.iam.domain.model.commands;

/**
 * Create operator command.
 * Creates a new operator with an auto-generated 4-digit PIN.
 * @param username the operator's username (full name)
 * @param email the operator's email
 * @param telefono the operator's phone number (optional)
 */
public record CreateOperatorCommand(String username, String email, String telefono) {
}
