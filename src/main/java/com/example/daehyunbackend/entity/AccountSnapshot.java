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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "account_snapshot",
        indexes = {
                @Index(name = "idx_account_snapshot_account_fetched", columnList = "account_id, fetched_at"),
                @Index(name = "idx_account_snapshot_fetched", columnList = "fetched_at"),
                @Index(name = "idx_account_snapshot_nickname", columnList = "nickname")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_snapshot_legacy_record", columnNames = "legacy_record_id")
        }
)
public class AccountSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "legacy_record_id")
    private Long legacyRecordId;

    @Column(name = "record_date")
    private LocalDate recordDate;

    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;

    @Column(name = "nickname")
    private String nickname;

    @Column(name = "win_count", nullable = false)
    private int winCount;

    @Column(name = "lose_count", nullable = false)
    private int loseCount;

    @Column(name = "rankpoint", nullable = false)
    private int rankpoint;

    @Column(name = "rankpoint2", nullable = false)
    private int rankpoint2;

    @Column(name = "fame", nullable = false)
    private int fame;

    @Column(name = "nickname_color", nullable = false)
    private int nicknameColor;

    @Column(name = "guild_id", nullable = false)
    private int guildId;

    @Column(name = "guild_point", nullable = false)
    private int guildPoint;

    @Column(name = "guild_name")
    private String guildName;

    @Column(name = "guild_initial")
    private String guildInitial;

    @Column(name = "guild_initial_color", nullable = false)
    private int guildInitialColor;

    @Column(name = "guild_initial_background_color", nullable = false)
    private int guildInitialBackgroundColor;

    @Column(name = "current_skin")
    private String currentSkin;

    @Column(name = "current_gem")
    private String currentGem;

    @Column(name = "introduce")
    private String introduce;
}
