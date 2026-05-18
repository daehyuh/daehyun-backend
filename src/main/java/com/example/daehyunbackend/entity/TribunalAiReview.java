package com.example.daehyunbackend.entity;

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
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tribunal_ai_review",
        indexes = {
                @Index(name = "idx_tribunal_ai_review_status", columnList = "status"),
                @Index(name = "idx_tribunal_ai_review_case", columnList = "case_id"),
                @Index(name = "idx_tribunal_ai_review_retry_due", columnList = "status, next_retry_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tribunal_ai_review_case", columnNames = "case_id")
        }
)
public class TribunalAiReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private TribunalAiReviewStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", length = 30)
    private TribunalVerdict verdict;

    @Column(name = "score")
    private Integer score;

    @Column(name = "grade", length = 30)
    private String grade;

    @Column(name = "team_alignment")
    private Integer teamAlignment;

    @Column(name = "confidence")
    private Double confidence;

    @Lob
    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Lob
    @Column(name = "good_points", columnDefinition = "TEXT")
    private String goodPoints;

    @Lob
    @Column(name = "bad_points", columnDefinition = "TEXT")
    private String badPoints;

    @Lob
    @Column(name = "timeline", columnDefinition = "TEXT")
    private String timeline;

    @Lob
    @Column(name = "recommendations", columnDefinition = "TEXT")
    private String recommendations;

    @Column(name = "model", length = 100)
    private String model;

    @Lob
    @Column(name = "raw_response", columnDefinition = "TEXT")
    private String rawResponse;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TribunalAiReview pending(TribunalCase tribunalCase, LocalDateTime now) {
        TribunalAiReview review = new TribunalAiReview();
        review.tribunalCase = tribunalCase;
        review.status = TribunalAiReviewStatus.PENDING;
        review.retryCount = 0;
        review.requestedAt = now;
        review.updatedAt = now;
        return review;
    }

    public void markPendingRetry(LocalDateTime now) {
        this.status = TribunalAiReviewStatus.PENDING;
        this.requestedAt = now;
        this.startedAt = null;
        this.completedAt = null;
        this.errorMessage = null;
        this.nextRetryAt = null;
        this.updatedAt = now;
    }

    public void markRunning(LocalDateTime now) {
        this.status = TribunalAiReviewStatus.RUNNING;
        this.startedAt = now;
        this.completedAt = null;
        this.updatedAt = now;
        this.errorMessage = null;
        this.nextRetryAt = null;
        this.retryCount = effectiveRetryCount() + 1;
    }

    public void markSucceeded(
            TribunalVerdict verdict,
            Integer score,
            String grade,
            Integer teamAlignment,
            Double confidence,
            String summary,
            String goodPoints,
            String badPoints,
            String timeline,
            String recommendations,
            String model,
            String rawResponse,
            LocalDateTime now
    ) {
        this.status = TribunalAiReviewStatus.SUCCEEDED;
        this.verdict = verdict;
        this.score = score;
        this.grade = grade;
        this.teamAlignment = teamAlignment;
        this.confidence = confidence;
        this.summary = summary;
        this.goodPoints = goodPoints;
        this.badPoints = badPoints;
        this.timeline = timeline;
        this.recommendations = recommendations;
        this.model = model;
        this.rawResponse = rawResponse;
        this.errorMessage = null;
        this.nextRetryAt = null;
        this.completedAt = now;
        this.updatedAt = now;
    }

    public void markFailed(String errorMessage, LocalDateTime nextRetryAt, LocalDateTime now) {
        this.status = TribunalAiReviewStatus.FAILED;
        this.errorMessage = errorMessage;
        this.nextRetryAt = nextRetryAt;
        this.completedAt = now;
        this.updatedAt = now;
    }

    public int effectiveRetryCount() {
        return retryCount == null ? 0 : retryCount;
    }
}
