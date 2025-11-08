package army.helper.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Table(name = "recruitments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"specialty_id","intake_id"}))
public class Recruitment { //모집 현황에 대한 정보를 담고 있는 엔티티

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Specialty specialty;

    @Column(nullable = false, length = 10)
    private String branch; // 군구분(육해공)

    @Column(nullable = false, length = 20)
    private String mojipGbnm; //모집구분명(장교,부사관 etc)

    @Column(nullable = false, length = 32)
    private String code; //특기 번호

    @Column(nullable = false, length = 100)
    private String specialtyName;

    @ManyToOne
    private Intake intake;

    private OffsetDateTime applyStart; //모집 시작 시간
    private OffsetDateTime applyEnd; //모집 마감 시간

    private String status; //모집상태(모집중, 모집예정, 모집완료)

    private Integer quota; //모집 인원 수

    private Integer applied;//현재 지원한 인원

    private BigDecimal rate; //경쟁률 (지원자 수 / 모집 인원)


}
