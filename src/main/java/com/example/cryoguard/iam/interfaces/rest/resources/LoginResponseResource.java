package com.example.cryoguard.iam.interfaces.rest.resources;

/**
 * Login response resource
 * <p>
 *     This record represents the response for a successful login.
 *     Maps to frontend LoginResponse interface with full UserResource.
 * </p>
 * @param token the JWT token
 * @param user the user info with computed fields (id, name, email, role, status, telefono, pinBloqueado, viajesAsignados, viajesCompletados, ultimaActividad)
 */
public record LoginResponseResource(
    String token,
    UserResource user
) {
}