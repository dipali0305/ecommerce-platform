package com.ecommerce.auth_service.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum RoleName {
    CUSTOMER,
    SELLER,
    ADMIN;

    @JsonCreator
    public static RoleName from(String value) {
        if (value == null || value.isBlank()) {
            return null; // allow optional role
        }

        return Arrays.stream(RoleName.values())
                .filter(role -> role.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid role. Allowed values: CUSTOMER, SELLER"));
    }

    @JsonValue
    public String toValue() {
        return this.name(); // ensures consistent response
    }
}
