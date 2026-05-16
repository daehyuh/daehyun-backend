package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.RankType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record RankPageResponse(
        RankType rankType,
        LocalDate rankingDate,
        LocalDateTime updatedAt,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean cached,
        List<RankEntryResponse> content
) {
    public RankPageResponse withCached(boolean cached) {
        return new RankPageResponse(
                rankType,
                rankingDate,
                updatedAt,
                page,
                size,
                totalElements,
                totalPages,
                cached,
                content
        );
    }
}
