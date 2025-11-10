package army.helper.domain.overall_points.majorDetiail;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class CreditBankScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String degreeStatus; //학위 구분(학사, 전문학사(3년), 전문학사(2년)
    @Column(nullable = false)
    private String credits; //학력 인정 기준 학점
    @Column(nullable = false)
    private Integer bankScore;
}
