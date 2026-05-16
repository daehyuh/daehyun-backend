package com.example.daehyunbackend.service;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.RecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class RecordService {
    private final RecordRepository recordRepository;
    private final AccountRecordProjectionService projectionService;

    public List<Record> findAll() {
        return recordRepository.findAll();
    }

    @Transactional
    public void saveAll(List<Record> records) {
        List<Record> savedRecords = recordRepository.saveAll(records);
        savedRecords.forEach(record -> projectionService.projectLegacyRecord(record, true));
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

    public Optional<Record> findLatestByAccount(Account account) {
        return recordRepository.findTopByAccountOrderByDateDesc(account);
    }

    @Transactional
    public Record save(Record record) {
        Record savedRecord = recordRepository.save(record);
        projectionService.projectLegacyRecord(savedRecord, true);
        return savedRecord;
    }

    public boolean existsByAccount(Account account) {
        return recordRepository.existsByAccount(account);
    }

    public void deleteById(Long id) {
        recordRepository.deleteById(id);
    }

}
