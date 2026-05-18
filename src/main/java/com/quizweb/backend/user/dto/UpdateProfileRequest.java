package com.quizweb.backend.user.dto;

public record UpdateProfileRequest(
        String fullName,
        String phoneNumber,
        String avatarUrl
) {}
