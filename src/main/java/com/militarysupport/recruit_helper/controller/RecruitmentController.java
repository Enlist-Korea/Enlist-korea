package com.militarysupport.recruit_helper.controller;

import com.militarysupport.recruit_helper.dto.RecruitmentStatusResponse;
import com.militarysupport.recruit_helper.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    // 모집중인 병과 조회 엔드포인트
    @GetMapping("/status")
    public ResponseEntity<List<RecruitmentStatusResponse>> getAvailableRecruitments(
            // 클라이언트가 보낼 파라미터들을 받도록 설정 (선택 사항)
            @RequestParam(value = "gunGbnm", required = false) String gunGbnm,
            @RequestParam(value = "mojipGbnm", required = false) String mojipGbnm,
            @RequestParam(value = "statusType", required = false) String statusType
    ) {

        List<RecruitmentStatusResponse> result = recruitmentService.findStatusByFilters(
                gunGbnm,
                mojipGbnm,
                statusType
        );

        // 결과가 비어있지 않은지 확인하고 200 OK 응답
        return ResponseEntity.ok(result);
    }
}
