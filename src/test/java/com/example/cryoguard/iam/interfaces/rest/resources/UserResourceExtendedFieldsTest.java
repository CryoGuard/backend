package com.example.cryoguard.iam.interfaces.rest.resources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UserResource with extended fields.
 */
class UserResourceExtendedFieldsTest {

    @Test
    void shouldHaveTelefonoField() {
        // WHEN creating UserResource with telefono
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null, // lastLogin, createdAt (old fields)
                "+51 987 654 321", // telefono
                false, // pinBloqueado
                3, // viajesAsignados
                10, // viajesCompletados
                "10/06/2024 14:30" // ultimaActividad
        );

        // THEN telefono should be stored
        assertEquals("+51 987 654 321", resource.telefono());
    }

    @Test
    void shouldHavePinBloqueadoField() {
        // WHEN creating UserResource with pinBloqueado
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null,
                "+51 987 654 321",
                true, // pinBloqueado = true
                3,
                10,
                "10/06/2024 14:30"
        );

        // THEN pinBloqueado should be true
        assertTrue(resource.pinBloqueado());
    }

    @Test
    void shouldHaveViajesAsignadosField() {
        // WHEN creating UserResource with viajesAsignados
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null,
                "+51 987 654 321",
                false,
                5, // viajesAsignados
                10,
                "10/06/2024 14:30"
        );

        // THEN viajesAsignados should be 5
        assertEquals(5, resource.viajesAsignados());
    }

    @Test
    void shouldHaveViajesCompletadosField() {
        // WHEN creating UserResource with viajesCompletados
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null,
                "+51 987 654 321",
                false,
                3,
                15, // viajesCompletados
                "10/06/2024 14:30"
        );

        // THEN viajesCompletados should be 15
        assertEquals(15, resource.viajesCompletados());
    }

    @Test
    void shouldHaveUltimaActividadField() {
        // WHEN creating UserResource with ultimaActividad
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null,
                "+51 987 654 321",
                false,
                3,
                10,
                "11/06/2024 09:15" // ultimaActividad
        );

        // THEN ultimaActividad should be formatted string
        assertEquals("11/06/2024 09:15", resource.ultimaActividad());
    }

    @Test
    void shouldHaveNullTelefonoWhenNotProvided() {
        // WHEN creating UserResource without telefono
        UserResource resource = new UserResource(
                1L, "testuser", "test@cryoguard.com", "operator", "active",
                null, null,
                null, // telefono
                false,
                0,
                0,
                "Sin actividad" // ultimaActividad when no lastLogin
        );

        // THEN telefono should be null
        assertNull(resource.telefono());
    }
}