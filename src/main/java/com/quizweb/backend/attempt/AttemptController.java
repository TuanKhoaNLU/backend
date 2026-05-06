package com.quizweb.backend.attempt;

import com.quizweb.backend.attempt.dto.LeaderboardEntryResponse;
import com.quizweb.backend.attempt.dto.PlayQuizResponse;
import com.quizweb.backend.attempt.dto.SubmitAttemptRequest;
import com.quizweb.backend.attempt.dto.SubmitAttemptResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attempts")
public class AttemptController {

    private final AttemptService attemptService;

    public AttemptController(AttemptService attemptService) {
        this.attemptService = attemptService;
    }

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        return ResponseEntity.ok(Map.of("message", "Attempt module is ready"));
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<PlayQuizResponse> getPlayableQuiz(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.getPlayableQuiz(quizId));
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitAttemptResponse> submit(@Valid @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.ok(attemptService.submitAttempt(request));
    }

    @PostMapping("/preview")
    public ResponseEntity<SubmitAttemptResponse> preview(@Valid @RequestBody SubmitAttemptRequest request) {
        return ResponseEntity.ok(attemptService.previewScore(request));
    }

    @GetMapping("/leaderboard/{quizId}")
    public ResponseEntity<List<LeaderboardEntryResponse>> leaderboard(@PathVariable Long quizId) {
        return ResponseEntity.ok(attemptService.getLeaderboard(quizId));
    }
}
