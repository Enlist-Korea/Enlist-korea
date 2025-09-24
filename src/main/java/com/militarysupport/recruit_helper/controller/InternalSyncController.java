package com.militarysupport.recruit_helper.controller;

import com.militarysupport.recruit_helper.service.RecruitSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalSyncController {

    private final RecruitSyncService service;

    @PostMapping("/sync")
    public Map<String, Object> syncRecruitments() {
        log.info("Manual recruitment sync triggered.");

        try {
            int syncedCount = service.syncAll(); // service 메서드를 직접 호출합니다.
            return Map.of("syncedCount", syncedCount, "status", "success");
        } catch (Exception e) {
            log.error("Manual recruitment sync failed.", e);
            return Map.of("status", "failed", "error", e.getMessage());
        }
    }
}