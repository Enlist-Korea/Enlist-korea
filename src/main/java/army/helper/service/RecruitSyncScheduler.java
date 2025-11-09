package army.helper.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncScheduler {

    private final RecruitSyncService service;
    @PostConstruct
    public void initFetch() {
        service.syncAll();
    }

    @Scheduled(cron = " 0 15 5 * * *", zone = "Asia/Seoul")
    public void runDaily(){
        log.info("Starting daily recruitment Sync");

        try{
            int syncedCount = service.syncAll();
            log.info("Recruit sync completed. saved/updated= {}", syncedCount);
        } catch(Exception e){
            log.error("Failed to run daily recruitment sync", e);
        }
    }
}
