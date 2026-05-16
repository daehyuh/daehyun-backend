package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tribunal_case_anonymous_author",
        indexes = {
                @Index(name = "idx_tribunal_case_anonymous_case", columnList = "case_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tribunal_case_anonymous_case_user", columnNames = {"case_id", "user_id"}),
                @UniqueConstraint(name = "uk_tribunal_case_anonymous_case_no", columnNames = {"case_id", "anonymous_no"})
        }
)
public class TribunalCaseAnonymousAuthor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "anonymous_no", nullable = false)
    private int anonymousNo;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static TribunalCaseAnonymousAuthor create(
            TribunalCase tribunalCase,
            User user,
            int anonymousNo,
            LocalDateTime now
    ) {
        TribunalCaseAnonymousAuthor author = new TribunalCaseAnonymousAuthor();
        author.tribunalCase = tribunalCase;
        author.user = user;
        author.anonymousNo = anonymousNo;
        author.createdAt = now;
        return author;
    }
}
