package com.example.daehyunbackend.response;

import com.example.daehyunbackend.entity.AccountSyncStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdminAccountResponse(
        Long id,
        Long accountId,
        String nickname,
        Integer rankPoint,
        LocalDate latestRecordDate,
        LocalDateTime lastSyncedAt,
        AccountSyncStatus syncStatus,
        long recordCount,
        long snapshotCount,
        long dailyBaselineCount,
        boolean hasSyncState
) {
}
