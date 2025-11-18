package army.helper.dto.overall_points;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ScoreQueryDto {
    // "UNIVERSITY", "HIGH_SCHOOL" 등
    private String queryGroup;

    private String category;

    // "4학년", "전공" 등
    private String mainCondition;

    // "재학", null 등
    private String subCondition;

    private String typeCondition;

    private String attendanceCount;
}
