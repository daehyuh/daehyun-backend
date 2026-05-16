package com.example.daehyunbackend.dto;

public record TribunalCommentRequest(
        String content,
        Long parentId,
        Boolean anonymous
) {
}
