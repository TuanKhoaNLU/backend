package com.quizweb.backend.live.dto;

import java.util.List;

/**
 * Payload được broadcast tới /topic/room/{pin} sau mỗi sự kiện thay đổi trạng thái phòng:
 * host bắt đầu, chuyển câu, kết thúc, hoặc người chơi mới tham gia.
 */
public record LiveRoomBroadcast(
        LiveSessionStateResponse session,
        List<LiveLeaderboardEntryResponse> leaderboard
) {}
