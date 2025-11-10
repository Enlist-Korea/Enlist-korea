package army.helper.repository;

import army.helper.domain.overall_points.AttendanceDetailEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 출결 구간(0일/1~4일/5~8일/9일 이상) → 점수 조회 리포지토리
 */
@Repository
public interface AttendanceDetailRepository extends JpaRepository<AttendanceDetailEntity, Long> {

    /** 출결 구간 문자열로 점수 조회 */
    @Query("select a.attendanceScore from AttendanceDetailEntity a where a.attendanceCount = :bucket")
    Optional<Integer> findScoreByBucket(@Param("bucket") String attendanceCountBucket);
}
