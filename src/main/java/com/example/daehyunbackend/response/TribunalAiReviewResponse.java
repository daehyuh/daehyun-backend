package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalAiReview;
import com.example.daehyunbackend.entity.TribunalAiReviewStatus;
import com.example.daehyunbackend.entity.TribunalVerdict;

import java.time.LocalDateTime;

public record TribunalAiReviewResponse(
        Long id,
        TribunalAiReviewStatus status,
        TribunalVerdict verdict,
        Integer score,
        String grade,
        Integer teamAlignment,
        Double confidence,
        String model,
        String errorMessage,
        Integer retryCount,
        LocalDateTime nextRetryAt,
        LocalDateTime requestedAt,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {
    public static TribunalAiReviewResponse from(TribunalAiReview review) {
        if (review == null) {
            return null;
        }
        return new TribunalAiReviewResponse(
                review.getId(),
                review.getStatus(),
                review.getVerdict(),
                review.getScore(),
                review.getGrade(),
                review.getTeamAlignment(),
                review.getConfidence(),
                review.getModel(),
                review.getStatus() == TribunalAiReviewStatus.FAILED ? "AI_REVIEW_FAILED" : null,
                review.effectiveRetryCount(),
                review.getNextRetryAt(),
                review.getRequestedAt(),
                review.getStartedAt(),
                review.getCompletedAt(),
                review.getUpdatedAt()
        );
    }
}
