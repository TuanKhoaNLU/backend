package com.quizweb.backend.live;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LiveSessionParticipantRepository extends JpaRepository<LiveSessionParticipant, Long> {

    Optional<LiveSessionParticipant> findByLiveSessionIdAndDisplayNameIgnoreCase(Long liveSessionId, String displayName);

    List<LiveSessionParticipant> findByLiveSessionIdOrderByScoreDescCorrectCountDescJoinedAtAsc(Long liveSessionId);

    List<LiveSessionParticipant> findByLiveSessionIdOrderByJoinedAtAsc(Long liveSessionId);
}
