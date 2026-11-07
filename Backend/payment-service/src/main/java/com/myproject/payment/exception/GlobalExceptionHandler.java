package com.myproject.payment.exception;

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


    // API error handling: preserve known downstream statuses, never forward downstream bodies or URLs.
    @ExceptionHandler(org.springframework.web.client.RestClientException.class)
    public ResponseEntity<java.util.Map<String, String>> handleDownstream(org.springframework.web.client.RestClientException ex) {
        int status = 502;
        String message = "Dependent service failed";
        if (ex instanceof org.springframework.web.client.RestClientResponseException response) {
            int code = response.getStatusCode().value();
            if (code == 404 || code == 410) { status = 404; message = "Requested resource is unavailable"; }
            else if (code == 409) { status = 409; message = "Resource state conflicts with this operation"; }
            else if (code == 429) { status = 429; message = "Too many attempts"; }
            else if (code == 503) { status = 503; message = "Dependent service is unavailable"; }
            else if (code == 401 || code == 403) { status = code; message = "Request authorization was rejected"; }
        }
        return ResponseEntity.status(status).body(java.util.Map.of("message", message));
    }

    // API error handling: classify existing state and ownership rejections explicitly.
    @ExceptionHandler(PaymentConflictException.class)
    public ResponseEntity<java.util.Map<String, String>> handleConflict(PaymentConflictException ex) {
        return ResponseEntity.status(409).body(java.util.Map.of("message", "Payment state conflicts with this operation"));
    }
    @ExceptionHandler(PaymentForbiddenException.class)
    public ResponseEntity<java.util.Map<String, String>> handleForbidden(PaymentForbiddenException ex) {
        return ResponseEntity.status(403).body(java.util.Map.of("message", "Access denied"));
    }

    // API error responses: use the same JSON message field for known business errors.
    @ExceptionHandler(TermsNotAcceptedException.class)
    public ResponseEntity<java.util.Map<String, String>> handleTermsNotAccepted(
            TermsNotAcceptedException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(TuitionAlreadyPaidException.class)
    public ResponseEntity<java.util.Map<String, String>> handleTuitionAlreadyPaid(
            TuitionAlreadyPaidException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

    // API error responses: preserve existing business statuses with a JSON message field.
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<java.util.Map<String, String>> handleInsufficientBalance(
            InsufficientBalanceException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<java.util.Map<String, String>> handlePaymentFailed(
            PaymentFailedException ex) {

        // Security: downstream OTP messages are not trusted client-facing error details.
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("message", "Payment could not be completed"));
    }

    // API error responses: expose only controlled business messages.
    @ExceptionHandler(TuitionNotFoundException.class)
    public ResponseEntity<java.util.Map<String, String>> handleTuitionNotFound(
            TuitionNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", ex.getMessage()));
    }

@ExceptionHandler(BalanceNotFoundException.class)
    public ResponseEntity<java.util.Map<String, String>> handleBalanceNotFound(
            BalanceNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", ex.getMessage()));
    }
    
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<java.util.Map<String, String>> BadRequestResponse(
            BadRequestException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(java.util.Map.of("message", ex.getMessage()));
    } 

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<java.util.Map<String, String>> PaymentNotFound(
            PaymentNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(java.util.Map.of("message", ex.getMessage()));
    } 
}
