package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findAllByDate(LocalDate date);
    boolean existsByAccount(Account account);
    Record findByNICKNAMEAndDate(String nickname, LocalDate date);
    Optional<Record> findByAccountAndDate(Account account, LocalDate date);
}