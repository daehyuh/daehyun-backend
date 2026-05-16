package com.example.daehyunbackend.response;

import java.time.LocalDateTime;

public record LegacyMigrationStatusResponse(
        boolean enabled,
        String migrationName,
        Long lastMigratedRecordId,
        Long migratedRecordCount,
        Long skippedRecordCount,
        boolean caughtUp,
        Long legacyRecordCount,
        Long accountSnapshotCount,
        Long dailyBaselineCount,
        LocalDateTime updatedAt,
        String lastMessage
) {
}
