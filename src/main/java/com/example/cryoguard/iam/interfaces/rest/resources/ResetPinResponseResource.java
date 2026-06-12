package com.example.cryoguard.iam.interfaces.rest.resources;

/**
 * Reset PIN response resource
 * <p>
 *     This record represents the response returned after a successful PIN reset.
 *     Contains the new plain-text PIN that the user can use to log in.
 * </p>
 * @param newPin the new 4-digit PIN in plain text
 */
public record ResetPinResponseResource(String newPin) {
}