package com.quizweb.backend.attempt.dto;

import com.quizweb.backend.quiz.SlideType;

import java.util.List;

public record PlaySlideResponse(
        Long slideId,
        int positionIndex,
        SlideType type,
        String question,
        String imageUrl,
        Integer timeLimitSeconds,
        Integer revealDurationMs,
        List<String> options,
        List<Integer> correctOptionIndexes,
        List<String> correctOrderingItems,
        List<String> acceptedAnswers
) {
}
