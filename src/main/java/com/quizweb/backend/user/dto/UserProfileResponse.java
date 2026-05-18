package com.quizweb.backend.user.dto;

public record UserProfileResponse(
        String username,
        String fullName,
        String phoneNumber,
        String avatarUrl,
        String role
) {}
