package army.helper.repository;

import army.helper.domain.overall_points.majorDetiail.CreditBankScore;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 전공(학점은행제) 기준 점수 조회 리포지토리
 * - 학위구분/기준학점 문자열 조합으로 점수 조회
 */
@Repository
public interface CreditBankScoreRepository extends JpaRepository<CreditBankScore, Long> {

    @Query("select c.bankScore from CreditBankScore c " +
           "where c.degreeStatus = :degree and c.credits = :credits")
    Optional<Integer> findScore(@Param("degree") String degreeStatus,
                                @Param("credits") String credits);
}
