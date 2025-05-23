package com.example.daehyunbackend.repository;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;


@Repository
public interface RecordRepository extends JpaRepository<Record, Long> {

    List<Record> findAllByDate(LocalDate date);

    Record findByNICKNAMEAndDate(String nickname, LocalDate date);
}