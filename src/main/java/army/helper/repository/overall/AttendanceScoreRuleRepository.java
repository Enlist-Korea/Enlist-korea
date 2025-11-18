package army.helper.repository.overall;

import army.helper.domain.overall_points.AttendanceScoreRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceScoreRuleRepository extends JpaRepository<AttendanceScoreRule, Long> {
    Optional<AttendanceScoreRule> findByAttendanceCount(
            String attendanceCount
    );
}
