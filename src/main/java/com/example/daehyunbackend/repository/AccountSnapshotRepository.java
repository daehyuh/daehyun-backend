package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import com.example.daehyunbackend.entity.Account;

@Repository
public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, Long> {
    long countByAccount(Account account);
    long deleteByAccount(Account account);
    Optional<AccountSnapshot> findByLegacyRecordId(Long legacyRecordId);
    Optional<AccountSnapshot> findTopByAccountOrderByFetchedAtDesc(Account account);
}
