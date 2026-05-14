package com.quizweb.backend.live.dto;

public record LiveLeaderboardEntryResponse(
        int rank,
        Long participantId,
        String displayName,
        int score,
        int correctCount,
        boolean connected
) {
}
