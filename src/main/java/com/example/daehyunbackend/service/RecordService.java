package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;

    public List<Record> findAll() {
        return recordRepository.findAll();
    }

    public void saveAll(List<Record> records) {
        recordRepository.saveAll(records);
    }
    public List<Record> findAllByDate(LocalDate date) {
        return recordRepository.findAllByDate(date);
    }

    public Optional<Record> findById(Long id) {
        return recordRepository.findById(id);
    }

    public Optional<Record> findByAccountAndDate(Account account, LocalDate date) {
        return recordRepository.findByAccountAndDate(account, date);
    }

    public Record save(Record record) {
        return recordRepository.save(record);
    }

    public boolean existsByAccount(Account account) {
        return recordRepository.existsByAccount(account);
    }

    public void deleteById(Long id) {
        recordRepository.deleteById(id);
    }

}
