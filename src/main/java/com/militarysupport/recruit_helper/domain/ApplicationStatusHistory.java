package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "application_status_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"recruitment_id","observedAt"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationStatusHistory {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruitment_id", nullable = false)
    private Recruitment recruitment;

    @Column(nullable = false)
    private OffsetDateTime observedAt;

    private Integer quota;
    private Integer applied;

    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
