package com.militarysupport.recruit_helper.dto;

import lombok.Data;

/**
 * 모집 조건 API (외부 API)의 응답 데이터를 담는 DTO입니다.
 * 이 객체는 DB 엔티티가 아니며, 순수하게 API 통신 목적으로만 사용됩니다.
 */
@Data // Getter, Setter, toString 등을 자동 생성
public class RequirementApiDto {

    // API에서 제공하는 필수 식별 정보
    private String gunGbnm;     // 군 구분명 (예: 육군, 해군)
    private String gsteukgiCd;  // 특기 코드 (예: S18, 55.02 등 API 형식 그대로)
    private String gsteukgiNm;  // 특기명 (예: 보병, 운전병)

    // API에서 제공하는 필수 지원 조건 필드
    private String majorRequired;       // 전공명 (API 필드명이 '전공'과 관련 있다고 가정)
    private String certificateName;     // 자격증명 (API 필드명이 '자격면허'와 관련 있다고 가정)
    // ... 기타 필드 (medicalClassMin 등)
}
