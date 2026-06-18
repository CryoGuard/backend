package com.example.cryoguard.logistics.domain.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum RouteStatus {
    PLANNED("planned"),
    IN_PROGRESS("in_progress"),
    COMPLETED("completed"),
    CANCELLED("cancelled");

    private final String value;

    RouteStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }
}