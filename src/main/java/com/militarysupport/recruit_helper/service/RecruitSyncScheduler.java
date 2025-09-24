package com.militarysupport.recruit_helper.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncScheduler {

    private final RecruitSyncService service;

    // 매일 05:15 KST
    @Scheduled(cron = "0 15 5 * * *", zone = "Asia/Seoul")
    public void runDaily() {
        log.info("Starting daily recruitment sync.");

        try {
            int syncedCount = service.syncAll(); // 이제 동기적으로 실행됩니다.
            log.info("Recruit sync completed. saved/updated={}", syncedCount);
        } catch (Exception e) {
            log.error("Failed to run daily recruitment sync.", e);
        }
    }
}