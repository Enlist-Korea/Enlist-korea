package army.helper.domain.overall_points;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "qualification_detail")
public class QualificationDetailEntity { //기술 자격 면허 배점

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 30)
    private String qualifications; //자격증 명칭 (국가 기술 자격증/일학습병행자격증/일반자격증/운전면허증/자격증 미소지 로 구분)

    @Column(nullable = false, length = 100)
    private String qualificationLevel; //자격 등급
    // (기사이상,산업기사,기능사 / (L6,L5),(L4,L3),(L2) / 공인,일반 /(대형,특수),1종보통(수동),1종보통(자동) 으로 구분)

    @Column(nullable = false)
    private Integer directScore; //직접 관련 점수

    @Column(nullable = false)
    private Integer indirectScore; //간접 관련 점수

}

