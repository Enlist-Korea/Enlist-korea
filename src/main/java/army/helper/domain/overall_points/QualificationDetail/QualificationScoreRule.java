package army.helper.domain.overall_points.QualificationDetail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "qualification_detail")
@NoArgsConstructor
public class QualificationScoreRule { //기술 자격 면허 배점

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QualificationCategory qualifications; //자격증 명칭 (국가 기술 자격증/일학습병행자격증/일반자격증/운전면허증/자격증 미소지 로 구분)

    @Column(nullable = false, length = 100)
    private String mainCondition; //자격 등급
    // (기사이상,산업기사,기능사 / (L6,L5),(L4,L3),(L2) / 공인,일반 /(대형,특수),1종보통(수동),1종보통(자동) 으로 구분)

    @Column(nullable = false)
    private String subCondition;

    @Column(nullable = false)
    private String typeCondition; //직접관련 / 간접관련

    @Column(nullable = false)
    private Integer score; //점수

    @Builder
    public QualificationScoreRule(QualificationCategory qualifications, String mainCondition, String subCondition, String typeCondition, Integer score) {
        this.qualifications = qualifications;
        this.mainCondition = mainCondition;
        this.subCondition = subCondition;
        this.typeCondition = typeCondition;
        this.score = score;
    }
}

