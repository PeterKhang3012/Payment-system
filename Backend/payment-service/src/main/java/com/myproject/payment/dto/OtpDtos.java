
package com.myproject.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class OtpDtos {

    public record GenerateOtpRequest(
        @NotBlank(message = "paymentId is required")
        @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "paymentId must be a valid UUID"
        )
        String paymentId,

        @NotBlank(message = "email is required")
        String email
    ) {}

    public record GenerateOtpResponse(
        String paymentId,
        String email,
        String expiresAt,
        long expiresInSeconds,
        String message
    ) {}

    // API validation: require a generated payment UUID and six-digit OTP before service calls.
    public record VerifyOtpRequest(
        @NotBlank(message = "paymentId is required")
        @Pattern(
            regexp = "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$",
            message = "paymentId must be a valid UUID"
        )
        String paymentId,

        @NotBlank(message = "OTP code is required")
        @Pattern(
            regexp = "^\\d{6}$",
            message = "OTP code must contain exactly 6 digits"
        )
        String code
    ) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerifyOtpResponse(
        boolean success,
        String paymentId,
        String message,
        Integer remainingAttempts
    ) {}
}
