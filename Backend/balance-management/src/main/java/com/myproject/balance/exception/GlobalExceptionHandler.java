package com.myproject.balance.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler extends org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler {

    // API error handling: normalize framework errors without exposing parser or binding details.
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
            org.springframework.http.HttpHeaders headers, org.springframework.http.HttpStatusCode status,
            org.springframework.web.context.request.WebRequest request) {
        return new ResponseEntity<>(java.util.Map.of("message",
                status.is5xxServerError() ? "Internal server error" : "Invalid request"), headers, status);
    }

    // API validation: method constraints must produce 400 before business processing.
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    public ResponseEntity<java.util.Map<String, String>> handleConstraint(Exception ex) {
        return ResponseEntity.badRequest().body(java.util.Map.of("message", "Invalid request"));
    }

    // Security: unexpected failures must never expose internal exception details.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<java.util.Map<String, String>> handleUnexpected(Exception ex) {
        if (ex instanceof org.springframework.security.access.AccessDeniedException) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Access denied"));
        }
        if (ex instanceof org.springframework.security.core.AuthenticationException) {
            return ResponseEntity.status(401).body(java.util.Map.of("message", "Authentication required"));
        }
        return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Internal server error"));
    }


    // API error responses: preserve existing business statuses with a JSON message field.
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<java.util.Map<String, String>> handleInsufficientBalance(
            InsufficientBalanceException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(BalanceNotFoundException.class)
    public ResponseEntity<java.util.Map<String, String>> handleBalanceNotFound(
            BalanceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(PaymentDeductionConflictException.class)
    public ResponseEntity<java.util.Map<String, String>> handlePaymentDeductionConflict(
            PaymentDeductionConflictException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("message", ex.getMessage()));
    }
}