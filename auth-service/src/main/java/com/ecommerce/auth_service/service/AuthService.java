package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.constants.AuthConstants;
import com.ecommerce.auth_service.dto.request.LoginRequest;
import com.ecommerce.auth_service.dto.response.LoginResponse;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.AccountLockedException;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;

    @Value("${app.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

   public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!AuthConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new BadRequestException("Account is not active. Current status: " + user.getStatus());
        }

        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    "Account is locked due to too many failed login attempts. Please reset your password to unlock.");
        }
        log.info("Token refreshed for user: {}", user.getEmail());
        return generateTokenPair(user);

    }

    private LoginResponse generateTokenPair(User user) {
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getRoleName().name())
                .toList();

        String accessToken = jwtProvider.generateToken(user.getUserId(), user.getEmail(), roles);
        tokenService.storeAccessToken(user.getUserId(), accessToken, jwtProvider.getExpirationMs());

        String refreshToken = tokenService.generateAndStoreRefreshToken(
                user.getUserId(), refreshTokenExpirationMs);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProvider.getExpirationMs() / 1000)
                .build();
    }

    public void logout(UUID userId, String refreshToken) {
        tokenService.revokeAccessToken(userId);
        if (refreshToken != null) {
            tokenService.deleteRefreshToken(refreshToken);
        }
        log.info("User logged out: {}", userId);
    }

}
