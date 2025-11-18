package army.helper.repository.overall;

import army.helper.domain.overall_points.bonusDetail.BonusCategory;
import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BonusScoreRuleRepository extends JpaRepository<BonusScoreRule, Long> {
    Optional<BonusScoreRule> findByBonusCategoryAndMainConditionAndSubCondition(
            BonusCategory category,
            String mainCondition,
            String subCondition
    );
}

