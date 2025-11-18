package army.helper.domain.recruitment_status;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Entity
@Table(name = "intakes"
    ,uniqueConstraints = @UniqueConstraint(columnNames = {"month" , "intakeNo"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intake { //모집기수/차수 시간적 기준을 관리를 위한 엔티티

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String month;

    private String intakeNo;

    @Column(nullable = false)
    private OffsetDateTime createAt;

    @Column(nullable = false)
    private OffsetDateTime updateAt;
}
