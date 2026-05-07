package com.ecommerce.auth_service.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * BRD 5.8.2 — Update Address (partial update)
 *
 * All fields are optional. Only non-null fields are applied to the existing address.
 * Contrast with AddressRequest (create) where addressLine1, city, state, country,
 * and zipCode are required.
 */
@Getter
@Setter
public class AddressUpdateRequest {

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    private String addressLine1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    private String addressLine2;

    @Size(max = 100, message = "City must not exceed 100 characters")
    private String city;

    @Size(max = 100, message = "State must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "Country must not exceed 100 characters")
    private String country;

    @Pattern(regexp = "^[A-Za-z0-9\\s\\-]{3,20}$", message = "Invalid zip code format")
    private String zipCode;

    private Boolean isDefault;
}
