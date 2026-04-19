package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.SlideType;

import java.util.List;

public record QuizSlideDetailResponse(
        Long id,
        int positionIndex,
        SlideType type,
        String question,
        String imageUrl,
        List<String> options,
        List<Integer> correctOptionIndexes,
        List<String> orderingItems,
        List<String> acceptedAnswers
) {
}
