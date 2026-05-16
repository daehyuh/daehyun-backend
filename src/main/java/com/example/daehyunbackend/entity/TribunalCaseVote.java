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
import jakarta.persistence.ManyToOne;
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
        name = "tribunal_case_vote",
        indexes = {
                @Index(name = "idx_tribunal_case_vote_case", columnList = "case_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tribunal_case_vote_case_user", columnNames = {"case_id", "user_id"})
        }
)
public class TribunalCaseVote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User voter;

    @Enumerated(EnumType.STRING)
    @Column(name = "verdict", nullable = false, length = 20)
    private TribunalVerdict verdict;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TribunalCaseVote create(TribunalCase tribunalCase, User voter, TribunalVerdict verdict, LocalDateTime now) {
        TribunalCaseVote vote = new TribunalCaseVote();
        vote.tribunalCase = tribunalCase;
        vote.voter = voter;
        vote.verdict = verdict;
        vote.createdAt = now;
        vote.updatedAt = now;
        return vote;
    }
}
