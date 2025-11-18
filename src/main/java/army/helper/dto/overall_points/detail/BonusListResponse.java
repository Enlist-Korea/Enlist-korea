package army.helper.dto.overall_points.detail;

import army.helper.domain.overall_points.bonusDetail.BonusCategory;
import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BonusListResponse {

    private String category;
    private String mainCondition;
    private String subCondition;
    private int  bonusScore;

    public BonusListResponse(BonusScoreRule rule){
        this.category = rule.getBonusCategory().name();
        this.mainCondition = rule.getMainCondition();
        this.subCondition = rule.getSubCondition();
        this.bonusScore = rule.getBonusScore();
    }
}
