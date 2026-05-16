package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.AccountSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountSnapshotRepository extends JpaRepository<AccountSnapshot, Long> {
    Optional<AccountSnapshot> findByLegacyRecordId(Long legacyRecordId);
}
