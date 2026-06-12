package com.example.cryoguard.shared.infrastructure.exception;

import com.example.cryoguard.shared.domain.exceptions.BusinessException;
import com.example.cryoguard.shared.domain.exceptions.DuplicateUserException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler that maps business exceptions to controlled
 * HTTP responses with descriptive, consistent error bodies. Also catches
 * unhandled RuntimeException as a safety net (logged + 500).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicate(DuplicateUserException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req, ex.getField());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Map<String, Object>> handleBusiness(BusinessException ex, HttpServletRequest req) {
        return build(ex.getStatus(), ex.getMessage(), req, ex.getField());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), req, null);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleAny(RuntimeException ex, HttpServletRequest req) {
        // Log the full stack so the operator can debug, but return a controlled body
        org.slf4j.LoggerFactory.getLogger(GlobalExceptionHandler.class)
            .error("Unhandled exception at {}: {}", req.getRequestURI(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please contact the administrator.",
            req, null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message, HttpServletRequest req, String field) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", Instant.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        if (field != null) body.put("field", field);
        body.put("path", req.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
