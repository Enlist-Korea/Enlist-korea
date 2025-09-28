package com.militarysupport.recruit_helper.service;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.dto.RecruitmentStatusResponse;
import com.militarysupport.recruit_helper.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    // RecruitmentApiClient는 DB 조회 로직에서는 사용되지 않으므로 제거합니다.
    // private final RecruitmentApiClient recruitmentApiClient;

    /**
     * 클라이언트 필터 조건에 따라 모집 현황을 조회하고 DTO로 변환합니다.
     * 이 메서드는 컨트롤러의 getAvailableRecruitments()와 연결됩니다.
     */
    public List<RecruitmentStatusResponse> findStatusByFilters(
            String gunGbnm,
            String mojipGbnm,
            String statusType) {

        // 1. 현재 시점 설정 (상태 판단 기준)
        OffsetDateTime now = OffsetDateTime.now();

        // 2. Repository를 호출하여 필터링된 엔티티 목록을 가져옵니다.
        //    현재는 모든 모집 현황 데이터를 가져온 후 서비스에서 필터링한다고 가정합니다.
        List<Recruitment> entities = recruitmentRepository.findFilteredRecruitments(
                gunGbnm,
                mojipGbnm,
                now
        );

        // 3. Entity 목록을 DTO 목록으로 변환합니다.
        return entities.stream()
                // DTO의 fromEntity 메서드를 사용하여 상태를 판단하고 필드를 매핑합니다.
                .map(RecruitmentStatusResponse::fromEntity)
                // 클라이언트가 요청한 '모집 상태' (모집중/예정/완료)로 최종 필터링합니다.
                .filter(dto -> statusType == null || statusType.equals(dto.getStatus()))
                .collect(Collectors.toList());
    }

    /// (기존 메서드명을 findAvailableBranches 호출에 맞게 변경)
    public List<Recruitment> getCurrentlyOpenRecruitments() {
        OffsetDateTime now = OffsetDateTime.now();
        // Repository에서 현재 모집 기간 내에 있는 데이터를 조회한다고 가정합니다.
        return recruitmentRepository.findAvailableRecruitmentsByDate(now);
    }
}
