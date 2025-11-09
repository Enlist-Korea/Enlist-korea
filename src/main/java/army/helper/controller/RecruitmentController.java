package army.helper.controller;

import army.helper.dto.RecruitmentStatusListResponse;
import army.helper.dto.RecruitmentStatusResponse;
import army.helper.service.RecruitmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recruitments")
@RequiredArgsConstructor
@Slf4j
public class RecruitmentController {

    private final RecruitmentService recruitmentService;

    /**
     * 🪖 모집 현황 전체 조회 + 필터링 지원
     * - JSON 배열 형식으로 반환
     * - 파라미터가 없으면 전체 조회
     */
    // 1. 'produces'를 JSON으로 변경
    @GetMapping(value = "/status", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RecruitmentStatusResponse>> getRecruitmentStatus( // 2. 반환 타입을 List<...>로 변경
                                                                                 @RequestParam(required = false) String status,
                                                                                 @RequestParam(required = false) String specialtyName,
                                                                                 @RequestParam(required = false) String statusType
    ) {
        log.info("🎯 Request /status with filters: status={}, specialtyName={}, statusType={}",
                status, specialtyName, statusType);

        List<RecruitmentStatusResponse> recruitments =
                recruitmentService.findStatusByFilters(status, specialtyName, statusType);

        log.info("✅ Retrieved {} recruitment records", recruitments.size());

        // 3. RecruitmentStatusListResponse 래퍼 빌드 로직 제거
        // RecruitmentStatusListResponse response = RecruitmentStatusListResponse.build(recruitments);

        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON) // 4. contentType JSON으로 변경
                .body(recruitments); // 5. 'recruitments' 리스트를 직접 body에 전달
    }
}