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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class Job {

    final private UserService userService;
    final private RecordService recordService;
    final private ReportService reportService;
    final private AccountRepository accountRepository;

    public void saveAllUserRecord() {
        List<Account> accounts = accountRepository.findAll();
        LocalDate localDate = LocalDate.now();
        System.out.println("localDate = " + localDate);
        accounts.forEach(account -> {
            Optional<Record> record = recordService.findByAccountAndDate(account, localDate);
            UserDataResponse userDataResponse = reportService.getUserData(account.getAccountId());
            UserData userData = userDataResponse.getUserData();
            System.out.println("account = " + account.getAccountId() + ", userData = " + userData);
            if (record.isPresent()) {
                System.out.println(userData.getNICKNAME() + userData.getNickname_color());
                Record record1 = record.get();
                record1.setDate(localDate);
                System.out.println("record1 = " + record1);
            } else {
                Record record1 = Record.fromEntity(userData);
                record1.setAccount(account);
                record1.setDate(localDate);
                recordService.save(record1);
                System.out.println("record2 = " + record1);
            }

        });

    }

    public void saveAllUserRecordByDate() {
        List<Account> accounts = accountRepository.findAll();
        LocalDate localDate = LocalDate.now();

        accounts.forEach(account -> {
            Optional<Record> record = recordService.findByAccountAndDate(account, localDate);
            UserDataResponse userDataResponse = reportService.getUserData(account.getAccountId());
            UserData userData = userDataResponse.getUserData();

            if (record.isPresent()) {
                Record record1 = record.get();
                record1.setDate(localDate);
            } else {
                Record record1 = Record.fromEntity(userData);
                record1.setAccount(account);
                record1.setDate(localDate);
                recordService.save(record1);
            }
        });


    }


}

