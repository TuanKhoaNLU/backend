package com.quizweb.backend.quiz.dto;

public record MyQuizResponse(
        Long id,
        String title,
        boolean published,
        int slideCount
) {
}
