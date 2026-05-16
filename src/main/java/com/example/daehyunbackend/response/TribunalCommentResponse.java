package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalCaseComment;

import java.time.LocalDateTime;

public record TribunalCommentResponse(
        Long id,
        Long parentId,
        TribunalAuthorResponse author,
        String content,
        boolean deleted,
        long likeCount,
        boolean likedByMe,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TribunalCommentResponse from(
            TribunalCaseComment comment,
            TribunalAuthorResponse author,
            long likeCount,
            boolean likedByMe
    ) {
        Long parentId = comment.getParent() == null ? null : comment.getParent().getId();
        return new TribunalCommentResponse(
                comment.getId(),
                parentId,
                author,
                comment.isDeleted() ? null : comment.getContent(),
                comment.isDeleted(),
                likeCount,
                likedByMe,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
