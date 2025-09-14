package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "intakes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"month", "intakeNo"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Intake {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String month;

    private String intakeNo;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false)
    private OffsetDateTime updatedAt;
}
