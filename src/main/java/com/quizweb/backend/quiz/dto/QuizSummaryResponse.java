package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.QuizMode;

public record QuizSummaryResponse(
        Long id,
        String title,
        boolean published,
        QuizMode mode
) {
}
