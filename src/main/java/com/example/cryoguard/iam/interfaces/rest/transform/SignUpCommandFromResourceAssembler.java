package com.example.cryoguard.iam.interfaces.rest.transform;

import com.example.cryoguard.iam.domain.model.commands.SignUpCommand;
import com.example.cryoguard.iam.domain.model.entities.Role;
import com.example.cryoguard.iam.interfaces.rest.resources.SignUpResource;

import java.util.List;

public class SignUpCommandFromResourceAssembler {
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        var role = resource.role() != null && !resource.role().isBlank()
            ? Role.toRoleFromName(resource.role())
            : null;
        return new SignUpCommand(resource.username(), resource.email(), resource.password(), role, resource.telefono());
    }
}