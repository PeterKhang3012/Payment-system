package com.paymentsystem.userservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/** Gộp tất cả exception + handler vào một file. */
public final class GlobalException {

    private GlobalException() {}

    // ── Error Response DTO ──────────────────────────────
    public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}

    // ── Exceptions ──────────────────────────────────────

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String resource, String field, Object value) {
            super(resource + " khong tim thay voi " + field + " = '" + value + "'");
        }
    }

    /** 409 — Cố thanh toán học phí đã PAID (double-payment). */
    @ResponseStatus(HttpStatus.CONFLICT)
    public static class TuitionAlreadyPaidException extends RuntimeException {
        public TuitionAlreadyPaidException(Long tuitionId, String existingPaymentId) {
            super("Hoc phi ID=" + tuitionId + " da duoc thanh toan (paymentId=" + existingPaymentId + "). Khong the thanh toan lai.");
        }
    }

    /** 401 — Header X-Internal-Service-Token không hợp lệ. */
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public static class InvalidServiceTokenException extends RuntimeException {
        public InvalidServiceTokenException() {
            super("Header 'X-Internal-Service-Token' khong hop le hoac bi thieu.");
        }
    }

    // ── Global Handler ───────────────────────────────────

    @RestControllerAdvice
    public static class Handler {

        private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
            String message = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining("; "));
            return respond(HttpStatus.BAD_REQUEST, "Validation Failed", message, req);
        }

        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
            return respond(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest req) {
            return respond(HttpStatus.UNAUTHORIZED, "Unauthorized", "Username hoac password khong dung.", req);
        }

        @ExceptionHandler(LockedException.class)
        public ResponseEntity<ErrorResponse> handleLocked(LockedException ex, HttpServletRequest req) {
            return respond(HttpStatus.UNAUTHORIZED, "Account Locked", "Tai khoan cua ban da bi khoa. Vui long lien he quan tri vien.", req);
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ErrorResponse> handleDisabled(DisabledException ex, HttpServletRequest req) {
            return respond(HttpStatus.UNAUTHORIZED, "Account Disabled", "Tai khoan cua ban da bi vo hieu hoa.", req);
        }

        @ExceptionHandler(TuitionAlreadyPaidException.class)
        public ResponseEntity<ErrorResponse> handleAlreadyPaid(TuitionAlreadyPaidException ex, HttpServletRequest req) {
            return respond(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), req);
        }

        @ExceptionHandler(InvalidServiceTokenException.class)
        public ResponseEntity<ErrorResponse> handleInvalidToken(InvalidServiceTokenException ex, HttpServletRequest req) {
            return respond(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), req);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArg(IllegalArgumentException ex, HttpServletRequest req) {
            return respond(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), req);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
            return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                    "Da xay ra loi khong mong muon. Vui long thu lai.", req);
        }

        private ResponseEntity<ErrorResponse> respond(HttpStatus status, String error, String message, HttpServletRequest req) {
            return ResponseEntity.status(status).body(new ErrorResponse(
                    LocalDateTime.now().format(FMT), status.value(), error, message, req.getRequestURI()));
        }
    }
}
