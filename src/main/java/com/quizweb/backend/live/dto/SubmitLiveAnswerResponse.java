package com.quizweb.backend.live.dto;

import java.util.List;

public record SubmitLiveAnswerResponse(
        boolean correct,
        int scoreEarned,
        int participantTotalScore,
        int participantCorrectCount,
        List<LiveLeaderboardEntryResponse> leaderboard
) {
}
