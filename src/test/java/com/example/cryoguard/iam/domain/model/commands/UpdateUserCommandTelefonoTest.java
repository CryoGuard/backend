package com.example.cryoguard.iam.domain.model.commands;

import com.example.cryoguard.iam.domain.model.aggregates.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for UpdateUserCommand telefono field.
 */
class UpdateUserCommandTelefonoTest {

    @Test
    void shouldAcceptTelefono() {
        // GIVEN an UpdateUserCommand with telefono
        UpdateUserCommand command = new UpdateUserCommand(1L, "newname", "new@cryoguard.com", "OPERATOR", User.UserStatus.ACTIVE, "+51 987 654 321");

        // THEN telefono should be stored
        assertEquals("+51 987 654 321", command.telefono());
    }

    @Test
    void shouldAcceptNullTelefono() {
        // GIVEN an UpdateUserCommand with null telefono
        UpdateUserCommand command = new UpdateUserCommand(1L, "newname", "new@cryoguard.com", "OPERATOR", User.UserStatus.ACTIVE, null);

        // THEN telefono should be null
        assertNull(command.telefono());
    }

    @Test
    void shouldWorkWithNullTelefonoWhenNotUpdating() {
        // GIVEN an UpdateUserCommand without telefono (simulating update without phone change)
        UpdateUserCommand command = new UpdateUserCommand(1L, "newname", "new@cryoguard.com", "OPERATOR", User.UserStatus.ACTIVE, null);

        // THEN telefono should be null
        assertNull(command.telefono());
    }
}