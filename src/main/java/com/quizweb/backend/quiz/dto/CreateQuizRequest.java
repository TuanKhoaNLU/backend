package com.quizweb.backend.quiz.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public class CreateQuizRequest {

    @NotBlank(message = "quiz title is required")
    @Size(max = 150, message = "quiz title max length is 150")
    private String title;

    private Boolean published = true;

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

    public List<CreateQuizSlideRequest> getSlides() {
        return slides;
    }

    public void setSlides(List<CreateQuizSlideRequest> slides) {
        this.slides = slides;
    }
}
