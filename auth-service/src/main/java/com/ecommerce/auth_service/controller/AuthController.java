package com.ecommerce.auth_service.controller;

import com.ecommerce.auth_service.constants.AuthConstants;
import com.ecommerce.auth_service.dto.request.LoginRequest;
import com.ecommerce.auth_service.dto.request.RefreshTokenRequest;
import com.ecommerce.auth_service.dto.response.ApiResponse;
import com.ecommerce.auth_service.dto.response.LoginResponse;
import com.ecommerce.auth_service.dto.response.TokenValidationResponse;
import com.ecommerce.auth_service.exception.TokenException;
import com.ecommerce.auth_service.security.JwtProvider;
import com.ecommerce.auth_service.service.AuthService;
import com.ecommerce.auth_service.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

   @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Login successful", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal UUID userId,
            @RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = (request != null) ? request.getRefreshToken() : null;
        authService.logout(userId, refreshToken);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Logged out successfully"));
    }

    @GetMapping("/auth/validate")
    public ResponseEntity<ApiResponse<TokenValidationResponse>> validateToken(HttpServletRequest request) {
        String token = extractTokenOrThrow(request);

        Claims claims = jwtProvider.parseTokenSafe(token);
        if (claims == null) {
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Token invalid",
                    TokenValidationResponse.builder().valid(false).build()));
        }

        UUID userId = jwtProvider.getUserIdFromClaims(claims);

        if (!tokenService.isTokenValid(userId, token)) {
            return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Token revoked",
                    TokenValidationResponse.builder().valid(false).build()));
        }

        List<String> roles = jwtProvider.getRolesFromClaims(claims);
        String email = jwtProvider.getEmailFromClaims(claims);

        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Token valid",
                TokenValidationResponse.builder()
                        .valid(true)
                        .userId(userId)
                        .email(email)
                        .roles(roles)
                        .build()));
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(HttpStatus.OK, "Token refreshed successfully", response));
    }


    private String extractTokenOrThrow(HttpServletRequest request) {
        String header = request.getHeader(AuthConstants.AUTH_HEADER);
        if (header != null && header.startsWith(AuthConstants.BEARER_PREFIX)) {
            return header.substring(AuthConstants.BEARER_PREFIX.length());
        }
        throw new TokenException("Missing or invalid Authorization header");
    }

}
