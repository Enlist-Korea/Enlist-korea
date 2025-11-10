package army.helper.repository;

import army.helper.domain.overall_points.majorDetiail.AcademicStatusScore;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 전공(학적) 기준 점수 조회 리포지토리
 * - 교육구분/학년/상태/전공여부 조합으로 점수 조회
 */
@Repository
public interface AcademicStatusScoreRepository extends JpaRepository<AcademicStatusScore, Long> {

    @Query("select a.academicScore from AcademicStatusScore a " +
           "where a.educationCategory = :category and a.grade = :grade " +
           "and a.status = :status and a.majorStatus = :major")
    Optional<Integer> findScore(@Param("category") String educationCategory,
                                @Param("grade") Integer grade,
                                @Param("status") String status,
                                @Param("major") Boolean majorStatus);
}
