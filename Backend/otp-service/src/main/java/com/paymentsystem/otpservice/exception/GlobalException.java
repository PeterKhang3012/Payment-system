package com.paymentsystem.otpservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * Gộp tất cả exception + handler vào một file.
 * Mỗi exception là nested static class.
 */
public final class GlobalException {

    private GlobalException() {}

    // ── Error Response DTO ──────────────────────────────
    public record ErrorResponse(String timestamp, int status, String error, String message, String path) {}

    // ── Exceptions ──────────────────────────────────────

    @ResponseStatus(HttpStatus.NOT_FOUND)
    public static class OtpNotFoundException extends RuntimeException {
        public OtpNotFoundException(String paymentId) {
            super("Khong tim thay OTP cho paymentId: '" + paymentId + "'. Vui long sinh OTP moi.");
        }
    }

    @ResponseStatus(HttpStatus.GONE)
    public static class OtpExpiredException extends RuntimeException {
        public OtpExpiredException(String paymentId) {
            super("OTP cho paymentId '" + paymentId + "' da het han. Vui long sinh OTP moi.");
        }
    }

    @ResponseStatus(HttpStatus.CONFLICT)
    public static class OtpAlreadyUsedException extends RuntimeException {
        public OtpAlreadyUsedException(String paymentId) {
            super("OTP cho paymentId '" + paymentId + "' da duoc su dung. Moi OTP chi dung duoc 1 lan.");
        }
    }

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    public static class OtpMaxAttemptsExceededException extends RuntimeException {
        public OtpMaxAttemptsExceededException(String paymentId, int maxAttempts) {
            super("OTP cho paymentId '" + paymentId + "' bi khoa sau " + maxAttempts + " lan nhap sai. Vui long sinh OTP moi.");
        }
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public static class EmailSendException extends RuntimeException {
        public EmailSendException(String email, Throwable cause) {
            super("Khong the gui email OTP den: " + maskEmail(email) + ". Kiem tra cau hinh SMTP.", cause);
        }

        private static String maskEmail(String email) {
            if (email == null) return "[unknown]";
            int at = email.indexOf('@');
            return (at <= 2) ? email : email.substring(0, 2) + "****" + email.substring(at);
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

        @ExceptionHandler(OtpNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(OtpNotFoundException ex, HttpServletRequest req) {
            return respond(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), req);
        }

        @ExceptionHandler(OtpExpiredException.class)
        public ResponseEntity<ErrorResponse> handleExpired(OtpExpiredException ex, HttpServletRequest req) {
            return respond(HttpStatus.GONE, "OTP Expired", ex.getMessage(), req);
        }

        @ExceptionHandler(OtpAlreadyUsedException.class)
        public ResponseEntity<ErrorResponse> handleAlreadyUsed(OtpAlreadyUsedException ex, HttpServletRequest req) {
            return respond(HttpStatus.CONFLICT, "OTP Already Used", ex.getMessage(), req);
        }

        @ExceptionHandler(OtpMaxAttemptsExceededException.class)
        public ResponseEntity<ErrorResponse> handleMaxAttempts(OtpMaxAttemptsExceededException ex, HttpServletRequest req) {
            return respond(HttpStatus.TOO_MANY_REQUESTS, "Too Many Attempts", ex.getMessage(), req);
        }

        @ExceptionHandler(EmailSendException.class)
        public ResponseEntity<ErrorResponse> handleEmailFail(EmailSendException ex, HttpServletRequest req) {
            return respond(HttpStatus.SERVICE_UNAVAILABLE, "Email Service Unavailable", ex.getMessage(), req);
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
