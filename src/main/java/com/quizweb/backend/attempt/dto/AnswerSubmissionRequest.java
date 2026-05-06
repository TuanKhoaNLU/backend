package com.quizweb.backend.attempt.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public class AnswerSubmissionRequest {

    @NotNull(message = "slideId is required")
    private Long slideId;

    private List<Integer> selectedOptionIndexes;
    private List<String> orderedItems;
    private String textAnswer;

    @NotNull(message = "elapsedMs is required")
    private Long elapsedMs;

    public Long getSlideId() {
        return slideId;
    }

    public void setSlideId(Long slideId) {
        this.slideId = slideId;
    }

    public List<Integer> getSelectedOptionIndexes() {
        return selectedOptionIndexes;
    }

    public void setSelectedOptionIndexes(List<Integer> selectedOptionIndexes) {
        this.selectedOptionIndexes = selectedOptionIndexes;
    }

    public List<String> getOrderedItems() {
        return orderedItems;
    }

    public void setOrderedItems(List<String> orderedItems) {
        this.orderedItems = orderedItems;
    }

    public String getTextAnswer() {
        return textAnswer;
    }

    public void setTextAnswer(String textAnswer) {
        this.textAnswer = textAnswer;
    }

    public Long getElapsedMs() {
        return elapsedMs;
    }

    public void setElapsedMs(Long elapsedMs) {
        this.elapsedMs = elapsedMs;
    }
}
