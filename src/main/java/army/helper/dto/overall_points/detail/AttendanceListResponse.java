package army.helper.dto.overall_points.detail;

import army.helper.domain.overall_points.AttendanceScoreRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceListResponse {

    private final String count;// 0일 , 1~4일, 5~8일, 9일 이상
    private final int score;// 출결 최대 점수 (예: 5점)

    public AttendanceListResponse(AttendanceScoreRule rules) {
        this.count = rules.getAttendanceCount();
        this.score = rules.getAttendanceScore();
    }
}