package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.AccountDailyBaseline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface AccountDailyBaselineRepository extends JpaRepository<AccountDailyBaseline, Long> {
    long countByAccount(Account account);
    long deleteByAccount(Account account);
    Optional<AccountDailyBaseline> findByAccountAndBaselineDate(Account account, LocalDate baselineDate);
}
