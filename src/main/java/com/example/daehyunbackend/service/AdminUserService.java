package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.AccountDailyBaseline;
import com.example.daehyunbackend.entity.AccountSnapshot;
import com.example.daehyunbackend.entity.AccountSyncState;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AccountDailyBaselineRepository;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.AccountSnapshotRepository;
import com.example.daehyunbackend.repository.AccountSyncStateRepository;
import com.example.daehyunbackend.repository.GuestRepository;
import com.example.daehyunbackend.repository.RecordRepository;
import com.example.daehyunbackend.repository.UserRepository;
import com.example.daehyunbackend.response.AdminAccountActionResponse;
import com.example.daehyunbackend.response.AdminAccountResponse;
import com.example.daehyunbackend.response.AdminUserPageResponse;
import com.example.daehyunbackend.response.AdminUserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final RecordRepository recordRepository;
    private final AccountSnapshotRepository accountSnapshotRepository;
    private final AccountDailyBaselineRepository accountDailyBaselineRepository;
    private final AccountSyncStateRepository accountSyncStateRepository;
    private final GuestRepository guestRepository;

    @Transactional(readOnly = true)
    public AdminUserPageResponse findUsers(Integer requestedPage, Integer requestedSize, String requestedQuery) {
        int page = Math.max(requestedPage == null ? 0 : requestedPage, 0);
        int size = Math.min(Math.max(requestedSize == null ? 20 : requestedSize, 1), 100);
        String query = requestedQuery == null ? "" : requestedQuery.trim();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (query.isBlank()) {
            users = userRepository.findAll(pageable);
        } else {
            users = userRepository.searchForAdmin(query, pageable);
        }

        List<AdminUserResponse> content = users.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new AdminUserPageResponse(
                content,
                users.getNumber(),
                users.getSize(),
                users.getTotalElements(),
                users.getTotalPages()
        );
    }

    @Transactional
    public AdminAccountActionResponse unlinkAccount(Long userId, Long accountRecordId) {
        User user = findUser(userId);
        Account account = findOwnedAccount(user, accountRecordId);
        account.unlink();
        accountRepository.save(account);

        return new AdminAccountActionResponse(userId, accountRecordId, false, 0, 0, 0, 0);
    }

    @Transactional
    public AdminAccountActionResponse deleteAccountData(Long userId, Long accountRecordId) {
        User user = findUser(userId);
        Account account = findOwnedAccount(user, accountRecordId);

        long records = recordRepository.countByAccount(account);
        long snapshots = accountSnapshotRepository.countByAccount(account);
        long dailyBaselines = accountDailyBaselineRepository.countByAccount(account);
        long guestMappings = guestRepository.deleteByAccountIdAndUser(account.getAccountId(), user);

        accountSyncStateRepository.deleteByAccount(account);
        accountSnapshotRepository.deleteByAccount(account);
        accountDailyBaselineRepository.deleteByAccount(account);
        recordRepository.deleteByAccount(account);
        accountRepository.delete(account);
        accountRepository.flush();

        return new AdminAccountActionResponse(
                userId,
                accountRecordId,
                true,
                records,
                snapshots,
                dailyBaselines,
                guestMappings
        );
    }

    private AdminUserResponse toResponse(User user) {
        List<AdminAccountResponse> accounts = accountRepository.findAllByUser(user).stream()
                .map(this::toAccountResponse)
                .toList();

        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getProvider(),
                user.getProviderId(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                accounts
        );
    }

    private AdminAccountResponse toAccountResponse(Account account) {
        Record latestRecord = recordRepository.findTopByAccountOrderByDateDesc(account).orElse(null);
        AccountSnapshot latestSnapshot = accountSnapshotRepository.findTopByAccountOrderByFetchedAtDesc(account).orElse(null);
        AccountSyncState syncState = accountSyncStateRepository.findByAccount(account).orElse(null);

        return new AdminAccountResponse(
                account.getId(),
                account.getAccountId(),
                latestRecord != null && latestRecord.getNICKNAME() != null
                        ? latestRecord.getNICKNAME()
                        : latestSnapshot == null ? null : latestSnapshot.getNickname(),
                latestRecord != null
                        ? latestRecord.getRankpoint()
                        : latestSnapshot == null ? null : latestSnapshot.getRankpoint(),
                latestRecord != null && latestRecord.getDate() != null
                        ? latestRecord.getDate()
                        : latestSnapshot == null ? null : latestSnapshot.getRecordDate(),
                syncState != null && syncState.getLastSyncedAt() != null
                        ? syncState.getLastSyncedAt()
                        : latestSnapshot == null ? null : latestSnapshot.getFetchedAt(),
                syncState == null ? null : syncState.getStatus(),
                recordRepository.countByAccount(account),
                accountSnapshotRepository.countByAccount(account),
                accountDailyBaselineRepository.countByAccount(account),
                syncState != null
        );
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }

    private Account findOwnedAccount(User user, Long accountRecordId) {
        Account account = accountRepository.findById(accountRecordId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountRecordId));
        if (account.getUser() == null || !user.getId().equals(account.getUser().getId())) {
            throw new IllegalArgumentException("The account is not linked to the selected user");
        }
        return account;
    }
}
