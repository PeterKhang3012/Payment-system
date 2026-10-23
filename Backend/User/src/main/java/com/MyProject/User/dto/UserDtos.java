package com.MyProject.User.dto;

public final class UserDtos {

    private UserDtos() {}
    public record UserResponse(
        String fullName,
        String email,
        String phoneNumber,
        String createdAt
    ) {}
}
