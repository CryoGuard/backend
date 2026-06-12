package com.example.cryoguard.iam.domain.model.aggregates;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserEntityTelefonoTest {

    @Test
    void shouldHaveTelefonoField() {
        // GIVEN a new User entity
        User user = new User("testuser", "password123", "test@cryoguard.com");

        // THEN telefono should be null by default (nullable field)
        assertNull(user.getTelefono());
    }

    @Test
    void shouldAcceptTelefonoValue() {
        // GIVEN a new User entity
        User user = new User("testuser", "password123", "test@cryoguard.com");

        // WHEN setting telefono
        user.setTelefono("+51 987 654 321");

        // THEN telefono should be stored correctly
        assertEquals("+51 987 654 321", user.getTelefono());
    }

    @Test
    void shouldAllowNullTelefono() {
        // GIVEN a new User entity with telefono set
        User user = new User("testuser", "password123", "test@cryoguard.com");
        user.setTelefono("+51 987 654 321");

        // WHEN clearing telefono
        user.setTelefono(null);

        // THEN telefono should be null
        assertNull(user.getTelefono());
    }

    @Test
    void shouldPreserveTelefonoOnUserCreation() {
        // WHEN creating a user with telefono
        User user = new User("testuser", "password123", "test@cryoguard.com");
        user.setTelefono("+51 999 888 777");

        // THEN telefono should be retrievable
        assertEquals("+51 999 888 777", user.getTelefono());
    }
}