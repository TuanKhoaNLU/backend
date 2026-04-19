package com.quizweb.backend.quiz;

import com.quizweb.backend.quiz.dto.CreateQuizRequest;
import com.quizweb.backend.quiz.dto.CreateQuizResponse;
import com.quizweb.backend.quiz.dto.MyQuizResponse;
import com.quizweb.backend.quiz.dto.QuizDetailResponse;
import com.quizweb.backend.quiz.dto.QuizSummaryResponse;
import com.quizweb.backend.quiz.dto.UpdateQuizRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @GetMapping
    public ResponseEntity<List<QuizSummaryResponse>> getPublicQuizzes() {
        return ResponseEntity.ok(quizService.getPublicQuizzes());
    }

    @PostMapping
    public ResponseEntity<CreateQuizResponse> createQuiz(
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.createQuiz(request, authentication.getName()));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyQuizResponse>> getMyQuizzes(Authentication authentication) {
        return ResponseEntity.ok(quizService.getMyQuizzes(authentication.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MyQuizResponse> updateQuiz(
            @PathVariable Long id,
            @Valid @RequestBody UpdateQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.updateQuiz(id, request, authentication.getName()));
    }

    @GetMapping("/{id}/mine")
    public ResponseEntity<QuizDetailResponse> getMyQuizDetail(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(quizService.getMyQuizDetail(id, authentication.getName()));
    }

    @PutMapping("/{id}/content")
    public ResponseEntity<QuizDetailResponse> replaceQuizContent(
            @PathVariable Long id,
            @Valid @RequestBody CreateQuizRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(quizService.replaceQuizContent(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/hide")
    public ResponseEntity<MyQuizResponse> hideQuiz(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(quizService.hideQuiz(id, authentication.getName()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Long id, Authentication authentication) {
        quizService.deleteQuiz(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
