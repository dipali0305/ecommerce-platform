package com.ecommerce.auth_service.exception;

public class TokenException extends RuntimeException {
    public TokenException(String message) {
        super(message);
    }
}
