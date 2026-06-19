package com.example.cryoguard.monitoring.presentation.resources;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateContainerWithKeyResource {
    private ContainerResource container;
    private String apiKey;
}
