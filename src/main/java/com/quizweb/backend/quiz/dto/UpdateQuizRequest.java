package com.quizweb.backend.quiz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateQuizRequest {

    @NotBlank(message = "quiz title is required")
    @Size(max = 150, message = "quiz title max length is 150")
    private String title;

    @NotNull(message = "published is required")
    private Boolean published;

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
}
