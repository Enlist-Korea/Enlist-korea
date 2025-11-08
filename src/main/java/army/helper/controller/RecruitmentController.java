package army.helper.controller;

import army.helper.dto.RecruitmentStatusResponse;
import army.helper.service.RecruitmentService;
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

    @GetMapping("/status")
    public ResponseEntity<List<RecruitmentStatusResponse>> getAvailableRecruitment(
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "specialtyName", required = false) String specialtyName,
            @RequestParam(value = "statusType", required = false) String statusType
    ) {
        List<RecruitmentStatusResponse> result = recruitmentService.findStatusByFilters(
                status,
                specialtyName,
                statusType);

        return ResponseEntity.ok(result);
    }
}
