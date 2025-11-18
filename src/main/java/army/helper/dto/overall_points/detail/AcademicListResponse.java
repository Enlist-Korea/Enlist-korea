package army.helper.dto.overall_points.detail;

import army.helper.domain.overall_points.majorDetiail.AcademicScoreRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AcademicListResponse {
    private final String category; // 학력 구분 (대학교, 전문대,(2년,3년) 고졸)
    private final String majorCondition; //학년(1,2,3,4학년 )
    private final String subCondition;//수료,재학
    private final int score; //점수

    public AcademicListResponse(AcademicScoreRule rule) {
        this.category = rule.getEducationCategory().name(); // Enum을 String으로 변환
        this.majorCondition = rule.getMajorCondition();
        this.subCondition = rule.getSubCondition();
        this.score = rule.getAcademicScore();
    }
}
