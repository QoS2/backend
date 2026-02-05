package com.app.questofseoul.exception;

import com.app.questofseoul.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e, HttpServletRequest request) {
        log.error("Business exception: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            e.getStatus().getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(e.getStatus()).body(errorResponse);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException e, HttpServletRequest request) {
        log.error("Resource not found: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
            NoResourceFoundException e, HttpServletRequest request) {
        log.debug("No static resource: {}", e.getResourcePath());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.getReasonPhrase(),
            "NOT_FOUND",
            "요청한 리소스를 찾을 수 없습니다.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResource(
            DuplicateResourceException e, HttpServletRequest request) {
        log.error("Duplicate resource: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT.getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(com.app.questofseoul.exception.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleCustomAuthentication(
            com.app.questofseoul.exception.AuthenticationException e, HttpServletRequest request) {
        // 비로그인 상태에서 /auth/me 등 호출 시 발생하는 정상 401은 DEBUG로만 기록
        if (request.getRequestURI() != null && request.getRequestURI().endsWith("/auth/me")) {
            log.debug("Unauthenticated request: {}", request.getRequestURI());
        } else {
            log.error("Authentication failed: {}", e.getMessage());
        }
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSpringAuthentication(
            org.springframework.security.core.AuthenticationException e, HttpServletRequest request) {
        log.error("Spring Security authentication failed: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.getReasonPhrase(),
            "AUTHENTICATION_FAILED",
            "인증에 실패했습니다. 토큰을 확인해주세요.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<ErrorResponse> handleAuthorization(
            AuthorizationException e, HttpServletRequest request) {
        log.error("Authorization failed: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.FORBIDDEN.getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            ValidationException e, HttpServletRequest request) {
        log.error("Validation failed: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            e.getErrorCode(),
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException e, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage != null ? errorMessage : "유효하지 않은 값입니다");
        });
        
        log.error("Validation error: {}", errors);
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "VALIDATION_FAILED",
            "입력값 검증에 실패했습니다.",
            errors
        );
        errorResponse = ErrorResponse.builder()
            .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
            .errorCode("VALIDATION_FAILED")
            .message("입력값 검증에 실패했습니다.")
            .timestamp(errorResponse.getTimestamp())
            .path(request.getRequestURI())
            .errors(errors)
            .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
            HttpMessageNotReadableException e, HttpServletRequest request) {
        log.error("Invalid request body: {}", e.getMessage());
        String message = "잘못된 요청 형식입니다. JSON 형식을 확인해주세요.";
        if (e.getMessage() != null && e.getMessage().contains("JSON parse error")) {
            message = "JSON 형식이 올바르지 않습니다. 요청 본문을 확인해주세요.";
        }
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "INVALID_REQUEST_BODY",
            message,
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException e, HttpServletRequest request) {
        log.error("Type mismatch: {}", e.getMessage());
        String message = String.format("파라미터 '%s'의 타입이 올바르지 않습니다.", e.getName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "INVALID_PARAMETER_TYPE",
            message,
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException e, HttpServletRequest request) {
        log.error("Illegal argument: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.getReasonPhrase(),
            "ILLEGAL_ARGUMENT",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.badRequest().body(errorResponse);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalState(
            IllegalStateException e, HttpServletRequest request) {
        log.error("Illegal state: {}", e.getMessage());
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.CONFLICT.getReasonPhrase(),
            "ILLEGAL_STATE",
            e.getMessage(),
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception e, HttpServletRequest request) {
        log.error("Unexpected error", e);
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
            "INTERNAL_SERVER_ERROR",
            "서버에 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
            request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
}
