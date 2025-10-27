package com.militarysupport.scoring.infrastructure.persistence;

import com.militarysupport.recruit_helper.domain.Crawler.RecruitmentCriteriaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 병과별 배점 cap(자격/전공/출결/가산) 헤더 테이블.
 * - 크롤링 결과가 저장되는 테이블(recruitment_criteria)
 * - 모든 병과에 대해 조회 후, 각 병과 기준으로 세부 점수표를 조회/합산한다.
 */
public interface RecruitmentCriteriaRepository extends JpaRepository<RecruitmentCriteriaEntity, Long> {}
