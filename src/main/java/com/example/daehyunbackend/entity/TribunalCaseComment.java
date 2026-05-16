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
        name = "tribunal_case_comment",
        indexes = {
                @Index(name = "idx_tribunal_case_comment_case_created", columnList = "case_id, created_at"),
                @Index(name = "idx_tribunal_case_comment_parent", columnList = "parent_id")
        }
)
public class TribunalCaseComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private TribunalCase tribunalCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private TribunalCaseComment parent;

    @Column(name = "content", nullable = false, length = 4000)
    private String content;

    @Column(name = "anonymous")
    private Boolean anonymous;

    @Column(name = "deleted", nullable = false)
    private boolean deleted;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TribunalCaseComment create(
            TribunalCase tribunalCase,
            User author,
            TribunalCaseComment parent,
            String content,
            boolean anonymous,
            LocalDateTime now
    ) {
        TribunalCaseComment comment = new TribunalCaseComment();
        comment.tribunalCase = tribunalCase;
        comment.author = author;
        comment.parent = parent;
        comment.content = content;
        comment.anonymous = anonymous;
        comment.createdAt = now;
        comment.updatedAt = now;
        return comment;
    }
}
