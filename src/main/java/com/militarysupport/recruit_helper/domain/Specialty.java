package com.militarysupport.recruit_helper.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.time.ZoneId;

// 군 지원 현황 API의 특기/병과 기본 정보를 저장하는 엔티티
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
    // 군 구분 (육군, 해군, 공군 등)
    // Branch Enum으로 타입 변경을 고려할 수 있지만, 현재는 String 유지
    @Column(nullable = false, length = 10)
    private String branch;

    @Column(nullable = false, length = 20)
    private String mojipGbnm;

    // 특기 코드 (표준화된 코드 사용을 가정)
    @Column(nullable = false, length = 32)
    private String code;

    // 특기명
    @Column(nullable = false, length = 100)
    private String name;

    // 특기에 대한 간단한 요약 설명
    private String summary;

    @Column(length = 255, nullable = true)
    private String requiredCertificateName; // 필수 자격증명 (예: '정보처리기능사' 또는 '운전면허')

    @Column(length = 255, nullable = true)
    private String requiredMajorName;  // 필수 전공명 (예: '컴퓨터공학' 또는 '기계공학')

    // 데이터의 활성화 상태 (필터링 용이)
    @Column(nullable = false)
    private boolean isActive = true; // 기본값 true로 설정

    // 등록 시간 (데이터 자동 삽입)
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // 수정 시간 (데이터 자동 업데이트)
    @Column(nullable = false)
    private OffsetDateTime updatedAt;

    /**
     * 엔티티가 영속화되기 전(INSERT 전)에 시간을 자동으로 설정합니다.
     * DB에서 자동으로 처리할 수도 있지만, 애플리케이션 레벨에서 관리합니다.
     */
    @PrePersist
    protected void onCreate() {
        OffsetDateTime now = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
        this.createdAt = now;
        this.updatedAt = now;
    }

    /**
     * 엔티티가 업데이트되기 전(UPDATE 전)에 수정 시간을 자동으로 갱신합니다.
     */
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now(ZoneId.of("Asia/Seoul"));
    }
}
