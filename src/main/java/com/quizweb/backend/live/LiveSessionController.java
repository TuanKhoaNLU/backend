package com.quizweb.backend.live;

import com.quizweb.backend.live.dto.CreateLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionResponse;
import com.quizweb.backend.live.dto.LiveLeaderboardEntryResponse;
import com.quizweb.backend.live.dto.LiveSessionStateResponse;
import com.quizweb.backend.live.dto.SubmitLiveAnswerRequest;
import com.quizweb.backend.live.dto.SubmitLiveAnswerResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/live/sessions")
public class LiveSessionController {

    private final LiveSessionService liveSessionService;

    public LiveSessionController(LiveSessionService liveSessionService) {
        this.liveSessionService = liveSessionService;
    }

    @PostMapping
    public ResponseEntity<LiveSessionStateResponse> createSession(
            @Valid @RequestBody CreateLiveSessionRequest request,
            Authentication authentication
    ) {
        return ResponseEntity.ok(liveSessionService.createSession(request, authentication.getName()));
    }

    @PostMapping("/join")
    public ResponseEntity<JoinLiveSessionResponse> joinByPin(@Valid @RequestBody JoinLiveSessionRequest request) {
        return ResponseEntity.ok(liveSessionService.joinByPin(request));
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<LiveSessionStateResponse> start(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(liveSessionService.startSession(sessionId, authentication.getName()));
    }

    @PostMapping("/{sessionId}/next")
    public ResponseEntity<LiveSessionStateResponse> next(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(liveSessionService.nextQuestion(sessionId, authentication.getName()));
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<LiveSessionStateResponse> end(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(liveSessionService.endSession(sessionId, authentication.getName()));
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<LiveSessionStateResponse> state(@PathVariable Long sessionId) {
        return ResponseEntity.ok(liveSessionService.getSessionState(sessionId));
    }

    @PostMapping("/{sessionId}/answers")
    public ResponseEntity<SubmitLiveAnswerResponse> submitAnswer(
            @PathVariable Long sessionId,
            @Valid @RequestBody SubmitLiveAnswerRequest request
    ) {
        return ResponseEntity.ok(liveSessionService.submitAnswer(sessionId, request));
    }

    @GetMapping("/{sessionId}/leaderboard")
    public ResponseEntity<List<LiveLeaderboardEntryResponse>> leaderboard(@PathVariable Long sessionId) {
        return ResponseEntity.ok(liveSessionService.getLeaderboard(sessionId));
    }
}
