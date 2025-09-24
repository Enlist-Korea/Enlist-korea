package com.militarysupport.recruit_helper.service;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.domain.Requirement;
import com.militarysupport.recruit_helper.infrastructure.Api.RecruitmentApiClient;
import com.militarysupport.recruit_helper.repository.RecruitmentRepository;
import com.militarysupport.recruit_helper.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncService {

    private final RecruitmentApiClient apiClient;
    private final RecruitmentRepository recruitmentRepository;
    private final SpecialtyRepository specialtyRepository;

    @Transactional
    public int syncAll() {
        // 1. API에서 데이터를 가져옵니다 (이제 동기적으로 동작합니다).
        List<Recruitment> recruitments = apiClient.fetchCurrentlyOpenRecruitments();
        List<Requirement> requirements = apiClient.fetchRecruitmentRequirements();

        // 2. 기존 데이터를 삭제합니다. (순차적으로 실행)
        recruitmentRepository.deleteAll();
        recruitmentRepository.deleteAll();
        specialtyRepository.deleteAll();

        // 3. 새로 가져온 데이터를 저장합니다.
        recruitmentRepository.saveAll(recruitments);
        log.info("Saved {} recruitments.", recruitments.size());

        // 4. (추가) 필요에 따라 requirements 또는 specialties 저장 로직 구현
        // specialtyRepository.saveAll(requirements);

        return recruitments.size(); // 동기화된 항목 수를 반환합니다.
    }
}