package com.example.cryoguard.iam.interfaces.rest.transform;

import com.example.cryoguard.iam.domain.model.commands.UpdateUserCommand;
import com.example.cryoguard.iam.interfaces.rest.resources.UpdateUserResource;

public class UpdateUserCommandFromResourceAssembler {
    public static UpdateUserCommand toCommandFromResource(Long userId, UpdateUserResource resource) {
        return new UpdateUserCommand(
            userId,
            resource.username(),
            resource.email(),
            resource.roles() != null && !resource.roles().isEmpty() ? resource.roles().get(0) : null,
            resource.status(),
            resource.telefono()
        );
    }
}