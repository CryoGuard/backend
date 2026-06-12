package com.example.cryoguard.iam.domain.model.commands;

import com.example.cryoguard.iam.domain.model.entities.Role;

/**
 * Sign up command
 * <p>
 *     This class represents the command to sign up a user.
 * </p>
 * @param username the username of the user
 * @param email the email of the user
 * @param password the password of the user
 * @param role the role of the user
 * @param telefono the optional phone number of the user
 */
public record SignUpCommand(String username, String email, String password, Role role, String telefono) {
}