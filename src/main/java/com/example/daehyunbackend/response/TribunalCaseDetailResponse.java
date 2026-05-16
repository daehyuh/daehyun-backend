package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.TribunalCase;
import com.example.daehyunbackend.support.MafiaJobNameResolver;

import java.time.LocalDateTime;
import java.util.List;

public record TribunalCaseDetailResponse(
        Long id,
        String replayUrl,
        String replayRoomId,
        String replayLang,
        String playerNickname,
        String playerPick,
        String playerPickName,
        String description,
        String winnerTeam,
        String gameType,
        String gameDuration,
        TribunalAuthorResponse author,
        List<TribunalCafeLinkResponse> cafeLinks,
        List<TribunalReplayMessageResponse> replayMessages,
        TribunalVoteSummaryResponse voteSummary,
        long viewCount,
        List<TribunalCommentResponse> comments,
        LocalDateTime replayFetchedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TribunalCaseDetailResponse from(
            TribunalCase tribunalCase,
            TribunalAuthorResponse author,
            List<TribunalCafeLinkResponse> cafeLinks,
            List<TribunalReplayMessageResponse> replayMessages,
            TribunalVoteSummaryResponse voteSummary,
            long viewCount,
            List<TribunalCommentResponse> comments
    ) {
        return new TribunalCaseDetailResponse(
                tribunalCase.getId(),
                tribunalCase.getReplayUrl(),
                tribunalCase.getReplayRoomId(),
                tribunalCase.getReplayLang(),
                tribunalCase.getPlayerNickname(),
                tribunalCase.getPlayerPick(),
                MafiaJobNameResolver.resolve(tribunalCase.getPlayerPick()),
                tribunalCase.getDescription(),
                tribunalCase.getWinnerTeam(),
                tribunalCase.getGameType(),
                tribunalCase.getGameDuration(),
                author,
                cafeLinks,
                replayMessages,
                voteSummary,
                viewCount,
                comments,
                tribunalCase.getReplayFetchedAt(),
                tribunalCase.getCreatedAt(),
                tribunalCase.getUpdatedAt()
        );
    }
}
