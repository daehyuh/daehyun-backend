package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "rank_snapshot",
        indexes = {
                @Index(name = "idx_rank_snapshot_page", columnList = "rank_type, ranking_date, rank_no"),
                @Index(name = "idx_rank_snapshot_updated", columnList = "rank_type, ranking_date, created_at")
        }
)
public class RankSnapshot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "rank_type", nullable = false, length = 30)
    private RankType rankType;

    @Column(name = "ranking_date", nullable = false)
    private LocalDate rankingDate;

    @Column(name = "rank_no", nullable = false)
    private int rankNo;

    @Column(length = 100)
    private String nickname;

    @Column(name = "guild_name", length = 100)
    private String guildName;

    @Column(length = 6)
    private String color;

    @Column(name = "initial_color", length = 6)
    private String initialColor;

    @Column(name = "initial_background_color", length = 6)
    private String initialBackgroundColor;

    private Double closeness;

    @Column(name = "initial_closeness")
    private Double initialCloseness;

    @Column(name = "initial_background_closeness")
    private Double initialBackgroundCloseness;

    @Column(name = "is_black")
    private Boolean black;

    @Column(name = "guild_point")
    private Integer guildPoint;

    @Column(name = "guild_initial", length = 30)
    private String guildInitial;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
