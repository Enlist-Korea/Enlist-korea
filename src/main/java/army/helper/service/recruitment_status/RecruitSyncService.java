package army.helper.service.recruitment_status;

import army.helper.domain.recruitment_status.Branch;
import army.helper.domain.recruitment_status.Recruitment;
import army.helper.domain.recruitment_status.Specialty;
import army.helper.dto.recruitment_status.RecruitmentStatusResponse;
import army.helper.dto.recruitment_status.RequirementApiDto;
import army.helper.infrastructure.ApiClient;
import army.helper.repository.recruitment.RecruitmentRepository;
import army.helper.repository.recruitment.SpecialtyRepository;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.LinkedHashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncService {

    private final ApiClient apiClient;
    private final RecruitmentRepository recruitmentRepository;
    private final SpecialtyRepository specialtyRepository;

    public int syncAll() {
        log.info("Starting data synchronization process.");

        // API 데이터 호출
        List<RecruitmentStatusResponse> recruitments = apiClient.fetchCurrentRecruitments();
        List<RequirementApiDto> requirements = apiClient.fetchCurrentRecruitRequirements();

        //  기존 데이터 삭제
        recruitmentRepository.deleteAll();
        specialtyRepository.deleteAll();

        // ⃣ 모집 현황 저장
        List<Recruitment> recruitmentEntities = recruitments.stream()
                .filter(dto -> hasText(dto.getSpecialtyCode()))
                .map(RecruitmentMapper::toEntity)
                .toList();

        recruitmentRepository.saveAll(recruitmentEntities);
        log.info("Saved recruitments: {}", recruitmentEntities.size());


        List<Specialty> allSpecialties = recruitmentEntities.stream()
                .flatMap(r -> mapRequirementToSpecialties(requirements, r.getBranch()).stream())
                .peek(s -> log.info("🔍 specialty branch={}, code={}, name={}", s.getBranch(), s.getCode(), s.getName()))
                .filter(s -> s.getBranch() != null && s.getCode() != null) // null 코드 제거
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(
                                s -> s.getBranch().name() + "|" + s.getCode(), // (branch, code) 키
                                Function.identity(),
                                (a, b) -> a,
                                LinkedHashMap::new
                        ),
                        m -> new ArrayList<>(m.values())
                ));

        specialtyRepository.saveAll(allSpecialties);
        log.info("Saved specialties: {}", allSpecialties.size());

        return recruitmentEntities.size();
    }

    //  branch enum 타입으로 직접 주입
    private List<Specialty> mapRequirementToSpecialties(List<RequirementApiDto> requirements, Branch branch) {
        return requirements.stream()
                .map(req -> Specialty.builder()
                        .branch(branch) //
                        .code(mapApiToStandardCode(req.getSpecialtyCode(), req.getSpecialtyName()))
                        .mojipGbnm(req.getMojipGbnm() != null ? req.getMojipGbnm() : "기술병")
                        .name(req.getSpecialtyName() != null && !req.getSpecialtyName().isBlank()
                                ? req.getSpecialtyName()
                                : req.getMajorRequirement() != null
                                        ? req.getMajorRequirement()
                                        : "미상특기")
                        .requiredCertificateName(hasText(req.getCertificateName()) ? req.getCertificateName() : null)
                        .requiredMajorNames(hasText(req.getMajorRequirement()) ? req.getMajorRequirement() : null)
                        .active(true)
                        .build())
                .filter(s -> s != null) // null Specialty 제거
                .toList();

    }



    private String mapApiToStandardCode(String apiCode, String fallbackName) {
        if (apiCode == null || apiCode.isBlank() || "null".equalsIgnoreCase(apiCode)) {
            if (fallbackName != null && !fallbackName.isBlank()) {
                return "ST_GEN_" + Math.abs(fallbackName.trim().toLowerCase().hashCode()); // 결정적 대체키
            }
            return null; // 코드 생성 불가
        }
        return "ST_" + apiCode.trim().toUpperCase();
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }
}