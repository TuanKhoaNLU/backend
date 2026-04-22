package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.SlideType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateQuizSlideRequest {

    @NotNull(message = "slide type is required")
    private SlideType type;

    @NotBlank(message = "slide question is required")
    @Size(max = 500, message = "slide question max length is 500")
    private String question;

    @Size(max = 500, message = "image url max length is 500")
    private String imageUrl;

    private List<String> options;
    private List<Integer> correctOptionIndexes;
    private List<String> orderingItems;
    private List<String> acceptedAnswers;

    public SlideType getType() {
        return type;
    }

    public void setType(SlideType type) {
        this.type = type;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public List<Integer> getCorrectOptionIndexes() {
        return correctOptionIndexes;
    }

    public void setCorrectOptionIndexes(List<Integer> correctOptionIndexes) {
        this.correctOptionIndexes = correctOptionIndexes;
    }

    public List<String> getOrderingItems() {
        return orderingItems;
    }

    public void setOrderingItems(List<String> orderingItems) {
        this.orderingItems = orderingItems;
    }

    public List<String> getAcceptedAnswers() {
        return acceptedAnswers;
    }

    public void setAcceptedAnswers(List<String> acceptedAnswers) {
        this.acceptedAnswers = acceptedAnswers;
    }
}
