package army.helper.service.recruitment_status;

import army.helper.domain.recruitment_status.Branch;
import army.helper.domain.recruitment_status.Recruitment;
import army.helper.dto.recruitment_status.RecruitmentStatusResponse;
import army.helper.repository.recruitment.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public List<RecruitmentStatusResponse> findStatusByFilters(
            String status,
            String specialtyName,
            String statusType) {

        OffsetDateTime now = OffsetDateTime.now();

        Branch branchEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                // 'status' 문자열(예: "ARMY")을 Branch Enum으로 변환
                branchEnum = Branch.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid branch status value: {}", status);
                // 잘못된 값이면 null을 유지하여 전체 검색
            }
        }
        List<Recruitment> entities = recruitmentRepository.findFileRecruitments(
                branchEnum,
                specialtyName,
                now
        );

// 람다 사용으로 타입 명시 (Object 문제 해결)
        return entities.stream()
                .map(entity -> RecruitmentStatusResponse.fromEntity(entity))
                // 6. [수정] statusType 필터링 (DTO 버그 수정됨)
                .filter(dto -> statusType == null || statusType.isBlank() || statusType.equals(dto.getMojipStatus()))
                .collect(Collectors.toList());
    }
}