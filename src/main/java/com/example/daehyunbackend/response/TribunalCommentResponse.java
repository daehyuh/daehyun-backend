package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalCaseComment;
import com.example.daehyunbackend.entity.TribunalCommentType;
import com.example.daehyunbackend.entity.TribunalVerdict;

import java.time.LocalDateTime;

public record TribunalCommentResponse(
        Long id,
        Long parentId,
        TribunalCommentType commentType,
        TribunalAuthorResponse author,
        TribunalVerdict authorVerdict,
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
            TribunalVerdict authorVerdict,
            long likeCount,
            boolean likedByMe
    ) {
        Long parentId = comment.getParent() == null ? null : comment.getParent().getId();
        return new TribunalCommentResponse(
                comment.getId(),
                parentId,
                comment.effectiveCommentType(),
                author,
                authorVerdict,
                comment.isDeleted() ? null : comment.getContent(),
                comment.isDeleted(),
                likeCount,
                likedByMe,
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }
}
