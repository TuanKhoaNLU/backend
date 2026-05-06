package com.quizweb.backend.attempt.dto;

public record LeaderboardEntryResponse(
        int rank,
        String username,
        int score,
        int correctCount,
        long totalDurationMs
) {
}
