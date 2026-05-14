package com.quizweb.backend.live;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LiveAnswerEventRepository extends JpaRepository<LiveAnswerEvent, Long> {

    List<LiveAnswerEvent> findByLiveSessionIdAndParticipantId(Long liveSessionId, Long participantId);

    boolean existsByLiveSessionIdAndParticipantIdAndSlideId(Long liveSessionId, Long participantId, Long slideId);
}
