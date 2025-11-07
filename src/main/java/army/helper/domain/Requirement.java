package army.helper.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "requirements",
    uniqueConstraints = @UniqueConstraint(columnNames = {"specialty_id"}))
public class Requirement { // 병과별 상세 요구 조건 및 규칙을 저장하는 엔티티

    // Specialty ID를 공유 (공유 기본키)
    @Id
    @Column(name = "specialty_id")
    private Long id;

    // 엔티티의 id가 곧 외래키임을 명시(1:1)
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    @Column(length = 50)
    private String educationLevel; //학력 수준 (고졸, 대졸 등)

    @Column(length = 100)
    private String majorRequirement; //필수 전공명 (컴공,기공 등)

    @Column(columnDefinition = "TEXT")
    private String certificatesJson; //자격증 목록 및 규칙을 JSON 문자열로 저장

}
