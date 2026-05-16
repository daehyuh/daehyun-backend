package com.example.daehyunbackend.dto;

import java.util.List;

public record TribunalCaseCreateRequest(
        String replayUrl,
        String nickname,
        String pick,
        String description,
        List<String> cafeLinks
) {
}
