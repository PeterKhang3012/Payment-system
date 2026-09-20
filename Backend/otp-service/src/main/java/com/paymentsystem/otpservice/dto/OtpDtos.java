package com.paymentsystem.otpservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Tập hợp tất cả DTO dưới dạng Java record. */
public class OtpDtos {

    /** POST /api/otp/generate */
    public record GenerateOtpRequest(
        @NotBlank(message = "Payment ID la bat buoc")
        String paymentId,

        @NotBlank(message = "Email la bat buoc")
        @Email(message = "Email khong dung dinh dang")
        String email
    ) {}

    /** POST /api/otp/verify */
    public record VerifyOtpRequest(
        @NotBlank(message = "Payment ID la bat buoc")
        String paymentId,

        @NotBlank(message = "Ma OTP la bat buoc")
        @Pattern(regexp = "\\d{6}", message = "Ma OTP phai gom dung 6 chu so (0-9)")
        String code
    ) {}

    /** Response sau khi sinh OTP — code KHÔNG có trong response, chỉ gửi qua email. */
    public record GenerateOtpResponse(
        String paymentId,
        String email,           // email đã mask
        String expiresAt,
        long   expiresInSeconds,
        String message
    ) {}

    /** Response sau khi verify OTP. remainingAttempts = null khi success = true. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record VerifyOtpResponse(
        boolean success,
        String  paymentId,
        String  message,
        Integer remainingAttempts
    ) {}
}
