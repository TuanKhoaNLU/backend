package com.quizweb.backend.quiz.dto;

import java.util.List;

public record QuizDetailResponse(
        Long id,
        String title,
        boolean published,
        String createdBy,
        List<QuizSlideDetailResponse> slides
) {
}
