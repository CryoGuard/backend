package com.example.cryoguard.iam.domain.model.commands;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResetUserPinCommand.
 */
class ResetUserPinCommandTest {

    @Test
    void shouldCreateWithUserId() {
        // GIVEN a user id
        Long userId = 5L;

        // WHEN creating ResetUserPinCommand
        ResetUserPinCommand command = new ResetUserPinCommand(userId);

        // THEN userId should be stored
        assertEquals(5L, command.userId());
    }

    @Test
    void shouldWorkWithDifferentUserIds() {
        // WHEN creating ResetUserPinCommand with different ids
        ResetUserPinCommand cmd1 = new ResetUserPinCommand(1L);
        ResetUserPinCommand cmd2 = new ResetUserPinCommand(999L);

        // THEN each should have correct userId
        assertEquals(1L, cmd1.userId());
        assertEquals(999L, cmd2.userId());
    }
}