package com.ecommerce.auth_service.dto.response;

import com.ecommerce.auth_service.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

/**
 * BRD 5.8.3 — Customer Profile Response
 *
 * Dedicated projection for the profile endpoint — intentionally separate from
 * UserResponse (which is used for registration and admin views) to avoid
 * coupling two different use cases to the same DTO.
 *
 * Addresses are ordered: default first, then newest first (delegated to
 * AddressService.getAddresses which uses a DB-level ORDER BY).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomerProfileResponse {

    private UUID userId;
    private String name;
    private String email;
    private String role;
    private List<AddressResponse> addresses;

    public static CustomerProfileResponse from(User user, List<AddressResponse> addresses) {
        return CustomerProfileResponse.builder()
                .userId(user.getUserId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().getRoleName().name())
                .addresses(addresses)
                .build();
    }
}
