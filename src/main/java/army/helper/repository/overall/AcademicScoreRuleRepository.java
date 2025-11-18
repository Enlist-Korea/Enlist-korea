package army.helper.repository.overall;

import army.helper.domain.overall_points.majorDetiail.AcademicCategory;
import army.helper.domain.overall_points.majorDetiail.AcademicScoreRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AcademicScoreRuleRepository extends JpaRepository<AcademicScoreRule, Long> {

    Optional<AcademicScoreRule> findByEducationCategoryAndMajorConditionAndSubCondition(
            AcademicCategory category,
            String majorCondition,
            String subCondition
    );
}
