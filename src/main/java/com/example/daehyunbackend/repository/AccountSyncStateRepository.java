package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.AccountSyncState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountSyncStateRepository extends JpaRepository<AccountSyncState, Long> {
    Optional<AccountSyncState> findByAccount(Account account);
}
