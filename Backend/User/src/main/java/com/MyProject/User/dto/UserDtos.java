package com.MyProject.User.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class UserDtos {

    private UserDtos() {}
    public record UserResponse(

        String studentId,
        String userId,
        String fullName,
        String email,
        String phoneNumber,
        String createdAt
    ) {}
}
