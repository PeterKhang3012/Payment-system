package com.myproject.payment.dto;
public final class UserDtos {

    private UserDtos() {}
    public record UserResponse(

        String userId,
        String fullName,
        String email,
        String phoneNumber,
        String createdAt
    ) {}
}
