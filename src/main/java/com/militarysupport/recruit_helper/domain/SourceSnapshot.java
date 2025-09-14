package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "source_snapshots")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SourceSnapshot {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String sourceName;   // 지원현황

    @Column(nullable = false)
    private String sourceKey;    // 특기코드+입영월 등

    @Column(nullable = false)
    private OffsetDateTime fetchedAt;

    @Lob
    @Column(nullable = false)
    private String payload;      // 원문 JSON/XML

}