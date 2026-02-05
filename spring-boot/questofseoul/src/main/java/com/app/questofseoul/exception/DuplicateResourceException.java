package com.app.questofseoul.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends BusinessException {
    public DuplicateResourceException(String resourceName, Object identifier) {
        super(
            String.format("%s가 이미 존재합니다: %s", resourceName, identifier),
            "DUPLICATE_RESOURCE",
            HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super(message, "DUPLICATE_RESOURCE", HttpStatus.CONFLICT);
    }
}
