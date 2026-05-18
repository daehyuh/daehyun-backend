package com.example.daehyunbackend.scheduler;

import com.example.daehyunbackend.scheduler.job.Job;
import com.example.daehyunbackend.service.LegacyDataMigrationService;
import com.example.daehyunbackend.service.TribunalAiReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class Scheduler {

    private final Job job;
    private final LegacyDataMigrationService legacyDataMigrationService;
    private final TribunalAiReviewService tribunalAiReviewService;

     @Scheduled(cron = "0 */15 * * * *")
     public void schedule15min() {
            job.saveAllUserRecord();
     }


    @Scheduled(cron = "0 0 0 * * *")
    public void schedule24hour() {
        job.saveAllUserRecordByDate();
    }

    @Scheduled(cron = "0 * * * * *")
    public void scheduleEveryMinute() {
        job.saveAllGuest();
    }

    @Scheduled(
            fixedDelayString = "${migration.legacy-record.fixed-delay-ms:30000}",
            initialDelayString = "${migration.legacy-record.initial-delay-ms:10000}"
    )
    public void migrateLegacyRecords() {
        legacyDataMigrationService.migrateNextBatchQuietly();
    }

    @Scheduled(
            fixedDelayString = "${tribunal.ai.retry.fixed-delay-ms:60000}",
            initialDelayString = "${tribunal.ai.retry.initial-delay-ms:30000}"
    )
    public void retryFailedTribunalAiReviews() {
        tribunalAiReviewService.retryDueFailuresQuietly();
    }
}
