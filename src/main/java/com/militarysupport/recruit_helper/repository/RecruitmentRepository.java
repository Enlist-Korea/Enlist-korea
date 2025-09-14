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

    // 현재 날짜 기준으로 모집 기간 내 병과 조회
    @Query("""
        SELECT b FROM Recruitment b\s
        WHERE b.applyStart <= :now\s
          AND b.applyEnd >= :now\s
          AND b.status = '모집중'
   \s""")

    List<Recruitment> findAvailableBranches(@Param("now") OffsetDateTime now);

}
