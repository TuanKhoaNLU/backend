package com.quizweb.backend.auth.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresInMs
) {
}
