package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.dto.request.ForgotPasswordRequest;
import com.ecommerce.auth_service.dto.request.ResetPasswordRequest;
import com.ecommerce.auth_service.entity.User;
import com.ecommerce.auth_service.exception.BadRequestException;
import com.ecommerce.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int RESET_TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-ms}")
    private long resetTokenExpirationMs;

    public void forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        userRepository.findByEmail(email).ifPresent(user -> {
            String resetToken = generateSecureToken();
            tokenService.storeResetToken(resetToken, user.getUserId(), resetTokenExpirationMs);
        });
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public void resetPassword(ResetPasswordRequest request) {
        UUID userId = tokenService.validateResetToken(request.getResetToken());
        if (userId == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        updatePasswordAndUnlock(userId, request.getNewPassword());

        tokenService.deleteResetToken(request.getResetToken());
        tokenService.revokeAllTokens(userId);

        log.info("Password reset and account unlocked for user: {}", userId);
    }

    @Transactional
    protected void updatePasswordAndUnlock(UUID userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("Reset token references non-existent user: {}", userId);
                    return new RuntimeException("Data integrity error: user not found for valid reset token");
                });

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        user.setLockedAt(null);

        userRepository.save(user);
    }
}
