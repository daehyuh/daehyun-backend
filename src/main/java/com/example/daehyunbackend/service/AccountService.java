package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Account findById(Long accountId) {
        return accountRepository.findById(accountId).orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<Account> findAll() {
        return accountRepository.findAll();
    }

    public Long save(Account account) {
        return accountRepository.save(account).getId();
    }

    @Transactional
    public Account linkAccount(Long accountId, User user, String secretKey) {
        Account existingAccount = accountRepository.findByAccountId(accountId);

        if (existingAccount != null) {
            User linkedUser = existingAccount.getUser();
            if (linkedUser != null && !linkedUser.getId().equals(user.getId())) {
                throw new IllegalArgumentException("이미 다른 유저에게 연동된 계정입니다.");
            }

            existingAccount.linkTo(user, secretKey);
            return accountRepository.save(existingAccount);
        }

        Account account = Account.builder()
                .accountId(accountId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .secretKey(secretKey)
                .build();

        return accountRepository.save(account);
    }

    public boolean existsByUser(User user) {
        return accountRepository.existsByUser(user);
    }

    public List<Account> findAllByUser(User user) {
        return accountRepository.findAllByUser(user);
    }

    public Long count() {
        return accountRepository.count();
    }

    public Long countToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();
        return accountRepository.countByCreatedAtBetween(startOfDay, endOfDay);
    }
}
