package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalCase;

import java.time.LocalDateTime;

public record TribunalCaseSummaryResponse(
        Long id,
        String replayUrl,
        String replayRoomId,
        String replayLang,
        String playerNickname,
        String playerPick,
        String description,
        String winnerTeam,
        String gameType,
        String gameDuration,
        TribunalAuthorResponse author,
        TribunalVoteSummaryResponse voteSummary,
        long commentCount,
        LocalDateTime replayFetchedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TribunalCaseSummaryResponse from(
            TribunalCase tribunalCase,
            TribunalVoteSummaryResponse voteSummary,
            long commentCount
    ) {
        return new TribunalCaseSummaryResponse(
                tribunalCase.getId(),
                tribunalCase.getReplayUrl(),
                tribunalCase.getReplayRoomId(),
                tribunalCase.getReplayLang(),
                tribunalCase.getPlayerNickname(),
                tribunalCase.getPlayerPick(),
                tribunalCase.getDescription(),
                tribunalCase.getWinnerTeam(),
                tribunalCase.getGameType(),
                tribunalCase.getGameDuration(),
                TribunalAuthorResponse.from(tribunalCase.getAuthor()),
                voteSummary,
                commentCount,
                tribunalCase.getReplayFetchedAt(),
                tribunalCase.getCreatedAt(),
                tribunalCase.getUpdatedAt()
        );
    }
}
