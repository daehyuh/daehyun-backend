package com.example.daehyunbackend.scheduler;

import com.example.daehyunbackend.scheduler.job.Job;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final Job job;

     @Scheduled(cron = "0 */10 * * * *")
     public synchronized  void schedule10min() {
            job.saveAllUserRecord();
     }


    @Scheduled(cron = "0 0 0 * * *")
    public void schedule24hour() {
        job.saveAllUserRecordByDate();
    }

    @Scheduled(cron = "* * * * * *")
    public void scheduleEveryMinute() {
        job.saveAllGuest();
    }
}