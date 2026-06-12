package com.example.cryoguard.iam.application.internal.commandservices;

import com.example.cryoguard.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.ResetUserPinCommand;
import com.example.cryoguard.iam.domain.services.ResetUserPinCommandService;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.example.cryoguard.shared.domain.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Random;

/**
 * Implementation of ResetUserPinCommandService.
 * Generates a random 4-digit PIN, hashes it, and stores it as the user's password.
 */
@Service
public class ResetUserPinCommandServiceImpl implements ResetUserPinCommandService {

    private final UserRepository userRepository;
    private final HashingService hashingService;
    private final Random random = new Random();

    public ResetUserPinCommandServiceImpl(UserRepository userRepository, HashingService hashingService) {
        this.userRepository = userRepository;
        this.hashingService = hashingService;
    }

    @Override
    public String handle(ResetUserPinCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }

        // Generate random 4-digit PIN
        int pin = 1000 + random.nextInt(9000);
        String plainPin = String.valueOf(pin);

        // Hash and store as password
        User user = userOpt.get();
        user.setPassword(hashingService.encode(plainPin));
        userRepository.save(user);

        return plainPin;
    }
}