package com.quizweb.backend.live.dto;

import com.quizweb.backend.live.LiveParticipantRole;

public record JoinLiveSessionResponse(
        Long participantId,
        String displayName,
        LiveParticipantRole role,
        LiveSessionStateResponse session
) {
}
