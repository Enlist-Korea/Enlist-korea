package com.militarysupport.recruit_helper.repository;

import com.militarysupport.recruit_helper.domain.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    /**
     * 클라이언트의 필터 조건에 따라 모집 현황(Recruitment)과
     * 연관된 병과 정보(Specialty)를 조인하여 조회합니다.
     * JOIN FETCH를 사용하여 N+1 문제를 방지하고 한 번의 쿼리로 모든 정보를 가져옵니다.
     * 모집 상태(모집중, 예정, 완료)에 대한 최종 필터링은 Service 계층에서 수행합니다.
     * @param branch 군 구분명 필터 (선택적, null 허용)
     * @param mojipGbnm 모집 구분명 필터 (선택적, null 허용)
     * @param now 현재 시점 (모집 상태 판단용으로 Service 계층에서 전달받음)
     * @return 필터링된 Recruitment 엔티티 목록 (Specialty 포함)
     */
    @Query("SELECT r FROM Recruitment r " +
            // r.specialty를 통해 연결된 Specialty 엔티티를 즉시(Eager) 함께 가져오도록 JPA에 지시
            "JOIN FETCH r.specialty s " +
            // branch 필터링. 파라미터가 NULL이면 조건을 무시
            "WHERE (:branch IS NULL OR r.branch = :branch) " +
            // mojipGbnm 필터링. 파라미터가 NULL이면 조건을 무시
            "AND (:mojipGbnm IS NULL OR r.mojipGbnm = :mojipGbnm) " +
            "ORDER BY r.applyStart DESC") // 최신 모집 공고를 상단에 표시
    List<Recruitment> findFilteredRecruitments(
            @Param("branch") String branch,
            @Param("mojipGbnm") String mojipGbnm,
            @Param("now") OffsetDateTime now
    );

    /**
     * 현재 시점(now)이 모집 기간(applyStart ~ applyEnd) 내에 있는
     * '모집 중'인 데이터만 조회합니다.
     * @param now 현재 시점
     * @return 현재 모집 중인 Recruitment 엔티티 목록
     */
    @Query("""
        SELECT b FROM Recruitment b 
        WHERE b.applyStart <= :now 
          AND b.applyEnd >= :now 
   \s""")
    List<Recruitment> findAvailableRecruitmentsByDate(@Param("now") OffsetDateTime now);
}
