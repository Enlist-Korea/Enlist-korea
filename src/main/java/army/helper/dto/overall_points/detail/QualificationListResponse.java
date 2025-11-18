package army.helper.dto.overall_points.detail;

import army.helper.domain.overall_points.QualificationDetail.QualificationScoreRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class QualificationListResponse{

    private final String qualifications;//자격증 명칭 (국가 기술 자격증/일학습병행자격증/일반자격증/운전면허증/자격증 미소지 로 구분)
    private final String mainCondition;//자격 등급
    // (기사이상,산업기사,기능사 / (L6,L5),(L4,L3),(L2) / 공인,일반 /(대형,특수),1종보통(수동),1종보통(자동) 으로 구분)
    private final String subCondition;
    private final String typeCondition; //직접 관련, 간접 관련
    private final int score; //점수

    public QualificationListResponse(QualificationScoreRule qualification){
        this.qualifications = qualification.getQualifications().name();
        this.mainCondition = qualification.getMainCondition();
        this.subCondition = qualification.getSubCondition();
        this.typeCondition = qualification.getTypeCondition();
        this.score = qualification.getScore();
    }
}

