package com.quizweb.backend.live;

import com.quizweb.backend.quiz.QuizSlide;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "live_answer_events",
        indexes = {
                @Index(name = "idx_live_answer_session_participant", columnList = "live_session_id,participant_id"),
                @Index(name = "idx_live_answer_slide", columnList = "slide_id")
        }
)
public class LiveAnswerEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "participant_id", nullable = false)
    private LiveSessionParticipant participant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slide_id", nullable = false)
    private QuizSlide slide;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String submittedAnswerJson;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int scoreEarned;

    @Column(nullable = false)
    private long responseTimeMs;

    @Column(nullable = false)
    private Instant submittedAt;

    @PrePersist
    void prePersist() {
        if (submittedAt == null) {
            submittedAt = Instant.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LiveSession getLiveSession() {
        return liveSession;
    }

    public void setLiveSession(LiveSession liveSession) {
        this.liveSession = liveSession;
    }

    public LiveSessionParticipant getParticipant() {
        return participant;
    }

    public void setParticipant(LiveSessionParticipant participant) {
        this.participant = participant;
    }

    public QuizSlide getSlide() {
        return slide;
    }

    public void setSlide(QuizSlide slide) {
        this.slide = slide;
    }

    public String getSubmittedAnswerJson() {
        return submittedAnswerJson;
    }

    public void setSubmittedAnswerJson(String submittedAnswerJson) {
        this.submittedAnswerJson = submittedAnswerJson;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public int getScoreEarned() {
        return scoreEarned;
    }

    public void setScoreEarned(int scoreEarned) {
        this.scoreEarned = scoreEarned;
    }

    public long getResponseTimeMs() {
        return responseTimeMs;
    }

    public void setResponseTimeMs(long responseTimeMs) {
        this.responseTimeMs = responseTimeMs;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
    }
}
