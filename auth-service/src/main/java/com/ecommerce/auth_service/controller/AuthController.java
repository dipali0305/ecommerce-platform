package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.dto.request.LoginRequest;
import com.ecommerce.auth_service.dto.response.ApiResponse;
import com.ecommerce.auth_service.dto.response.LoginResponse;
import com.ecommerce.auth_service.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    //private final AuthService authService;

  /*  @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Login successful", response));
    }*/



}
