package com.example.cryoguard.iam.application.internal.commandservices;

import com.example.cryoguard.iam.application.internal.outboundservices.hashing.HashingService;
import com.example.cryoguard.iam.application.internal.services.PinGenerator;
import com.example.cryoguard.iam.domain.model.aggregates.User;
import com.example.cryoguard.iam.domain.model.commands.ResetUserPinCommand;
import com.example.cryoguard.iam.domain.services.ResetUserPinCommandService;
import com.example.cryoguard.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import com.example.cryoguard.shared.domain.exceptions.UserNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementation of ResetUserPinCommandService.
 * Generates a random 4-digit PIN, hashes it, and stores it as the user's password.
 */
@Service
public class ResetUserPinCommandServiceImpl implements ResetUserPinCommandService {

    private final UserRepository userRepository;
    private final PinGenerator pinGenerator;
    private final HashingService hashingService;

    public ResetUserPinCommandServiceImpl(UserRepository userRepository, PinGenerator pinGenerator, HashingService hashingService) {
        this.userRepository = userRepository;
        this.pinGenerator = pinGenerator;
        this.hashingService = hashingService;
    }

    @Override
    public String handle(ResetUserPinCommand command) {
        var userOpt = userRepository.findById(command.userId());
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException(command.userId());
        }

        String plainPin = pinGenerator.generateUniquePin(command.userId());
        User user = userOpt.get();
        user.setPassword(hashingService.encode(plainPin));
        userRepository.save(user);

        return plainPin;
    }
}