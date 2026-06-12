package com.example.cryoguard.iam.interfaces.rest.resources;

import java.util.List;

/**
 * Sign up resource
 * <p>
 *     This record represents the resource for signing up a new user.
 * </p>
 * @param username the username
 * @param email the email
 * @param password the password
 * @param role the role (ADMINISTRATOR or OPERATOR)
 * @param telefono the optional phone number
 */
public record SignUpResource(String username, String email, String password, String role, String telefono) {
}