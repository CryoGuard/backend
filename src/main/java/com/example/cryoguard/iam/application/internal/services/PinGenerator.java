package com.example.cryoguard.iam.application.internal.services;

import com.example.cryoguard.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * Stateless utility that generates unique 4-digit operator PINs.
 * Extracted so the same logic is used by both reset-password and create-operator flows.
 */
@Component
public class PinGenerator {
    private static final int PIN_MIN = 1000;
    private static final int PIN_MAX_EXCLUSIVE = 9000; // 9000 values
    private static final int MAX_ATTEMPTS = 20;
    private final Random random = new Random();

    private final UserRepository userRepository;
    private final HashingService hashingService;

    public PinGenerator(UserRepository userRepository, HashingService hashingService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    /**
     * Returns a 4-digit PIN that does NOT match any other operator's stored PIN.
     * Skip checking against user with id == excludeUserId (useful for reset where the user already has an old PIN).
     */
    public String generateUniquePin(Long excludeUserId) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String candidate = String.valueOf(PIN_MIN + random.nextInt(PIN_MAX_EXCLUSIVE));
            if (!isPinInUse(candidate, excludeUserId)) {
                return candidate;
            }
        }
        throw new IllegalStateException("PIN space exhausted after " + MAX_ATTEMPTS + " attempts");
    }

    private boolean isPinInUse(String plainPin, Long excludeUserId) {
        return userRepository.findAll().stream()
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .anyMatch(u -> hashingService.matches(plainPin, u.getPassword()));
    }
}
