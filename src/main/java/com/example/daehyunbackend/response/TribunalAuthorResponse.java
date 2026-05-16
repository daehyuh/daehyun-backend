package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.User;

public record TribunalAuthorResponse(
        Long id,
        String name,
        String avatarUrl
) {
    public static TribunalAuthorResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new TribunalAuthorResponse(user.getId(), user.getName(), user.getAvatarUrl());
    }
}
