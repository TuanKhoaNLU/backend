package com.quizweb.backend.attempt.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SubmitAttemptRequest {

    @NotBlank(message = "nickname is required")
    @Size(min = 2, max = 30, message = "nickname must be 2-30 characters")
    private String nickname;

    @NotNull(message = "quizId is required")
    private Long quizId;

    @NotNull(message = "totalDurationMs is required")
    private Long totalDurationMs;

    @Valid
    @NotEmpty(message = "answers are required")
    private List<AnswerSubmissionRequest> answers;

    public Long getQuizId() {
        return quizId;
    }

    public void setQuizId(Long quizId) {
        this.quizId = quizId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Long getTotalDurationMs() {
        return totalDurationMs;
    }

    public void setTotalDurationMs(Long totalDurationMs) {
        this.totalDurationMs = totalDurationMs;
    }

    public List<AnswerSubmissionRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<AnswerSubmissionRequest> answers) {
        this.answers = answers;
    }
}
