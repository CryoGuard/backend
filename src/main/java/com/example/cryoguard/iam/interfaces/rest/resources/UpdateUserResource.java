package com.example.cryoguard.iam.interfaces.rest.resources;

import com.example.cryoguard.iam.domain.model.aggregates.User;

import java.util.List;

/**
 * Update user resource
 * <p>
 *     This record represents the resource for updating a user.
 * </p>
 * @param username the new username
 * @param email the new email
 * @param roles the new roles
 * @param status the new status
 * @param telefono the optional phone number (null means don't update phone)
 */
public record UpdateUserResource(
    String username,
    String email,
    List<String> roles,
    User.UserStatus status,
    String telefono
) {
}