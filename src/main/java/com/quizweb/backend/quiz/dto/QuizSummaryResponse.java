package com.quizweb.backend.quiz.dto;

public record QuizSummaryResponse(
        Long id,
        String title,
        boolean published
) {
}
