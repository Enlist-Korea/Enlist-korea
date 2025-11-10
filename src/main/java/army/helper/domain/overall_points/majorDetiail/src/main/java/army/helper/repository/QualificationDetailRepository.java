package army.helper.repository;

import army.helper.domain.overall_points.QualificationDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 기술자격·면허 배점 테이블 조회용 리포지토리
 * - 특정 자격군 + 등급 조합의 '직접/간접' 점수만 즉시 조회
 * - 엔티티 게터 없이 JPQL로 점수 필드만 select → 의존성 최소화
 */
@Repository
public interface QualificationDetailRepository extends JpaRepository<QualificationDetailEntity, Long> {

    /** 자격군/등급에 대한 '직접 관련' 점수 */
    @Query("select q.directScore from QualificationDetailEntity q " +
           "where q.qualifications = :qualifications and q.qualificationLevel = :level")
    Optional<Integer> findDirectScore(@Param("qualifications") String qualifications,
                                      @Param("level") String level);

    /** 자격군/등급에 대한 '간접 관련' 점수 */
    @Query("select q.indirectScore from QualificationDetailEntity q " +
           "where q.qualifications = :qualifications and q.qualificationLevel = :level")
    Optional<Integer> findIndirectScore(@Param("qualifications") String qualifications,
                                        @Param("level") String level);
}
