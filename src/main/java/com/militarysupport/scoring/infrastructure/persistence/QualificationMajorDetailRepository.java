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

    // ─────────────── 점수 계산용 ───────────────

    /** 특정 병과의 scoreType + itemLevel에 정확히 일치하는 점수 */
    @Query("""
           select q.score
           from QualificationMajorDetailEntity q
           where q.criteria.id = :criteriaId
             and q.scoreType = :scoreType
             and q.itemLevel = :itemLevel
           """)
    Optional<Integer> findPointsExact(Long criteriaId, String scoreType, String itemLevel);

    /** 특정 병과의 scoreType 전체 목록 조회 (출결 밴드 탐색용) */
    @Query("""
           select q
           from QualificationMajorDetailEntity q
           where q.criteria.id = :criteriaId
             and q.scoreType = :scoreType
           """)
    List<QualificationMajorDetailEntity> findAllByType(Long criteriaId, String scoreType);


    // ─────────────── 옵션/자동완성용 ───────────────

    /** 자격/전공 타입별 distinct itemLevel 조회 (셀렉트 박스 구성용) */
    @Query("""
        select distinct q.itemLevel
        from QualificationMajorDetailEntity q
        where q.scoreType = :scoreType
        order by q.itemLevel
    """)
    List<String> findDistinctItemLevelByType(String scoreType);

    /** 키워드 포함 검색 (자동완성용) – 대소문자 무시 */
    @Query("""
        select distinct q.itemLevel
        from QualificationMajorDetailEntity q
        where lower(q.itemLevel) like lower(concat('%', :query, '%'))
        order by q.itemLevel
    """)
    List<String> searchItemLevelContaining(String query);
}
