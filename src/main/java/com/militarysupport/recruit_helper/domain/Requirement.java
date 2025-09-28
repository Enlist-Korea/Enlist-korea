package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

// 병과별 상세 요구 조건 및 규칙을 저장하는 엔티티
@Entity
@Table(name = "requirements",
        // specialty_id가 유일함을 보장하여 Specialty와의 1:1 관계를 명확히 함
        uniqueConstraints = @UniqueConstraint(columnNames = {"specialty_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Requirement {

    // [수정: ID는 Specialty ID를 공유하도록 변경 - Option 1]
    // Requirement 테이블의 기본키는 Specialty 테이블의 기본키를 외래키로 겸함 (공유 기본키)
    @Id
    @Column(name = "specialty_id")
    private Long id;

    // [수정: @MapsId 사용] - 엔티티의 ID가 곧 외래키임을 명시 (1:1 관계의 모범 사례)
    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    // 학력 수준 (예: 고졸, 대졸)
    @Column(length = 50)
    private String educationLevel;

    // 필수 전공명 (예: 컴퓨터공학, 기계공학)
    @Column(length = 100)
    private String majorRequired;

    // 자격증 목록 및 규칙을 JSON 문자열로 저장
    // 길어질 수 있으므로 @Column만 사용하여 VARCHAR 길이를 DB 기본값 또는 지정값으로 설정
    @Column(columnDefinition = "TEXT")
    private String certificatesJson;

    // 시력 요건
    @Column(length = 50)
    private String visionRequirement;

    // 최소 신체 등급
    @Column(nullable = false)
    private Integer medicalClassMin;

    // 나이 요건
    private Integer ageMin;
    private Integer ageMax;
}
//```eof
//
//### **주요 리팩토링 사항**
//
//        1.  **공유 기본키 (Shared Primary Key) 적용**:
//        * **문제**: `Specialty`와 `Requirement`는 1:1 관계이며, `Specialty`의 ID가 곧 `Requirement`의 외래키가 됩니다.
//    * **해결**: `@Id`와 `@JoinColumn`에 동일한 컬럼명(`specialty_id`)을 사용하고 **`@MapsId`**를 추가했습니다. 이는 `Requirement`의 ID가 `Specialty`의 ID를 공유함을 명시하여 1:1 관계를 JPA 표준에 맞게 명확히 합니다.
//
//        2.  **데이터 타입 개선**:
//        * `@Column`만 있던 `certificatesJson` 필드에 `columnDefinition = "TEXT"`를 추가하여 긴 JSON 문자열을 저장할 수 있도록 했습니다. (MySQL의 경우 `TEXT` 타입으로 매핑되어 긴 문자열 저장에 유리합니다.)
//
//        3.  **Nullable 명시**:
//        * `medicalClassMin`은 지원 자격에서 매우 기본적인 항목이므로, `@Column(nullable = false)`를 추가하여 **null이 될 수 없도록** 데이터 무결성을 높였습니다.
//    * 나머지 필드(`educationLevel`, `ageMin`, `ageMax` 등)는 조건이 없는 경우 `null`을 허용할 수 있도록 `nullable = true` (기본값)를 유지했습니다.