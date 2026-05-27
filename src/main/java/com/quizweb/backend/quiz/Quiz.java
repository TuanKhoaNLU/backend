package com.quizweb.backend.quiz;

import jakarta.persistence.Column;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizMode mode = QuizMode.NORMAL;

    @Column(nullable = false)
    private boolean published = true;

    @Column
    private Integer totalTimeLimitSeconds;

    @Column(length = 50)
    private String createdBy;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizLifecycleStatus lifecycleStatus = QuizLifecycleStatus.PUBLISHED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuizAccessScope accessScope = QuizAccessScope.PUBLIC;

    @Column(length = 80)
    private String slug;

    @Column(length = 1000)
    private String description;

    @Column
    private Instant publishedAt;

    @Column(nullable = false)
    private boolean practiceEnabled = true;

    @Column
    private Integer maxAttemptsPerUser;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuizSlide> slides = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
        if (published && publishedAt == null) {
            publishedAt = Instant.now();
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = Instant.now();
        if (published && publishedAt == null) {
            publishedAt = updatedAt;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public QuizMode getMode() {
        return mode;
    }

    public void setMode(QuizMode mode) {
        this.mode = mode;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    public Integer getTotalTimeLimitSeconds() {
        return totalTimeLimitSeconds;
    }

    public void setTotalTimeLimitSeconds(Integer totalTimeLimitSeconds) {
        this.totalTimeLimitSeconds = totalTimeLimitSeconds;
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

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public QuizLifecycleStatus getLifecycleStatus() {
        return lifecycleStatus;
    }

    public void setLifecycleStatus(QuizLifecycleStatus lifecycleStatus) {
        this.lifecycleStatus = lifecycleStatus;
    }

    public QuizAccessScope getAccessScope() {
        return accessScope;
    }

    public void setAccessScope(QuizAccessScope accessScope) {
        this.accessScope = accessScope;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public boolean isPracticeEnabled() {
        return practiceEnabled;
    }

    public void setPracticeEnabled(boolean practiceEnabled) {
        this.practiceEnabled = practiceEnabled;
    }

    public Integer getMaxAttemptsPerUser() {
        return maxAttemptsPerUser;
    }

    public void setMaxAttemptsPerUser(Integer maxAttemptsPerUser) {
        this.maxAttemptsPerUser = maxAttemptsPerUser;
    }

    public List<QuizSlide> getSlides() {
        return slides;
    }

    public void setSlides(List<QuizSlide> slides) {
        this.slides = slides;
    }
}
