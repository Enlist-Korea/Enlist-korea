package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "recruitments",
        uniqueConstraints = @UniqueConstraint(columnNames = {"specialty_id","intake_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Recruitment {

    @Id
    private Long id;

    @ManyToOne
    private Specialty specialty;

    private String branch; // 군구분 (조회 성능을 높이 위한 아키텍쳐)

    private String mojipGbnm;//모집상태

    @ManyToOne
    private Intake intake; //모집 차수

    private String status;//모집상태 ( 모집중, 모집 예정, 모집완료)

    private OffsetDateTime applyStart; //모집 시작 시간
    private OffsetDateTime applyEnd; //모집 마감 시간

    private Integer quota; // 모집 인원 수
    private Integer applied; //현재까지 지원한 인원

    private BigDecimal rate; //경쟁률 (지원자 수 ÷ 모집 인원)
    private OffsetDateTime enlistStart; //입영날짜

    private String sourceUrl; //출처 url 저장

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
