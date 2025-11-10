package army.helper.domain.overall_points.majorDetiail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class AcademicStatusScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String educationCategory; // 학력 구분 (대학교, 전문대,(2년,3년) 고졸)
    @Column(nullable = false)
    private Integer grade; //학년(1,2,3,4학년 )
    @Column(nullable = false)
    private String status; //수료,재학
    @Column(nullable = false)
    private Boolean majorStatus; //전공,비전공(True,False)
    @Column(nullable = false)
    private Integer academicScore;
}
