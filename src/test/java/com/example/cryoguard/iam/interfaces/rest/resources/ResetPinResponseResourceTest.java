package com.example.cryoguard.iam.interfaces.rest.resources;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResetPinResponseResource.
 */
class ResetPinResponseResourceTest {

    @Test
    void shouldCreateWithNewPin() {
        // GIVEN a new PIN
        String newPin = "1234";

        // WHEN creating ResetPinResponseResource
        ResetPinResponseResource resource = new ResetPinResponseResource(newPin);

        // THEN newPin should be stored
        assertEquals("1234", resource.newPin());
    }

    @Test
    void shouldWorkWithDifferentPins() {
        // WHEN creating with different PINs
        ResetPinResponseResource r1 = new ResetPinResponseResource("0000");
        ResetPinResponseResource r2 = new ResetPinResponseResource("9999");

        // THEN each should have correct pin
        assertEquals("0000", r1.newPin());
        assertEquals("9999", r2.newPin());
    }
}