package army.helper.domain.overall_points;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class AttendanceScoreRule { //출결 점수 배점

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String attendanceCount; // 0일 , 1~4일, 5~8일, 9일 이상

    @Column(nullable = false)
    private Integer attendanceScore; // 출결 최대 점수 (예: 5점)

    @Builder
    public AttendanceScoreRule(String attendanceCount, Integer attendanceScore) {
        this.attendanceCount = attendanceCount;
        this.attendanceScore = attendanceScore;
    }
}

