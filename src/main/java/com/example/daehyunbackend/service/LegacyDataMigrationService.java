package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.LegacyDataMigrationState;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.AccountDailyBaselineRepository;
import com.example.daehyunbackend.repository.AccountSnapshotRepository;
import com.example.daehyunbackend.repository.LegacyDataMigrationStateRepository;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.response.LegacyMigrationStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LegacyDataMigrationService {
    private static final String MIGRATION_NAME = "legacy-record-v1";

    private final RecordRepository recordRepository;
    private final AccountSnapshotRepository accountSnapshotRepository;
    private final AccountDailyBaselineRepository accountDailyBaselineRepository;
    private final LegacyDataMigrationStateRepository migrationStateRepository;
    private final AccountRecordProjectionService projectionService;

    @Value("${migration.legacy-record.enabled:true}")
    private boolean enabled;

    @Value("${migration.legacy-record.batch-size:500}")
    private int batchSize;

    @Transactional
    public void migrateNextBatchQuietly() {
        migrateNextBatchInternal(false);
    }

    @Transactional
    public LegacyMigrationStatusResponse migrateNextBatch() {
        return migrateNextBatchInternal(true);
    }

    private LegacyMigrationStatusResponse migrateNextBatchInternal(boolean includeCounts) {
        LegacyDataMigrationState state = getOrCreateStateForUpdate();
        if (!enabled) {
            state.setLastMessage("Legacy record migration is disabled.");
            state.setUpdatedAt(LocalDateTime.now());
            return toResponse(state, includeCounts);
        }

        int normalizedBatchSize = Math.max(1, batchSize);
        List<Record> records = recordRepository.findByRecordidGreaterThanOrderByRecordidAsc(
                state.getLastMigratedRecordId(),
                PageRequest.of(0, normalizedBatchSize)
        );

        int migrated = 0;
        int skipped = 0;
        for (Record record : records) {
            if (projectionService.projectLegacyRecord(record, false)) {
                migrated++;
            } else {
                skipped++;
            }
            state.setLastMigratedRecordId(record.getRecordid());
        }

        LocalDateTime now = LocalDateTime.now();
        boolean caughtUp = records.size() < normalizedBatchSize;
        state.setMigratedRecordCount(state.getMigratedRecordCount() + migrated);
        state.setSkippedRecordCount(state.getSkippedRecordCount() + skipped);
        state.setCaughtUp(caughtUp);
        state.setLastMessage("Migrated " + migrated + " legacy records, skipped " + skipped + ".");
        state.setUpdatedAt(now);

        if (migrated > 0 || skipped > 0) {
            log.info(
                    "Legacy record migration batch completed. migrated={}, skipped={}, lastRecordId={}, caughtUp={}",
                    migrated,
                    skipped,
                    state.getLastMigratedRecordId(),
                    caughtUp
            );
        }

        return toResponse(state, includeCounts);
    }

    @Transactional(readOnly = true)
    public LegacyMigrationStatusResponse getStatus() {
        LegacyDataMigrationState state = migrationStateRepository.findById(MIGRATION_NAME)
                .orElse(null);
        if (state == null) {
            return new LegacyMigrationStatusResponse(
                    enabled,
                    MIGRATION_NAME,
                    0L,
                    0L,
                    0L,
                    false,
                    recordRepository.count(),
                    accountSnapshotRepository.count(),
                    accountDailyBaselineRepository.count(),
                    null,
                    "Legacy record migration has not started."
            );
        }
        return toResponse(state, true);
    }

    private LegacyDataMigrationState getOrCreateStateForUpdate() {
        return migrationStateRepository.findByMigrationNameForUpdate(MIGRATION_NAME)
                .orElseGet(() -> migrationStateRepository.saveAndFlush(
                        LegacyDataMigrationState.initialize(MIGRATION_NAME, LocalDateTime.now())
                ));
    }

    private LegacyMigrationStatusResponse toResponse(LegacyDataMigrationState state, boolean includeCounts) {
        return new LegacyMigrationStatusResponse(
                enabled,
                state.getMigrationName(),
                state.getLastMigratedRecordId(),
                state.getMigratedRecordCount(),
                state.getSkippedRecordCount(),
                state.isCaughtUp(),
                includeCounts ? recordRepository.count() : null,
                includeCounts ? accountSnapshotRepository.count() : null,
                includeCounts ? accountDailyBaselineRepository.count() : null,
                state.getUpdatedAt(),
                state.getLastMessage()
        );
    }
}
