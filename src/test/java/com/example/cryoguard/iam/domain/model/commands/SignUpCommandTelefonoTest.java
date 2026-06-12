package com.example.cryoguard.iam.domain.model.commands;

import com.example.cryoguard.iam.domain.model.entities.Role;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for SignUpCommand telefono field.
 */
class SignUpCommandTelefonoTest {

    @Test
    void shouldAcceptTelefono() {
        // GIVEN a SignUpCommand with telefono
        SignUpCommand command = new SignUpCommand("testuser", "test@cryoguard.com", "Pin1234", Role.getDefaultRole(), "+51 987 654 321");

        // THEN telefono should be stored
        assertEquals("+51 987 654 321", command.telefono());
    }

    @Test
    void shouldAcceptNullTelefono() {
        // GIVEN a SignUpCommand without telefono (null)
        SignUpCommand command = new SignUpCommand("testuser", "test@cryoguard.com", "Pin1234", Role.getDefaultRole(), null);

        // THEN telefono should be null
        assertNull(command.telefono());
    }

    @Test
    void shouldWorkWithEmptyStringTelefono() {
        // GIVEN a SignUpCommand with empty string telefono
        SignUpCommand command = new SignUpCommand("testuser", "test@cryoguard.com", "Pin1234", Role.getDefaultRole(), "");

        // THEN telefono should be empty string
        assertEquals("", command.telefono());
    }
}