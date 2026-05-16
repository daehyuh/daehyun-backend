package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.RankSnapshot;
import com.fasterxml.jackson.annotation.JsonProperty;

public record RankEntryResponse(
        int rankNo,
        String nickname,
        String guildName,
        String color,
        String initialColor,
        String initialBackgroundColor,
        Double closeness,
        Double initialCloseness,
        Double initialBackgroundCloseness,
        @JsonProperty("isBlack")
        Boolean isBlack,
        Integer guildPoint,
        String guildInitial
) {
    public static RankEntryResponse from(RankSnapshot snapshot) {
        return new RankEntryResponse(
                snapshot.getRankNo(),
                snapshot.getNickname(),
                snapshot.getGuildName(),
                snapshot.getColor(),
                snapshot.getInitialColor(),
                snapshot.getInitialBackgroundColor(),
                snapshot.getCloseness(),
                snapshot.getInitialCloseness(),
                snapshot.getInitialBackgroundCloseness(),
                snapshot.getBlack(),
                snapshot.getGuildPoint(),
                snapshot.getGuildInitial()
        );
    }
}
