package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.RankSnapshot;
import com.example.daehyunbackend.entity.RankType;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.RankSnapshotRepository;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.response.RankEntryResponse;
import com.example.daehyunbackend.response.RankPageResponse;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RankService {
    private final RecordRepository recordRepository;
    private final RankSnapshotRepository rankSnapshotRepository;
    private final MeterRegistry meterRegistry;

    private final Object refreshLock = new Object();
    private final Map<RankPageCacheKey, CachedRankPage> pageCache = new ConcurrentHashMap<>();
    private final Map<RankSearchCacheKey, CachedRankPage> searchCache = new ConcurrentHashMap<>();
    private final Map<LegacyRankCacheKey, CachedLegacyRank> legacyCache = new ConcurrentHashMap<>();

    @Value("${rank.page.default-size:50}")
    private int defaultPageSize;

    @Value("${rank.page.max-size:200}")
    private int maxPageSize;

    @Value("${rank.cache.ttl-seconds:60}")
    private long cacheTtlSeconds;

    @Value("${rank.snapshot.max-age-seconds:900}")
    private long snapshotMaxAgeSeconds;

    public RankPageResponse getRankPage(RankType rankType, Integer page, Integer size) {
        LocalDate rankingDate = LocalDate.now();
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = normalizeSize(size);

        ensureSnapshot(rankType, rankingDate);

        RankPageCacheKey cacheKey = new RankPageCacheKey(rankType, rankingDate, normalizedPage, normalizedSize);
        CachedRankPage cached = pageCache.get(cacheKey);
        if (cached != null && cached.isAlive()) {
            meterRegistry.counter("rank_cache_requests_total", "cache", "hit", "mode", "page").increment();
            return cached.response().withCached(true);
        }

        meterRegistry.counter("rank_cache_requests_total", "cache", "miss", "mode", "page").increment();
        Page<RankSnapshot> snapshotPage = rankSnapshotRepository.findByRankTypeAndRankingDateOrderByRankNoAsc(
                rankType,
                rankingDate,
                PageRequest.of(normalizedPage, normalizedSize)
        );
        LocalDateTime updatedAt = findUpdatedAt(rankType, rankingDate);
        List<RankEntryResponse> content = snapshotPage.getContent().stream()
                .map(RankEntryResponse::from)
                .toList();

        RankPageResponse response = new RankPageResponse(
                rankType,
                rankingDate,
                updatedAt,
                normalizedPage,
                normalizedSize,
                snapshotPage.getTotalElements(),
                snapshotPage.getTotalPages(),
                false,
                content
        );
        pageCache.put(cacheKey, new CachedRankPage(response, expiresAt()));
        return response;
    }

    public RankPageResponse searchRankPage(RankType rankType, String keyword, Integer page, Integer size) {
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword == null) {
            return getRankPage(rankType, page, size);
        }

        LocalDate rankingDate = LocalDate.now();
        int normalizedPage = Math.max(page == null ? 0 : page, 0);
        int normalizedSize = normalizeSize(size);

        ensureSnapshot(rankType, rankingDate);

        RankSearchCacheKey cacheKey = new RankSearchCacheKey(
                rankType,
                rankingDate,
                normalizedKeyword.toLowerCase(Locale.ROOT),
                normalizedPage,
                normalizedSize
        );
        CachedRankPage cached = searchCache.get(cacheKey);
        if (cached != null && cached.isAlive()) {
            meterRegistry.counter("rank_cache_requests_total", "cache", "hit", "mode", "search").increment();
            return cached.response().withCached(true);
        }

        meterRegistry.counter("rank_cache_requests_total", "cache", "miss", "mode", "search").increment();
        Page<RankSnapshot> snapshotPage = searchSnapshots(rankType, rankingDate, normalizedKeyword, normalizedPage, normalizedSize);
        LocalDateTime updatedAt = findUpdatedAt(rankType, rankingDate);
        List<RankEntryResponse> content = snapshotPage.getContent().stream()
                .map(RankEntryResponse::from)
                .toList();

        RankPageResponse response = new RankPageResponse(
                rankType,
                rankingDate,
                updatedAt,
                normalizedPage,
                normalizedSize,
                snapshotPage.getTotalElements(),
                snapshotPage.getTotalPages(),
                false,
                content
        );
        searchCache.put(cacheKey, new CachedRankPage(response, expiresAt()));
        return response;
    }

    public List<RankEntryResponse> getLegacyRank(RankType rankType) {
        LocalDate rankingDate = LocalDate.now();
        ensureSnapshot(rankType, rankingDate);

        LegacyRankCacheKey cacheKey = new LegacyRankCacheKey(rankType, rankingDate);
        CachedLegacyRank cached = legacyCache.get(cacheKey);
        if (cached != null && cached.isAlive()) {
            meterRegistry.counter("rank_cache_requests_total", "cache", "hit", "mode", "legacy").increment();
            return cached.response();
        }

        meterRegistry.counter("rank_cache_requests_total", "cache", "miss", "mode", "legacy").increment();
        List<RankEntryResponse> response = rankSnapshotRepository
                .findByRankTypeAndRankingDateOrderByRankNoAsc(rankType, rankingDate)
                .stream()
                .map(RankEntryResponse::from)
                .toList();
        legacyCache.put(cacheKey, new CachedLegacyRank(response, expiresAt()));
        return response;
    }

    public void refreshRanks(LocalDate rankingDate) {
        synchronized (refreshLock) {
            List<Record> records = recordRepository.findAllByDate(rankingDate);
            LocalDateTime createdAt = LocalDateTime.now();

            List<RankSnapshot> blackRanks = buildBlackRanks(records, rankingDate, createdAt);
            List<RankSnapshot> guildRanks = buildGuildBlackRanks(records, rankingDate, createdAt);

            rankSnapshotRepository.deleteByRankTypeAndRankingDate(RankType.BLACK, rankingDate);
            rankSnapshotRepository.deleteByRankTypeAndRankingDate(RankType.GUILD_BLACK, rankingDate);
            rankSnapshotRepository.saveAll(blackRanks);
            rankSnapshotRepository.saveAll(guildRanks);

            evictCaches(rankingDate);
            meterRegistry.counter("rank_snapshot_refresh_total").increment();
            log.info(
                    "Rank snapshots refreshed. date={}, black={}, guildBlack={}",
                    rankingDate,
                    blackRanks.size(),
                    guildRanks.size()
            );
        }
    }

    private void ensureSnapshot(RankType rankType, LocalDate rankingDate) {
        LocalDateTime updatedAt = findUpdatedAt(rankType, rankingDate);
        if (updatedAt == null || updatedAt.isBefore(LocalDateTime.now().minusSeconds(snapshotMaxAgeSeconds))) {
            refreshRanks(rankingDate);
        }
    }

    private LocalDateTime findUpdatedAt(RankType rankType, LocalDate rankingDate) {
        return rankSnapshotRepository
                .findTopByRankTypeAndRankingDateOrderByCreatedAtDesc(rankType, rankingDate)
                .map(RankSnapshot::getCreatedAt)
                .orElse(null);
    }

    private List<RankSnapshot> buildBlackRanks(List<Record> records, LocalDate rankingDate, LocalDateTime createdAt) {
        List<RankSnapshot> snapshots = new ArrayList<>();

        for (Record record : records) {
            String hex = intToHexColor(record.getNickname_color());
            int[] rgb = hexToRgb(hex);
            double closeness = calculateBlackCloseness(rgb[0], rgb[1], rgb[2]);

            snapshots.add(RankSnapshot.builder()
                    .rankType(RankType.BLACK)
                    .rankingDate(rankingDate)
                    .nickname(record.getNICKNAME())
                    .color(hex)
                    .closeness(round4(closeness))
                    .black(closeness > 90)
                    .createdAt(createdAt)
                    .build());
        }

        snapshots.sort(Comparator.comparing(RankSnapshot::getCloseness, Comparator.reverseOrder()));
        return applyRankNo(snapshots);
    }

    private List<RankSnapshot> buildGuildBlackRanks(List<Record> records, LocalDate rankingDate, LocalDateTime createdAt) {
        Map<String, Record> guildRecords = new LinkedHashMap<>();
        for (Record record : records) {
            String guildName = record.getGuild_name();
            if (guildName == null || guildName.isBlank()) {
                continue;
            }
            guildRecords.merge(
                    guildName,
                    record,
                    (left, right) -> right.getGuild_point() > left.getGuild_point() ? right : left
            );
        }

        List<RankSnapshot> snapshots = new ArrayList<>();
        for (Record record : guildRecords.values()) {
            String initialColor = intToHexColor(record.getGuild_initial_color());
            int[] initialRgb = hexToRgb(initialColor);
            double initialCloseness = calculateBlackCloseness(initialRgb[0], initialRgb[1], initialRgb[2]);

            String backgroundColor = intToHexColor(record.getGuild_initial_background_color());
            int[] backgroundRgb = hexToRgb(backgroundColor);
            double backgroundCloseness = calculateBlackCloseness(backgroundRgb[0], backgroundRgb[1], backgroundRgb[2]);

            snapshots.add(RankSnapshot.builder()
                    .rankType(RankType.GUILD_BLACK)
                    .rankingDate(rankingDate)
                    .guildName(record.getGuild_name())
                    .guildPoint(record.getGuild_point())
                    .guildInitial(record.getGuild_initial())
                    .initialColor(initialColor)
                    .initialBackgroundColor(backgroundColor)
                    .initialCloseness(round4(initialCloseness))
                    .initialBackgroundCloseness(round4(backgroundCloseness))
                    .createdAt(createdAt)
                    .build());
        }

        snapshots.sort(Comparator.comparing(
                RankSnapshot::getInitialBackgroundCloseness,
                Comparator.reverseOrder()
        ));
        return applyRankNo(snapshots);
    }

    private List<RankSnapshot> applyRankNo(List<RankSnapshot> snapshots) {
        List<RankSnapshot> ranked = new ArrayList<>(snapshots.size());
        for (int index = 0; index < snapshots.size(); index++) {
            RankSnapshot snapshot = snapshots.get(index);
            ranked.add(RankSnapshot.builder()
                    .rankType(snapshot.getRankType())
                    .rankingDate(snapshot.getRankingDate())
                    .rankNo(index + 1)
                    .nickname(snapshot.getNickname())
                    .guildName(snapshot.getGuildName())
                    .color(snapshot.getColor())
                    .initialColor(snapshot.getInitialColor())
                    .initialBackgroundColor(snapshot.getInitialBackgroundColor())
                    .closeness(snapshot.getCloseness())
                    .initialCloseness(snapshot.getInitialCloseness())
                    .initialBackgroundCloseness(snapshot.getInitialBackgroundCloseness())
                    .black(snapshot.getBlack())
                    .guildPoint(snapshot.getGuildPoint())
                    .guildInitial(snapshot.getGuildInitial())
                    .createdAt(snapshot.getCreatedAt())
                    .build());
        }
        return ranked;
    }

    private int normalizeSize(Integer size) {
        int requested = size == null ? defaultPageSize : size;
        return Math.max(1, Math.min(requested, maxPageSize));
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private Page<RankSnapshot> searchSnapshots(
            RankType rankType,
            LocalDate rankingDate,
            String keyword,
            int page,
            int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        if (rankType == RankType.GUILD_BLACK) {
            return rankSnapshotRepository.findByRankTypeAndRankingDateAndGuildNameContainingIgnoreCaseOrderByRankNoAsc(
                    rankType,
                    rankingDate,
                    keyword,
                    pageRequest
            );
        }

        return rankSnapshotRepository.findByRankTypeAndRankingDateAndNicknameContainingIgnoreCaseOrderByRankNoAsc(
                rankType,
                rankingDate,
                keyword,
                pageRequest
        );
    }

    private Instant expiresAt() {
        return Instant.now().plusSeconds(cacheTtlSeconds);
    }

    private void evictCaches(LocalDate rankingDate) {
        pageCache.keySet().removeIf(key -> key.rankingDate().equals(rankingDate));
        searchCache.keySet().removeIf(key -> key.rankingDate().equals(rankingDate));
        legacyCache.keySet().removeIf(key -> key.rankingDate().equals(rankingDate));
    }

    public static int[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.valueOf(hex.substring(0, 2), 16);
        int g = Integer.valueOf(hex.substring(2, 4), 16);
        int b = Integer.valueOf(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    public static double calculateBlackCloseness(int r, int g, int b) {
        double distance = Math.sqrt(r * r + g * g + b * b);
        double closeness = 100.0 * (1.0 - (distance / 441.673));
        return Math.round(closeness * 100.0) / 100.0;
    }

    public static String intToHexColor(int color) {
        long unsigned = color & 0xFFFFFFFFL;
        String hex = Long.toHexString(unsigned).toUpperCase();
        if (hex.length() > 6) {
            hex = hex.substring(hex.length() - 6);
        } else {
            hex = String.format("%06X", unsigned);
        }
        return hex;
    }

    private static double round4(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private record RankPageCacheKey(RankType rankType, LocalDate rankingDate, int page, int size) {
    }

    private record RankSearchCacheKey(RankType rankType, LocalDate rankingDate, String keyword, int page, int size) {
    }

    private record LegacyRankCacheKey(RankType rankType, LocalDate rankingDate) {
    }

    private record CachedRankPage(RankPageResponse response, Instant expiresAt) {
        boolean isAlive() {
            return Instant.now().isBefore(expiresAt);
        }
    }

    private record CachedLegacyRank(List<RankEntryResponse> response, Instant expiresAt) {
        boolean isAlive() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
