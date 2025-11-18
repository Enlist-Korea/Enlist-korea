package army.helper.domain.overall_points.bonusDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class BonusScoreRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private  BonusCategory bonusCategory;

    @Column(nullable = false)
    private String mainCondition;

    @Column(nullable = false)
    private String subCondition;

    @Column(nullable = false)
    private Integer  bonusScore;

    @Builder
    public BonusScoreRule(BonusCategory bonusCategory, String mainCondition, String subCondition, Integer bonusScore) {
        this.bonusCategory = bonusCategory;
        this.mainCondition = mainCondition;
        this.subCondition = subCondition;
        this.bonusScore = bonusScore;
    }
}
