package com.quizweb.backend.quiz.dto;

public record CreateQuizResponse(
        Long id,
        String title,
        int slideCount,
        String createdBy
) {
}
