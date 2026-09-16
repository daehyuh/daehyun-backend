package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.MobileOAuthState;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface MobileOAuthStateRepository extends JpaRepository<MobileOAuthState, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<MobileOAuthState> findByStateHash(String stateHash);
}
