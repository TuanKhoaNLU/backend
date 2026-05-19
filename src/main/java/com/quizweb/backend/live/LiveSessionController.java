package com.quizweb.backend.live;

import com.quizweb.backend.live.dto.CreateLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionRequest;
import com.quizweb.backend.live.dto.JoinLiveSessionResponse;
import com.quizweb.backend.live.dto.LiveLeaderboardEntryResponse;
import com.quizweb.backend.live.dto.LiveRoomBroadcast;
import com.quizweb.backend.live.dto.LiveSessionStateResponse;
import com.quizweb.backend.live.dto.SubmitLiveAnswerRequest;
import com.quizweb.backend.live.dto.SubmitLiveAnswerResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    private final SimpMessagingTemplate messagingTemplate;

    public LiveSessionController(LiveSessionService liveSessionService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.liveSessionService = liveSessionService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping
    public ResponseEntity<LiveSessionStateResponse> createSession(
            @Valid @RequestBody CreateLiveSessionRequest request,
            Authentication authentication
    ) {
        LiveSessionStateResponse state = liveSessionService.createSession(request, authentication.getName());
        broadcastRoomState(state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/join")
    public ResponseEntity<JoinLiveSessionResponse> joinByPin(@Valid @RequestBody JoinLiveSessionRequest request) {
        JoinLiveSessionResponse joinResponse = liveSessionService.joinByPin(request);
        // Broadcast trạng thái phòng mới (bao gồm danh sách người chơi đã cập nhật) tới tất cả client
        broadcastRoomState(joinResponse.session());
        return ResponseEntity.ok(joinResponse);
    }

    @PostMapping("/{sessionId}/start")
    public ResponseEntity<LiveSessionStateResponse> start(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        LiveSessionStateResponse state = liveSessionService.startSession(sessionId, authentication.getName());
        broadcastRoomState(state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{sessionId}/next")
    public ResponseEntity<LiveSessionStateResponse> next(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        LiveSessionStateResponse state = liveSessionService.nextQuestion(sessionId, authentication.getName());
        broadcastRoomState(state);
        return ResponseEntity.ok(state);
    }

    @PostMapping("/{sessionId}/end")
    public ResponseEntity<LiveSessionStateResponse> end(
            @PathVariable Long sessionId,
            Authentication authentication
    ) {
        LiveSessionStateResponse state = liveSessionService.endSession(sessionId, authentication.getName());
        broadcastRoomState(state);
        return ResponseEntity.ok(state);
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
        SubmitLiveAnswerResponse response = liveSessionService.submitAnswer(sessionId, request);
        // Sau khi gửi đáp án, broadcast leaderboard cập nhật cho cả phòng
        LiveSessionStateResponse state = liveSessionService.getSessionState(sessionId);
        broadcastRoomState(state);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{sessionId}/leaderboard")
    public ResponseEntity<List<LiveLeaderboardEntryResponse>> leaderboard(@PathVariable Long sessionId) {
        return ResponseEntity.ok(liveSessionService.getLeaderboard(sessionId));
    }

    // ─── Helper: broadcast trạng thái phòng kèm leaderboard tới /topic/room/{pin} ───
    private void broadcastRoomState(LiveSessionStateResponse state) {
        List<LiveLeaderboardEntryResponse> leaderboard = liveSessionService.getLeaderboard(state.sessionId());
        LiveRoomBroadcast payload = new LiveRoomBroadcast(state, leaderboard);
        messagingTemplate.convertAndSend("/topic/room/" + state.pin(), payload);
    }
}
