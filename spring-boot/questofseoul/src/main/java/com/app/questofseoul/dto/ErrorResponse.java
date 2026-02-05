package com.app.questofseoul.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private final String error;
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;
    private final String path;
    private final Map<String, String> errors; // 필드별 검증 에러

    public static ErrorResponse of(String error, String errorCode, String message) {
        return ErrorResponse.builder()
            .error(error)
            .errorCode(errorCode)
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();
    }

    public static ErrorResponse of(String error, String errorCode, String message, String path) {
        return ErrorResponse.builder()
            .error(error)
            .errorCode(errorCode)
            .message(message)
            .timestamp(LocalDateTime.now())
            .path(path)
            .build();
    }

    public static ErrorResponse of(String error, String errorCode, String message, Map<String, String> errors) {
        return ErrorResponse.builder()
            .error(error)
            .errorCode(errorCode)
            .message(message)
            .timestamp(LocalDateTime.now())
            .errors(errors)
            .build();
    }
}
