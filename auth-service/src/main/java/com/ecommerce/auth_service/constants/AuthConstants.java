package com.ecommerce.auth_service.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AuthConstants {

    // HTTP Headers
    public static final String AUTH_HEADER = "Authorization";
    public static final String BEARER_PREFIX = "Bearer ";

    // Redis key prefixes
    public static final String ACCESS_TOKEN_PREFIX = "access_token:";
    public static final String REFRESH_TOKEN_PREFIX = "refresh_token:";
    public static final String RESET_TOKEN_PREFIX = "reset_token:";
    public static final String ACTIVATION_TOKEN_PREFIX = "activation_token:";

    // Token type
    public static final String TOKEN_TYPE_BEARER = "Bearer";

    // API Response status
    public static final String RESPONSE_SUCCESS = "success";
    public static final String RESPONSE_ERROR = "error";

    // User status
    public static final String STATUS_ACTIVE = "ACTIVE";
    public static final String STATUS_INACTIVE = "INACTIVE";
    public static final String STATUS_SUSPENDED = "SUSPENDED";

    // Account lock
    public static final int MAX_FAILED_LOGIN_ATTEMPTS = 3;

    // JWT claim keys
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_ROLES = "roles";
}
