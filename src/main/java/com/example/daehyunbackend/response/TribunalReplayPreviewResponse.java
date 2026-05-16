package com.example.daehyunbackend.response;

import java.time.LocalDateTime;
import java.util.List;

public record TribunalReplayPreviewResponse(
        String replayUrl,
        String replayRoomId,
        String replayLang,
        String winnerTeam,
        String gameType,
        String gameDuration,
        LocalDateTime fetchedAt,
        List<TribunalReplayPlayerResponse> players
) {
}
