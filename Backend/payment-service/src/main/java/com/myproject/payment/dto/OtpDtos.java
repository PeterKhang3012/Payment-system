package com.myproject.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * DTO dùng để giao tiếp giữa payment-service và otp-service.
 */
public class OtpDtos {

    /**
     * Request gửi đến:
     * POST /api/otp/generate
     */
    public record GenerateOtpRequest(
        String paymentId,
        String email
    ) {}

    /**
     * Response nhận từ:
     * POST /api/otp/generate
     */
    public record GenerateOtpResponse(
        String paymentId,
        String email,
        String expiresAt,
        long expiresInSeconds,
        String message
    ) {}

    /**
     * Request gửi đến:
     * POST /api/otp/verify
     */
    public record VerifyOtpRequest(
        String paymentId,
        String code
    ) {}

    /**
     * Response nhận từ:
     * POST /api/otp/verify
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerifyOtpResponse(
        boolean success,
        String paymentId,
        String message,
        Integer remainingAttempts
    ) {}
}
