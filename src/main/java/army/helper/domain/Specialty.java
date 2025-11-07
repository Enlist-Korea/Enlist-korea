package army.helper.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 군 지원 현황 API의 특기/병과 기본 정보를 저장하는 엔티티
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name ="specialties"
    ,uniqueConstraints = @UniqueConstraint(columnNames = {"branch", "code"}))
public class Specialty { // 특기별 지원 가능 정보를 담고 있는 엔티티 (--학과)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String branch;

    @Column(nullable = false, length = 20)
    private String mojipGbnm; //모집구분명(장교,부사관 etc)

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String summary;

    @Column(nullable = true, length = 255)
    private String requiredCertificateName;

    @Column(nullable = true, length = 255)
    private String requiredMajorNames;

}
