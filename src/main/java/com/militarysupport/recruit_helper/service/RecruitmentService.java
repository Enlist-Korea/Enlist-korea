package com.militarysupport.recruit_helper.service;

import com.militarysupport.recruit_helper.domain.Recruitment;
import com.militarysupport.recruit_helper.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    //현재 모집중인 병과 조회 서비스
     public List<Recruitment> getCurrentlyOpenRecruitments() {
         OffsetDateTime now = OffsetDateTime.now();  // 현재 시간
         return recruitmentRepository.findAvailableBranches(now);
     }

}
