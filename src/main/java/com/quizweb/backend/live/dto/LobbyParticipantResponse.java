package com.quizweb.backend.live.dto;

public record LobbyParticipantResponse(
        Long participantId,
        String displayName,
        String role,
        boolean host,
        boolean connected
) {
}
