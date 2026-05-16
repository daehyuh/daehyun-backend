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
        name = "tribunal_comment_like",
        indexes = {
                @Index(name = "idx_tribunal_comment_like_comment", columnList = "comment_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tribunal_comment_like_comment_user", columnNames = {"comment_id", "user_id"})
        }
)
public class TribunalCommentLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private TribunalCaseComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public static TribunalCommentLike create(TribunalCaseComment comment, User user, LocalDateTime now) {
        TribunalCommentLike like = new TribunalCommentLike();
        like.comment = comment;
        like.user = user;
        like.createdAt = now;
        return like;
    }
}
