package com.example.daehyunbackend.scheduler.job;

import com.example.daehyunbackend.entity.Account;
import com.example.daehyunbackend.entity.Record;
import com.example.daehyunbackend.repository.AccountRepository;
import com.example.daehyunbackend.response.UserData;
import com.example.daehyunbackend.response.UserDataResponse;
import com.example.daehyunbackend.service.GuestService;
import com.example.daehyunbackend.service.RecordService;
import com.example.daehyunbackend.service.ReportService;
import com.example.daehyunbackend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class Job {

    final private UserService userService;
    final private RecordService recordService;
    final private ReportService reportService;
    final private AccountRepository accountRepository;
    final private GuestService guestService;

    private final ReentrantLock userRecordLock = new ReentrantLock();

    public void saveAllUserRecord() {
        runWithUserRecordLock("saveAllUserRecord", () -> syncAccountsForDate(LocalDate.now()));
    }

    public void saveAllUserRecordByDate() {
        runWithUserRecordLock("saveAllUserRecordByDate", () -> syncAccountsForDate(LocalDate.now()));
    }

    public void saveAllGuest() {
        guestService.getLastDiscussion();
    }

    private void runWithUserRecordLock(String jobName, Runnable task) {
        userRecordLock.lock();
        try {
            task.run();
        } finally {
            userRecordLock.unlock();
        }
    }

    private void syncAccountsForDate(LocalDate localDate) {
        List<Account> accounts = accountRepository.findAll();
        accounts.forEach(account -> {
            try {
                upsertRecord(account, localDate);
            } catch (Exception e) {
                log.error("Failed to sync account {} on {}", account.getAccountId(), localDate, e);
            }
        });
    }

    private void upsertRecord(Account account, LocalDate localDate) {
        Optional<Record> record = recordService.findByAccountAndDate(account, localDate);
        UserDataResponse userDataResponse = reportService.getUserData(account.getAccountId());
        UserData userData = userDataResponse.getUserData();

        if (record.isPresent()) {
            Record existing = record.get();
            existing.setNickname_color(userData.getNickname_color());
            existing.setGuild_initial_background_color(userData.getGuild_initial_background_color());
            existing.setDate(localDate);
            recordService.save(existing);
        } else {
            Record created = Record.fromEntity(userData);
            created.setAccount(account);
            created.setDate(localDate);
            recordService.save(created);
        }
    }

}
