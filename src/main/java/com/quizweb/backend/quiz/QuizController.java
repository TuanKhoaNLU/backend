package com.quizweb.backend.quiz;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getPublicQuizzes() {
        return ResponseEntity.ok(List.of(
                Map.of("id", 1, "title", "Java Basics", "published", true),
                Map.of("id", 2, "title", "Spring Boot Intro", "published", true)
        ));
    }
}
