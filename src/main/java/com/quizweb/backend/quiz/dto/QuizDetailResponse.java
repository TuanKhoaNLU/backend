package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.QuizMode;

import java.util.List;

public record QuizDetailResponse(
        Long id,
        String title,
        boolean published,
        QuizMode mode,
        Integer totalTimeLimitSeconds,
        String createdBy,
        List<QuizSlideDetailResponse> slides
) {
}
