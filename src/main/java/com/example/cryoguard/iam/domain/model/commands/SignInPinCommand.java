package com.example.cryoguard.iam.domain.model.commands;

/**
 * Sign in command using operator PIN (no email required).
 */
public record SignInPinCommand(String pin) {}
