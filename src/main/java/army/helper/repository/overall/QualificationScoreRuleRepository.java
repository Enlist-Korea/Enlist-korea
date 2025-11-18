package army.helper.repository.overall;

import army.helper.domain.overall_points.QualificationDetail.QualificationCategory;
import army.helper.domain.overall_points.QualificationDetail.QualificationScoreRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QualificationScoreRuleRepository extends JpaRepository<QualificationScoreRule, Long> {
    Optional<QualificationScoreRule> findByQualificationsAndMainConditionAndSubConditionAndTypeCondition(
            QualificationCategory qualifications,
            String mainCondition,
            String subCondition,
            String typeCondition
    );
}
