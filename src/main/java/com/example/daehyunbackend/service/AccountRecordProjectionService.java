package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.AccountDailyBaseline;
import com.example.daehyunbackend.entity.AccountSnapshot;
import com.example.daehyunbackend.entity.AccountSyncState;
import com.example.daehyunbackend.entity.AccountSyncStatus;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.AccountDailyBaselineRepository;
import com.example.daehyunbackend.repository.AccountSnapshotRepository;
import com.example.daehyunbackend.repository.AccountSyncStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AccountRecordProjectionService {
    private final AccountSnapshotRepository accountSnapshotRepository;
    private final AccountDailyBaselineRepository accountDailyBaselineRepository;
    private final AccountSyncStateRepository accountSyncStateRepository;

    @Value("${account.sync.default-refresh-interval-seconds:900}")
    private int defaultRefreshIntervalSeconds;

    public boolean projectLegacyRecord(Record record, boolean liveWrite) {
        if (record == null || record.getRecordid() == null || record.getAccount() == null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime capturedAt = liveWrite ? now : resolveLegacyCapturedAt(record.getDate(), now);

        upsertSnapshot(record, capturedAt);
        insertDailyBaselineIfMissing(record, capturedAt);
        upsertSyncState(record.getAccount(), capturedAt, liveWrite, now);
        return true;
    }

    private void upsertSnapshot(Record record, LocalDateTime fetchedAt) {
        AccountSnapshot snapshot = accountSnapshotRepository.findByLegacyRecordId(record.getRecordid())
                .orElseGet(() -> AccountSnapshot.builder()
                        .account(record.getAccount())
                        .legacyRecordId(record.getRecordid())
                        .build());

        snapshot.setRecordDate(record.getDate());
        snapshot.setFetchedAt(fetchedAt);
        snapshot.setNickname(record.getNICKNAME());
        snapshot.setWinCount(record.getWin_count());
        snapshot.setLoseCount(record.getLose_count());
        snapshot.setRankpoint(record.getRankpoint());
        snapshot.setRankpoint2(record.getRankpoint2());
        snapshot.setFame(record.getFame());
        snapshot.setNicknameColor(record.getNickname_color());
        snapshot.setGuildId(record.getGuild_id());
        snapshot.setGuildPoint(record.getGuild_point());
        snapshot.setGuildName(record.getGuild_name());
        snapshot.setGuildInitial(record.getGuild_initial());
        snapshot.setGuildInitialColor(record.getGuild_initial_color());
        snapshot.setGuildInitialBackgroundColor(record.getGuild_initial_background_color());
        snapshot.setCurrentSkin(record.getCurrent_skin());
        snapshot.setCurrentGem(record.getCurrent_gem());
        snapshot.setIntroduce(record.getIntroduce());

        accountSnapshotRepository.save(snapshot);
    }

    private void insertDailyBaselineIfMissing(Record record, LocalDateTime capturedAt) {
        LocalDate date = record.getDate();
        if (date == null) {
            return;
        }

        accountDailyBaselineRepository.findByAccountAndBaselineDate(record.getAccount(), date)
                .orElseGet(() -> accountDailyBaselineRepository.save(AccountDailyBaseline.builder()
                        .account(record.getAccount())
                        .legacyRecordId(record.getRecordid())
                        .baselineDate(date)
                        .winCount(record.getWin_count())
                        .loseCount(record.getLose_count())
                        .capturedAt(capturedAt)
                        .build()));
    }

    private void upsertSyncState(Account account, LocalDateTime capturedAt, boolean liveWrite, LocalDateTime now) {
        AccountSyncState syncState = accountSyncStateRepository.findByAccount(account)
                .orElseGet(() -> AccountSyncState.initialize(account, now, defaultRefreshIntervalSeconds));

        if (syncState.getLastSyncedAt() == null || capturedAt.isAfter(syncState.getLastSyncedAt())) {
            syncState.setLastSyncedAt(capturedAt);
        }
        if (syncState.getNextSyncAt() == null || liveWrite) {
            syncState.setNextSyncAt(now.plusSeconds(syncState.getRefreshIntervalSeconds()));
        }
        syncState.setSyncEnabled(true);
        syncState.setSyncFailureCount(0);
        syncState.setLastSyncError(null);
        syncState.setStatus(AccountSyncStatus.SUCCESS);
        syncState.setUpdatedAt(now);

        accountSyncStateRepository.save(syncState);
    }

    private LocalDateTime resolveLegacyCapturedAt(LocalDate date, LocalDateTime fallback) {
        if (date == null) {
            return fallback;
        }
        return date.atStartOfDay();
    }
}
