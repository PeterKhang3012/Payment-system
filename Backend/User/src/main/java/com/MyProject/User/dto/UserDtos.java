package com.MyProject.User.dto;

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
