package com.example.daehyunbackend.service;

import com.example.daehyunbackend.dto.UserDto;
import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.User;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
