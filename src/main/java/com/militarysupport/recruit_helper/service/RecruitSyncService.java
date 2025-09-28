package com.militarysupport.recruit_helper.service;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.domain.Specialty;
import com.militarysupport.recruit_helper.dto.RequirementApiDto;
import com.militarysupport.recruit_helper.infrastructure.Api.RecruitmentApiClient;
import com.militarysupport.recruit_helper.repository.RecruitmentRepository;
import com.militarysupport.recruit_helper.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncService {

    private final RecruitmentApiClient apiClient;
    private final RecruitmentRepository recruitmentRepository;
    private final SpecialtyRepository specialtyRepository;

    // 특기 코드 매핑을 위한 가상의 헬퍼 메서드
    private String mapApiCodeToStandardCode(String apiCode) {
        // 실제로는 DB 매핑 테이블을 조회하는 로직이 들어갑니다.
        return "ST_" + apiCode;
    }

    @Transactional
    public int syncAll() {
        log.info("Starting data synchronization process.");

        // 1. API에서 데이터를 가져옵니다 (동기적 호출).
        List<Recruitment> recruitments = apiClient.fetchCurrentlyOpenRecruitments();
        // [수정된 API 클라이언트 사용 가정] 외부 API 응답 DTO를 사용합니다.
        List<RequirementApiDto> requirementApiDto = apiClient.fetchRecruitmentRequirements();

        // 2. 기존 데이터를 삭제합니다.
        recruitmentRepository.deleteAll();
        specialtyRepository.deleteAll();

        // 3. Recruitment 현황 데이터를 저장합니다.
        recruitmentRepository.saveAll(recruitments);
        log.info("Saved {} recruitments.", recruitments.size());

        // 4. Requirement 조건 데이터를 Specialty 엔티티로 변환합니다.
        List<Specialty> specialtiesToSave = mapRequirementsToSpecialties(requirementApiDto);

        // 5. 변환된 Specialty 조건 데이터를 저장합니다.
        specialtyRepository.saveAll(specialtiesToSave);
        log.info("Successfully saved {} specialty/rule data.", specialtiesToSave.size());

        return recruitments.size(); // 동기화된 항목 수를 반환합니다.
    }
    /**
     * Requirement API DTO를 DB 엔티티인 Specialty로 변환하는 매핑 로직입니다.
     */
    private List<Specialty> mapRequirementsToSpecialties(List<RequirementApiDto> requirements) {

        return requirements.stream().map(req -> {

            // 필수 지원 조건이 null이거나 비어있을 경우를 대비한 헬퍼 메서드
            String major = req.getMajorRequired();
            String certName = req.getCertificateName();

            // API DTO에서 필수 자격/전공 정보를 추출하여 Specialty 필드에 할당합니다.
            Specialty specialty = Specialty.builder()

                    // 1. 병과 식별 정보 할당 (DTO에서 직접 가져옴)
                    .branch(req.getGunGbnm())
                    .code(mapApiCodeToStandardCode(req.getGsteukgiCd()))
                    .name(req.getGsteukgiNm())

                    // 2. 필수 지원 조건 정보 할당 (NULL 안전하게 처리)
                    .requiredCertificateName(certName != null && !certName.isBlank() ? certName : null)
                    .requiredMajorName(major != null && !major.isBlank() ? major : null)

                    .build();
            return specialty;
        }).collect(Collectors.toList());
    }
}
