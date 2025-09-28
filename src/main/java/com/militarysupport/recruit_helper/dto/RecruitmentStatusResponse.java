package com.militarysupport.recruit_helper.dto;

import com.militarysupport.recruit_helper.domain.Recruitment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * 클라이언트에게 반환되는 최종 모집 현황 응답 DTO.
 * DTO에 명시된 필드만 담아 응답합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentStatusResponse {

    private String branch;         // 군 구분명
    private String mojipGbnm;       // 모집 구분명
    private String status;          // 모집 상태 (모집중, 모집예정, 모집완료)
    private Integer quota;    // 선발 인원 수
    private Integer jeopsuPcnt;     // 현재 접수 인원 수
    private OffsetDateTime applyStart; // 접수 시작일시
    private OffsetDateTime applyEnd; // 접수 종료일시
    private OffsetDateTime enlistStart;     // 입영 시작 연월
    private BigDecimal rate;        // 경쟁률 분류 (API 원시 데이터)


    /**
     * Recruitment Entity를 DTO로 변환하고 모집 상태를 판단하여 할당하는 팩토리 메서드.
     */
    public static RecruitmentStatusResponse fromEntity(Recruitment entity) {
        // 현재 시각을 KST로 설정
        OffsetDateTime now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime();

        // 모집 상태를 판단
        String determinedStatus = determineRecruitmentStatus(
                entity.getApplyStart(),
                entity.getApplyEnd(),
                now
        );

        return RecruitmentStatusResponse.builder()
                .branch(entity.getBranch())
                .mojipGbnm(entity.getMojipGbnm())
                .status(determinedStatus) // 계산된 상태 할당
                .jeopsuPcnt(entity.getApplied())
                .applyStart(entity.getApplyStart())
                .applyEnd(entity.getApplyEnd())
                .enlistStart(entity.getEnlistStart())
                .rate(entity.getRate())
                .build();
    }

    /**
     * 접수 기간을 기준으로 모집 상태를 판단하는 헬퍼 메서드.
     */
    private static String determineRecruitmentStatus(
            OffsetDateTime start,
            OffsetDateTime end,
            OffsetDateTime now) {

        if (now.isBefore(start)) {
            return "모집예정";
        } else if (now.isAfter(end)) {
            return "모집완료";
        } else {
            return "모집중";
        }
    }
}
