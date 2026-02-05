package com.app.questofseoul.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BusinessException {
    public AuthorizationException(String message) {
        super(message, "AUTHORIZATION_FAILED", HttpStatus.FORBIDDEN);
    }
}
