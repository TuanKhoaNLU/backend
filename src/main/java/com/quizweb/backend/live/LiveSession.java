package com.quizweb.backend.live;

import com.quizweb.backend.quiz.Quiz;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(
        name = "live_sessions",
        indexes = {
                @Index(name = "idx_live_session_pin", columnList = "pin", unique = true),
                @Index(name = "idx_live_session_quiz", columnList = "quiz_id"),
                @Index(name = "idx_live_session_status", columnList = "status")
        }
)
public class LiveSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 12, unique = true)
    private String pin;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 50)
    private String hostUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LiveSessionStatus status = LiveSessionStatus.WAITING;

    @Column(nullable = false)
    private int currentQuestionIndex = 0;

    @Column(nullable = false)
    private boolean allowLateJoin = true;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Column
    private Instant startedAt;

    @Column
    private Instant endedAt;

    /**
     * Server time when the current question became active (for synchronized live timers).
     */
    @Column
    private Instant currentSlideStartedAt;

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setQuiz(Quiz quiz) {
        this.quiz = quiz;
    }

    public String getHostUsername() {
        return hostUsername;
    }

    public void setHostUsername(String hostUsername) {
        this.hostUsername = hostUsername;
    }

    public LiveSessionStatus getStatus() {
        return status;
    }

    public void setStatus(LiveSessionStatus status) {
        this.status = status;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void setCurrentQuestionIndex(int currentQuestionIndex) {
        this.currentQuestionIndex = currentQuestionIndex;
    }

    public boolean isAllowLateJoin() {
        return allowLateJoin;
    }

    public void setAllowLateJoin(boolean allowLateJoin) {
        this.allowLateJoin = allowLateJoin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(Instant endedAt) {
        this.endedAt = endedAt;
    }

    public Instant getCurrentSlideStartedAt() {
        return currentSlideStartedAt;
    }

    public void setCurrentSlideStartedAt(Instant currentSlideStartedAt) {
        this.currentSlideStartedAt = currentSlideStartedAt;
    }
}
