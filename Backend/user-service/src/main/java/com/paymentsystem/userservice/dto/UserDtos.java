package com.paymentsystem.userservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.paymentsystem.userservice.entity.TuitionStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Tập hợp tất cả DTO dưới dạng Java record. */
public final class UserDtos {

    private UserDtos() {}

    /** POST /api/auth/login */
    public record LoginRequest(
        @NotBlank(message = "Username la bat buoc")
        String username,

        @NotBlank(message = "Password la bat buoc")
        String password
    ) {}

    /** PATCH /api/tuitions/{id}/status — API nội bộ cho payment-service. */
    public record UpdateTuitionStatusRequest(
        @NotNull(message = "Status la bat buoc")
        TuitionStatus status,

        @NotBlank(message = "Payment ID la bat buoc")
        String paymentId
    ) {}

    /** Thông tin sinh viên — không chứa passwordHash. */
    public record UserResponse(
        Long   id,
        String studentId,
        String username,
        String fullName,
        String email,
        String createdAt
    ) {}

    /** Response đăng nhập: JWT token + thông tin user. */
    public record LoginResponse(
        String       token,
        String       tokenType,
        long         expiresIn,
        UserResponse user
    ) {}

    /** paymentId = null khi chưa thanh toán → bị loại khỏi JSON. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record TuitionResponse(
        Long       id,
        String     studentId,
        String     semester,
        BigDecimal amount,
        String     status,
        String     description,
        String     dueDate,
        String     paymentId,
        String     createdAt
    ) {}
}
