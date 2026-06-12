package com.example.cryoguard.logistics.domain.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RouteStatus {
    active("active"),
    completed("completed"),
    cancelled("cancelled"),
    INITIATED("iniciado"),
    IN_PROGRESS("en_ruta");

    private final String value;

    RouteStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}