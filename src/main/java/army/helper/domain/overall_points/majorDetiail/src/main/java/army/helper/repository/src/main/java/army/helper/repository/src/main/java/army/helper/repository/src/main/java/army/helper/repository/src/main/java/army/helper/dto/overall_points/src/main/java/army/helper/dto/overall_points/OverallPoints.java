package army.helper.dto.overall_points;

import lombok.*;

/**
 * [응답 DTO] 항목별 점수 + 합계
 * - 자격(최대 50), 전공(최대 40), 출결(최대 5), 가산(최대 10) → total
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallPoints {
    private Integer qualificationScore; // 기술자격·면허
    private Integer majorScore;         // 전공학과
    private Integer attendanceScore;    // 출결상황
    private Integer bonusScore;         // 가산점
    private Integer total;              // 합계(1차 평가)
}
