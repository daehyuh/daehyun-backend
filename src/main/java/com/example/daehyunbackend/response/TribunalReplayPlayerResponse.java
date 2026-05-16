package com.example.daehyunbackend.response;

public record TribunalReplayPlayerResponse(
        int order,
        String nickname,
        String pick,
        String pickName,
        String jobImageUrl,
        String frameImageUrl
) {
}
