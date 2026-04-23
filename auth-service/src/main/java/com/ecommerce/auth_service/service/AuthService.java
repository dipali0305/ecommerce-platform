package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.constants.AuthConstants;
import com.ecommerce.auth_service.dto.request.LoginRequest;
import com.ecommerce.auth_service.dto.request.RefreshTokenRequest;
import com.ecommerce.auth_service.dto.response.LoginResponse;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.AccountLockedException;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.exception.TokenException;
import com.ecommerce.auth_service.repository.UserRepository;
import com.ecommerce.auth_service.security.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.refresh-token.expiration-ms}")
    private long refreshTokenExpirationMs;

    @Transactional
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        validateUserStatus(user);
        validatePassword(request, user);
        resetFailedAttemptsIfNeeded(user);
        return generateTokenPair(user);
    }

    private LoginResponse generateTokenPair(User user) {
        List<String> roles = List.of(user.getRole().getRoleName().name());

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

    private void handleFailedLogin(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);
        if (attempts >= AuthConstants.MAX_FAILED_LOGIN_ATTEMPTS) {
            user.setAccountLocked(true);
            user.setLockedAt(Instant.now());
            log.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), attempts);
        } else {
            log.info("Failed login attempt {} of {} for user: {}",
                    attempts, AuthConstants.MAX_FAILED_LOGIN_ATTEMPTS, user.getEmail());
        }
        userRepository.save(user);
    }

    private void resetFailedAttemptsIfNeeded(User user) {
        if (user.getFailedLoginAttempts() > 0) {
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }
    }

    private void validateUserStatus(User user) {
        if (!AuthConstants.STATUS_ACTIVE.equals(user.getStatus())) {
            throw new BadRequestException("Account is not active");
        }
        if (user.isAccountLocked()) {
            throw new AccountLockedException("Account is locked. Please reset password.");
        }
    }

    private void validatePassword(LoginRequest request, User user) {
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            handleFailedLogin(user);
            throw new BadCredentialsException("Invalid email or password");
        }
    }

    public LoginResponse refresh(RefreshTokenRequest request) {
        UUID userId = tokenService.validateRefreshToken(request.getRefreshToken());
        if (userId == null) {
            throw new TokenException("Invalid or expired refresh token");
        }
        tokenService.deleteRefreshToken(request.getRefreshToken());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenException("Invalid refresh token"));

        validateUserStatus(user);
        log.info("Token refreshed for user: {}", user.getEmail());
        return generateTokenPair(user);
    }

}
