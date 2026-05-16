package com.example.daehyunbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(
        name = "account_sync_state",
        indexes = {
                @Index(name = "idx_account_sync_due", columnList = "sync_enabled, next_sync_at, id"),
                @Index(name = "idx_account_sync_last_viewed", columnList = "last_viewed_at")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_sync_state_account", columnNames = "account_id")
        }
)
public class AccountSyncState {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "sync_enabled", nullable = false)
    private boolean syncEnabled;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;

    @Column(name = "next_sync_at")
    private LocalDateTime nextSyncAt;

    @Column(name = "sync_failure_count", nullable = false)
    private int syncFailureCount;

    @Column(name = "last_sync_error", length = 1000)
    private String lastSyncError;

    @Column(name = "last_viewed_at")
    private LocalDateTime lastViewedAt;

    @Column(name = "refresh_interval_seconds", nullable = false)
    private int refreshIntervalSeconds;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountSyncStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public static AccountSyncState initialize(Account account, LocalDateTime now, int refreshIntervalSeconds) {
        return AccountSyncState.builder()
                .account(account)
                .syncEnabled(true)
                .nextSyncAt(now)
                .syncFailureCount(0)
                .refreshIntervalSeconds(refreshIntervalSeconds)
                .status(AccountSyncStatus.PENDING)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
