package com.example.daehyunbackend.event;

public record TribunalAiReviewRequestedEvent(Long caseId, boolean force) {
    public TribunalAiReviewRequestedEvent(Long caseId) {
        this(caseId, false);
    }
}
