package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

//군 지원 현황 api
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Getter
@Entity
@Table(name = "specialties",
        uniqueConstraints = @UniqueConstraint(columnNames = {"branch", "code"}))
public class Specialty {

    @Id
    private Long id;

    @Column(nullable = false)   // ARMY/NAVY/AIR/MARINE 육/해/공/해병
    private String branch;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    private String summary; //요약

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;

}
