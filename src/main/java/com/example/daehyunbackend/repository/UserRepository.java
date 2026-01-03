package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByProviderId(String providerId);

    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}
