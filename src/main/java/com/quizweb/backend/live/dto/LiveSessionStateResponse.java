package com.quizweb.backend.live.dto;

import com.quizweb.backend.attempt.dto.PlaySlideResponse;
import com.quizweb.backend.live.LiveSessionStatus;

import java.time.Instant;
import java.util.List;

public record LiveSessionStateResponse(
        Long sessionId,
        String pin,
        Long quizId,
        String quizTitle,
        String hostUsername,
        LiveSessionStatus status,
        int currentQuestionIndex,
        int totalQuestions,
        Boolean allowLateJoin,
        Instant startedAt,
        Instant endedAt,
        Instant currentSlideStartedAt,
        List<LobbyParticipantResponse> participants,
        PlaySlideResponse currentSlide
) {
}
