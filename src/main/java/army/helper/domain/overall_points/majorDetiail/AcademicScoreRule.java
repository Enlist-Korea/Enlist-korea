package army.helper.domain.overall_points.majorDetiail;

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
public class AcademicScoreRule { //학교 점수 배점

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private AcademicCategory educationCategory; // 학력 구분 (대학교, 전문대,(2년,3년) 고졸)

    @Column(nullable = false)
    private String  majorCondition; //학년(1,2,3,4학년 )

    @Column(nullable = false)
    private String subCondition; //수료,재학

    @Column(nullable = false)
    private Integer academicScore; //점수

    @Builder
    public AcademicScoreRule(AcademicCategory educationCategory, String majorCondition, String subCondition, Integer academicScore){
        this.educationCategory = educationCategory;
        this.majorCondition = majorCondition;
        this.subCondition = subCondition;
        this.academicScore = academicScore;
    }
}
