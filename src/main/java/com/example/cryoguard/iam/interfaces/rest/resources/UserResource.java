package com.example.cryoguard.iam.interfaces.rest.resources;

import java.util.Date;

/**
 * User resource
 * <p>
 *     This record represents the resource for a user in responses.
 *     All strings are lowercase as per frontend expectations.
 *     Extended with computed fields for Vue frontend contract.
 * </p>
 * @param id the user id
 * @param name the user's full name
 * @param email the user's email
 * @param role the user's role (lowercase: admin, operator, supervisor, ngo)
 * @param status the user's status (lowercase: active, inactive, locked)
 * @param lastLogin the last login timestamp (legacy, kept for backward compatibility)
 * @param createdAt the creation timestamp (legacy, kept for backward compatibility)
 * @param telefono the optional phone number
 * @param pinBloqueado true when status is LOCKED
 * @param viajesAsignados count of active routes (INITIATED + IN_PROGRESS) for this operator
 * @param viajesCompletados count of completed routes for this operator
 * @param ultimaActividad formatted last login (dd/MM/yyyy HH:mm, es-PE) or "Sin actividad"
 */
public record UserResource(
    Long id,
    String name,
    String email,
    String role,
    String status,
    Date lastLogin,
    Date createdAt,
    String telefono,
    Boolean pinBloqueado,
    Integer viajesAsignados,
    Integer viajesCompletados,
    String ultimaActividad
) {
}