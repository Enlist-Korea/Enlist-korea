package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;

import java.time.OffsetDateTime;

//군 별 특기병 관리 api
@Entity
@Table(name = "external_specialty_map",
        uniqueConstraints = @UniqueConstraint(columnNames = {"source","branch","ext_code"}))
public class ExternalSpecialtyMap {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;// "MMA_STATUS" (지원현황 API) | "MMA_REQUIREMENT" (특기조건 API)

    @Column(nullable = false)
    String source;

    @Column(nullable = false)
    String branch;   // ARMY/NAVY/AIR/MARINE

    @Column(name="ext_code", nullable = false)
    String extCode; // 외부 특기코드
    String extName; // 외부 표시명(선택)

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name="specialty_id", nullable = false)
    private Specialty specialty;                       // 내부 표준 특기
    OffsetDateTime createdAt; OffsetDateTime updatedAt;
}