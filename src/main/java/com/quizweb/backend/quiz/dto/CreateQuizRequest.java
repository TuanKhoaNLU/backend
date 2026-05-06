package com.quizweb.backend.quiz.dto;

import com.quizweb.backend.quiz.QuizMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateQuizRequest {

    @NotBlank(message = "quiz title is required")
    @Size(max = 150, message = "quiz title max length is 150")
    private String title;

    @NotNull(message = "quiz mode is required")
    private QuizMode mode = QuizMode.NORMAL;

    private Boolean published = true;
    private Integer totalTimeLimitSeconds;

    @Valid
    @NotEmpty(message = "quiz must have at least one slide")
    private List<CreateQuizSlideRequest> slides;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getPublished() {
        return published;
    }

    public void setPublished(Boolean published) {
        this.published = published;
    }

    public QuizMode getMode() {
        return mode;
    }

    public void setMode(QuizMode mode) {
        this.mode = mode;
    }

    public Integer getTotalTimeLimitSeconds() {
        return totalTimeLimitSeconds;
    }

    public void setTotalTimeLimitSeconds(Integer totalTimeLimitSeconds) {
        this.totalTimeLimitSeconds = totalTimeLimitSeconds;
    }

    public List<CreateQuizSlideRequest> getSlides() {
        return slides;
    }

    public void setSlides(List<CreateQuizSlideRequest> slides) {
        this.slides = slides;
    }
}
