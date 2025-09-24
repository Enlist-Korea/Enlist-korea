package com.militarysupport.recruit_helper.service;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.infrastructure.Api.RecruitmentApiClient;
import com.militarysupport.recruit_helper.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;
    private final RecruitmentApiClient recruitmentApiClient;

    /// 현재 모집 중인 병과 조회 서비스 (DB 조회)
    public List<Recruitment> getCurrentlyOpenRecruitments() {
        OffsetDateTime now = OffsetDateTime.now();
        return recruitmentRepository.findAvailableBranches(now);
    }

    // 공공 API에서 모집 현황 가져오는 서비스
    public List<Recruitment> fetchRecruitmentsFromApi() {
        return recruitmentApiClient.fetchCurrentlyOpenRecruitments();
    }

}
