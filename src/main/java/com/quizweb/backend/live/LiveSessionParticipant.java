package com.quizweb.backend.live;

import com.quizweb.backend.user.UserAccount;
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
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;

@Entity
@Table(
        name = "live_session_participants",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_live_session_player_name", columnNames = {"live_session_id", "displayName"})
        },
        indexes = {
                @Index(name = "idx_live_participant_session", columnList = "live_session_id"),
                @Index(name = "idx_live_participant_user", columnList = "user_id")
        }
)
public class LiveSessionParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "live_session_id", nullable = false)
    private LiveSession liveSession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserAccount user;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LiveParticipantRole role = LiveParticipantRole.PLAYER;

    @Column(nullable = false)
    private int score = 0;

    @Column(nullable = false)
    private int correctCount = 0;

    @Column(nullable = false)
    private boolean connected = true;

    @Column(nullable = false)
    private Instant joinedAt;

    @Column(nullable = false)
    private Instant lastSeenAt;

    @PrePersist
    void prePersist() {
        if (joinedAt == null) {
            joinedAt = Instant.now();
        }
        if (lastSeenAt == null) {
            lastSeenAt = joinedAt;
        }
    }

    @PreUpdate
    void preUpdate() {
        if (lastSeenAt == null) {
            lastSeenAt = Instant.now();
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

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LiveParticipantRole getRole() {
        return role;
    }

    public void setRole(LiveParticipantRole role) {
        this.role = role;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Instant getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Instant joinedAt) {
        this.joinedAt = joinedAt;
    }

    public Instant getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Instant lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }
}
