package com.example.daehyunbackend.scheduler.job;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.response.UserData;
import com.example.daehyunbackend.response.UserDataResponse;
import com.example.daehyunbackend.service.RecordService;
import com.example.daehyunbackend.service.ReportService;
import com.example.daehyunbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class Job {

    final private UserService userService;
    final private RecordService recordService;
    final private ReportService reportService;
    final private AccountRepository accountRepository;

    public void saveAllUserRecord() {
        List<Record> Records = recordService.findAllByDate(LocalDate.now());

        Records.forEach(record -> {
            UserData userData = reportService.getUserData(record.getID()).getUserData();
            record.setNickname_color(userData.getNickname_color());
        });

        recordService.saveAll(Records);
    }

    public void saveAllUserRecordByDate() {

        List<Account> Accounts = accountRepository.findAll();

        Accounts.forEach(account -> {
            UserData userData = reportService.getUserData(account.getAccountId()).getUserData();
            Record record = Record.fromEntity(userData);
            record.setAccount(account);
            recordService.save(record);
        });
    }



}
