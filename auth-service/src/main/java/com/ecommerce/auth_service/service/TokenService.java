package com.ecommerce.auth_service.service;

import com.ecommerce.auth_service.constants.AuthConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 32;

    private final RedisTemplate<String, String> redisTemplate;


    public void storeAccessToken(UUID userId, String token, long expirationMs) {
        String key = AuthConstants.ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, token, expirationMs, TimeUnit.MILLISECONDS);
        log.debug("Access token stored for user: {}", userId);
    }

    public boolean isTokenValid(UUID userId, String token) {
        String key = AuthConstants.ACCESS_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return token.equals(storedToken);
    }

    public void revokeAccessToken(UUID userId) {
        String key = AuthConstants.ACCESS_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Access token revoked for user: {}", userId);
    }


    public String generateAndStoreRefreshToken(UUID userId, long expirationMs) {
        String randomPart = generateSecureToken();
        String refreshToken = userId + "." + randomPart;
        String key = AuthConstants.REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.opsForValue().set(key, refreshToken, expirationMs, TimeUnit.MILLISECONDS);
        log.debug("Refresh token stored for user: {}", userId);
        return refreshToken;
    }

    public UUID validateRefreshToken(String refreshToken) {
        int dotIndex = refreshToken.indexOf('.');
        if (dotIndex == -1) return null;

        UUID userId;
        try {
            userId = UUID.fromString(refreshToken.substring(0, dotIndex));
        } catch (IllegalArgumentException e) {
            return null;
        }

        String key = AuthConstants.REFRESH_TOKEN_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(key);
        return refreshToken.equals(storedToken) ? userId : null;
    }

    public void deleteRefreshToken(String refreshToken) {
        // Extract userId from token to build the key
        int dotIndex = refreshToken.indexOf('.');
        if (dotIndex == -1) return;
        try {
            UUID userId = UUID.fromString(refreshToken.substring(0, dotIndex));
            String key = AuthConstants.REFRESH_TOKEN_PREFIX + userId;
            redisTemplate.delete(key);
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse userId from refresh token during delete");
        }
    }

    public void deleteRefreshTokenByUserId(UUID userId) {
        String key = AuthConstants.REFRESH_TOKEN_PREFIX + userId;
        redisTemplate.delete(key);
        log.debug("Refresh token revoked for user: {}", userId);
    }


    public void storeActivationToken(String token, UUID userId, long expirationMs) {
        String key = AuthConstants.ACTIVATION_TOKEN_PREFIX + token;
        redisTemplate.opsForValue().set(key, userId.toString(), expirationMs, TimeUnit.MILLISECONDS);
        log.debug("Activation token stored for user: {}", userId);
    }

    public UUID validateActivationToken(String token) {
        String key = AuthConstants.ACTIVATION_TOKEN_PREFIX + token;
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? UUID.fromString(userId) : null;
    }

    public void deleteActivationToken(String token) {
        String key = AuthConstants.ACTIVATION_TOKEN_PREFIX + token;
        redisTemplate.delete(key);
    }


    public void storeResetToken(String resetToken, UUID userId, long expirationMs) {
        String key = AuthConstants.RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.opsForValue().set(key, userId.toString(), expirationMs, TimeUnit.MILLISECONDS);
    }

    public UUID validateResetToken(String resetToken) {
        String key = AuthConstants.RESET_TOKEN_PREFIX + resetToken;
        String userId = redisTemplate.opsForValue().get(key);
        return userId != null ? UUID.fromString(userId) : null;
    }

    public void deleteResetToken(String resetToken) {
        String key = AuthConstants.RESET_TOKEN_PREFIX + resetToken;
        redisTemplate.delete(key);
    }


    public void revokeAllTokens(UUID userId) {
        revokeAccessToken(userId);
        deleteRefreshTokenByUserId(userId);
        log.debug("All tokens revoked for user: {}", userId);
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
