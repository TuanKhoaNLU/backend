package com.quizweb.backend.live.dto;

import com.quizweb.backend.attempt.dto.AnswerSubmissionRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class SubmitLiveAnswerRequest {

    @NotNull(message = "participantId is required")
    private Long participantId;

    @NotNull(message = "answer is required")
    @Valid
    private AnswerSubmissionRequest answer;

    public Long getParticipantId() {
        return participantId;
    }

    public void setParticipantId(Long participantId) {
        this.participantId = participantId;
    }

    public AnswerSubmissionRequest getAnswer() {
        return answer;
    }

    public void setAnswer(AnswerSubmissionRequest answer) {
        this.answer = answer;
    }
}
