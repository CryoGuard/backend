package com.example.cryoguard.evaluation.domain.valueobjects;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AlertType {
    TEMPERATURE("temperature"),
    HUMIDITY("humidity"),
    VIBRATION("vibration"),
    DOOR("door"),
    GEOFENCE("geofence"),
    BATTERY("battery");   // NEW — added for edge local alert forwarding

    private final String value;

    AlertType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static AlertType fromValue(String value) {
        for (AlertType type : AlertType.values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown alert type: " + value);
    }
}