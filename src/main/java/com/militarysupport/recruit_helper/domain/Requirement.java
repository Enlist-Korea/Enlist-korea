package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "requirements",
        uniqueConstraints = @UniqueConstraint(columnNames = {"specialty_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Requirement {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "specialty_id", nullable = false)
    private Specialty specialty;

    private String educationLevel;    // 고졸/대졸 등
    private String majorRequired;     // 전공명

    @Column
    private String certificatesJson;  // 자격증 json 문자열을 받을 예정

    private String visionRequirement; //시력
    private Integer medicalClassMin; //신체등급
    private Integer ageMin; //최소나이
    private Integer ageMax; //최대나이


}
