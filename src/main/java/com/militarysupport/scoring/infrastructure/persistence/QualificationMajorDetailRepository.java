package com.militarysupport.scoring.infrastructure.persistence;

import com.militarysupport.recruit_helper.domain.Crawler.QualificationMajorDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

/**
 * 자격/전공/출결 "세부 점수표" 조회용 리포지토리.
 * - scoreType: "자격" | "전공" | "출결"
 * - itemLevel: "기사이상", "전공:4년제졸업", "1-4일" 등
 */
public interface QualificationMajorDetailRepository extends JpaRepository<QualificationMajorDetailEntity, Long> {

    @Query("""
           select q.score
           from QualificationMajorDetailEntity q
           where q.criteria.id = :criteriaId
             and q.scoreType = :scoreType
             and q.itemLevel = :itemLevel
           """)
    Optional<Integer> findPointsExact(Long criteriaId, String scoreType, String itemLevel);

    @Query("""
           select q
           from QualificationMajorDetailEntity q
           where q.criteria.id = :criteriaId
             and q.scoreType = :scoreType
           """)
    List<QualificationMajorDetailEntity> findAllByType(Long criteriaId, String scoreType);
}
