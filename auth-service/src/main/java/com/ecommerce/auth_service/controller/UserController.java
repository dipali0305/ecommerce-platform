package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.request.ForgotPasswordRequest;
import com.ecommerce.auth_service.dto.request.RegisterRequest;
import com.ecommerce.auth_service.dto.request.ResetPasswordRequest;
import com.ecommerce.auth_service.dto.response.ApiResponse;
import com.ecommerce.auth_service.dto.response.CustomerProfileResponse;
import com.ecommerce.auth_service.dto.response.UserResponse;
import com.ecommerce.auth_service.service.PasswordService;
import com.ecommerce.auth_service.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordService passwordService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(HttpStatus.CREATED, "Registration successful. Please check your email to activate your account.", user));
    }


    @GetMapping("/activate")
    public ResponseEntity<ApiResponse<Void>> activateAccount(@RequestParam String token) {
        userService.activateAccount(token);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Account activated successfully. You can now log in."));
    }

    @PostMapping("/forgot_password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordService.forgotPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "If the email exists, a reset link has been sent"));
    }

    @PostMapping("/reset_password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ResponseEntity.ok(
                ApiResponse.success(HttpStatus.OK, "Password reset successfully. Please login with your new password."));
    }


    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<CustomerProfileResponse>> getProfile(
            @AuthenticationPrincipal UUID userId) {

        CustomerProfileResponse profile = userService.getProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Profile fetched successfully", profile));
    }
}
