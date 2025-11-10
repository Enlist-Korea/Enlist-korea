package army.helper.controller;

import army.helper.dto.overall_points.OverallPoints;
import army.helper.dto.overall_points.OverallPointsRequest;
import army.helper.service.overall_points.OverallPointsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/**
 * 점수 계산 컨트롤러
 * - POST /api/points/calculate : 1차 평가(자격/전공/출결/가산) 합산 결과 반환
 * - Body는 OverallPointsRequest(JSON), 응답은 OverallPoints(JSON)
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
@Slf4j
public class OverallPointsController {

    private final OverallPointsService service;

    /** 총점 계산 엔드포인트 */
    @PostMapping(value = "/calculate", consumes = MediaType.APPLICATION_JSON_VALUE,
                                   produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OverallPoints> calculate(@Valid @RequestBody OverallPointsRequest request) {
        log.info("🎯 Calculate points: vehicleDriving={}, qualifications={}, majorType={}, attendance={}, bonus={}",
                request.isVehicleDrivingSpecialty(),
                request.getQualifications() != null ? request.getQualifications().size() : 0,
                request.getMajor() != null ? request.getMajor().getType() : null,
                request.getAttendanceCountBucket(),
                request.getBonusTotal());

        OverallPoints result = service.calculate(request);
        return ResponseEntity.ok(result);
    }

    /**
     * (선택) 디버깅용 보조 엔드포인트
     * - 단건 자격 점수 확인 시 임시로 사용 가능(운영에서 제거 가능)
     */
    @GetMapping(value = "/qualification", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<OverallPoints> testQualification(@RequestParam("name") String name,
                                                           @RequestParam("level") String level,
                                                           @RequestParam(value = "related", defaultValue = "true") boolean related,
                                                           @RequestParam(value = "vehicleDriving", defaultValue = "false") boolean vehicleDriving) {
        OverallPointsRequest req = OverallPointsRequest.builder()
                .vehicleDrivingSpecialty(vehicleDriving)
                .qualification(OverallPointsRequest.QualificationSelection.builder()
                        .qualifications(name).qualificationLevel(level).related(related).build())
                // 전공/출결/가산은 임의 기본값으로 세팅
                .major(OverallPointsRequest.MajorSelection.builder()
                        .type(OverallPointsRequest.MajorSelection.MajorType.ACADEMIC)
                        .academic(OverallPointsRequest.AcademicPayload.builder()
                                .educationCategory("고졸").grade(1).status("재학").major(false).build())
                        .build())
                .attendanceCountBucket("0일")
                .bonusTotal(0)
                .build();

        return ResponseEntity.ok(service.calculate(req));
    }
}
