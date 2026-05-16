package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "legacy_data_migration_state")
public class LegacyDataMigrationState {
    @Id
    @Column(name = "migration_name", length = 100)
    private String migrationName;

    @Column(name = "last_migrated_record_id", nullable = false)
    private Long lastMigratedRecordId;

    @Column(name = "migrated_record_count", nullable = false)
    private Long migratedRecordCount;

    @Column(name = "skipped_record_count", nullable = false)
    private Long skippedRecordCount;

    @Column(name = "caught_up", nullable = false)
    private boolean caughtUp;

    @Column(name = "last_message", length = 1000)
    private String lastMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static LegacyDataMigrationState initialize(String migrationName, LocalDateTime now) {
        return LegacyDataMigrationState.builder()
                .migrationName(migrationName)
                .lastMigratedRecordId(0L)
                .migratedRecordCount(0L)
                .skippedRecordCount(0L)
                .caughtUp(false)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
