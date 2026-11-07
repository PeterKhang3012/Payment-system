package com.myproject.payment.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentSuccessfulEvent(

    @NotBlank
    String paymentId,

    @NotBlank
    String userId,

    @NotBlank
    @Email
    String recipientEmail,

    @NotBlank
    String fullName,

    @NotBlank
    String studentName,

    @NotBlank
    String studentId,

    @NotNull
    @DecimalMin(value = "0.01")
    BigDecimal amount,

    @NotNull
    LocalDateTime paidAt

) {}
