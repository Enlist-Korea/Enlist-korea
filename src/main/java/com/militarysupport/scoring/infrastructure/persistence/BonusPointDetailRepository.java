package com.militarysupport.scoring.infrastructure.persistence;

import com.militarysupport.recruit_helper.domain.Crawler.BonusPointDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.List;

/**
 * 가산점 카탈로그 조회용 리포지토리.
 * - category, detail(label)별 점수 부여값을 담는다.
 * - 같은 category에서 사용자가 고른 label만 인정하여 합산.
 */
public interface BonusPointDetailRepository extends JpaRepository<BonusPointDetailEntity, Long> {

    @Query("""
           select b
           from BonusPointDetailEntity b
           where b.criteria.id = :criteriaId
             and b.category in :categories
           """)
    List<BonusPointDetailEntity> findByCriteriaAndCategories(Long criteriaId, Collection<String> categories);
}
