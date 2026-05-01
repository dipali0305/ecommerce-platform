package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.request.AddressRequest;
import com.ecommerce.auth_service.dto.request.AddressUpdateRequest;
import com.ecommerce.auth_service.dto.response.AddressResponse;
import com.ecommerce.auth_service.dto.response.ApiResponse;
import com.ecommerce.auth_service.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user/address")
@PreAuthorize("hasRole('CUSTOMER')")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AddressResponse>>> getAddresses(
            @AuthenticationPrincipal UUID userId) {

        List<AddressResponse> addresses = addressService.getAddresses(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Addresses fetched successfully", addresses));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<AddressResponse>> addAddress(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody AddressRequest request) {

        AddressResponse address = addressService.addAddress(userId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Address added successfully", address));
    }

    @PutMapping("/{addressId}")
    public ResponseEntity<ApiResponse<AddressResponse>> updateAddress(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID addressId,
            @Valid @RequestBody AddressUpdateRequest request) {

        AddressResponse address = addressService.updateAddress(userId, addressId, request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Address updated successfully", address));
    }
}
