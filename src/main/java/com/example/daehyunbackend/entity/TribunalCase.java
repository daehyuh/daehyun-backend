package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "tribunal_case",
        indexes = {
                @Index(name = "idx_tribunal_case_created_at", columnList = "created_at"),
                @Index(name = "idx_tribunal_case_replay_room", columnList = "replay_room_id")
        }
)
public class TribunalCase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(name = "replay_url", nullable = false, length = 1000)
    private String replayUrl;

    @Column(name = "replay_room_id", nullable = false, length = 100)
    private String replayRoomId;

    @Column(name = "replay_lang", nullable = false, length = 10)
    private String replayLang;

    @Column(name = "player_nickname", nullable = false, length = 100)
    private String playerNickname;

    @Column(name = "player_pick", nullable = false, length = 100)
    private String playerPick;

    @Column(name = "description", length = 4000)
    private String description;

    @Column(name = "winner_team", length = 100)
    private String winnerTeam;

    @Column(name = "game_type", length = 100)
    private String gameType;

    @Column(name = "game_duration", length = 100)
    private String gameDuration;

    @Column(name = "replay_fetched_at", nullable = false)
    private LocalDateTime replayFetchedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static TribunalCase create(
            User author,
            String replayUrl,
            String replayRoomId,
            String replayLang,
            String playerNickname,
            String playerPick,
            String description,
            String winnerTeam,
            String gameType,
            String gameDuration,
            LocalDateTime now
    ) {
        TribunalCase tribunalCase = new TribunalCase();
        tribunalCase.author = author;
        tribunalCase.replayUrl = replayUrl;
        tribunalCase.replayRoomId = replayRoomId;
        tribunalCase.replayLang = replayLang;
        tribunalCase.playerNickname = playerNickname;
        tribunalCase.playerPick = playerPick;
        tribunalCase.description = description;
        tribunalCase.winnerTeam = winnerTeam;
        tribunalCase.gameType = gameType;
        tribunalCase.gameDuration = gameDuration;
        tribunalCase.replayFetchedAt = now;
        tribunalCase.createdAt = now;
        tribunalCase.updatedAt = now;
        return tribunalCase;
    }
}
