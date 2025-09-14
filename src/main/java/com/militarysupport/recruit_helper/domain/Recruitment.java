package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

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

    @ManyToOne
    private Intake intake;

    private String status;

    private OffsetDateTime applyStart; //모집 시작 시간
    private OffsetDateTime applyEnd; //모집 마감 시간

    private Integer quota; // 모집 인원 수
    private Integer applied; //현재까지 지원한 인원

    private String sourceUrl; //출처 url 저장

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
