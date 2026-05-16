package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
    List<Record> findByAccountIn(List<Account> accounts);
    Optional<Record> findByAccountAndDate(Account account, LocalDate date);
    Optional<Record> findTopByAccountOrderByDateDesc(Account account);
    @EntityGraph(attributePaths = "account")
    List<Record> findByRecordidGreaterThanOrderByRecordidAsc(Long recordid, Pageable pageable);
}
