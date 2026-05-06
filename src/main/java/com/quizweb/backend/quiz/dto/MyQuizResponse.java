package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.QuizMode;

public record MyQuizResponse(
        Long id,
        String title,
        boolean published,
        int slideCount,
        QuizMode mode
) {
}
