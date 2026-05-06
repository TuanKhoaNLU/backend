package com.quizweb.backend.attempt.dto;

import java.util.List;

public record SubmitAttemptResponse(
        Long attemptId,
        String mode,
        int score,
        int correctCount,
        int totalQuestions,
        long totalDurationMs,
        List<LeaderboardEntryResponse> leaderboard
) {
}
