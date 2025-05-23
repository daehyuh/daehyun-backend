package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Auth;
import com.example.daehyunbackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Dictionary;
import java.util.Optional;

@Repository
public interface AuthRepository extends JpaRepository<Auth, Long> {

    boolean existsByUser(User user);

    Optional<Auth> findByRefreshToken(String refreshToken);

    Optional<Auth> findByUser(User user);
}