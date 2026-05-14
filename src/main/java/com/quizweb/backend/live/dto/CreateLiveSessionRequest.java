package com.quizweb.backend.live.dto;

import jakarta.validation.constraints.NotNull;

public class CreateLiveSessionRequest {

    @NotNull(message = "quizId is required")
    private Long quizId;

    private Boolean allowLateJoin = true;

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public Boolean getAllowLateJoin() {
        return allowLateJoin;
    }

    public void setAllowLateJoin(Boolean allowLateJoin) {
        this.allowLateJoin = allowLateJoin;
    }
}
