package com.quizweb.backend.attempt.dto;

import com.quizweb.backend.quiz.QuizMode;

import java.util.List;

public record PlayQuizResponse(
        Long quizId,
        String title,
        QuizMode mode,
        Integer totalTimeLimitSeconds,
        List<PlaySlideResponse> slides
) {
}
