package com.quizweb.backend.quiz.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AiGenerateRequest {

    @Min(value = 1, message = "Must generate at least 1 question")
    @Max(value = 20, message = "Cannot generate more than 20 questions")
    private int numberOfQuestions;

    @NotBlank(message = "Quiz topic/title is required")
    @Size(max = 150, message = "Quiz topic/title must be under 150 characters")
    private String quizTitle;

    @Min(value = 1, message = "Time limit must be at least 1 second")
    @Max(value = 10, message = "Time limit cannot exceed 10 seconds")
    private Integer timeLimitSeconds;

    public int getNumberOfQuestions() {
        return numberOfQuestions;
    }

    public void setNumberOfQuestions(int numberOfQuestions) {
        this.numberOfQuestions = numberOfQuestions;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public void setQuizTitle(String quizTitle) {
        this.quizTitle = quizTitle;
    }

    public Integer getTimeLimitSeconds() {
        return timeLimitSeconds;
    }

    public void setTimeLimitSeconds(Integer timeLimitSeconds) {
        this.timeLimitSeconds = timeLimitSeconds;
    }
}
