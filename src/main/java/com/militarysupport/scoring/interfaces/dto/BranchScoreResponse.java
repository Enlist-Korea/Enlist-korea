package com.militarysupport.scoring.interfaces.dto;

/**
 * 병과별 계산 결과 DTO.
 * - 도큐먼트(서류) 점수만 합산(자격/전공/출결/가산)
 * - eligible은 컷라인 데이터가 있을 경우 활용(현재는 true 고정)
 */
public record BranchScoreResponse(
        String branchId,            // recruitment_criteria.id (문자열화)
        String branchName,          // recruitment_criteria.militaryService
        double qualScore,           // 자격/면허 점수 (cap 적용)
        double majorScore,          // 전공 점수 (cap 적용)
        double attendanceScore,     // 출결 점수 (cap 적용)
        double bonusScore,          // 가산 점수 (cap 적용)
        double totalDocumentScore,  // 총 서류점수 (qual+major+attendance+bonus)
        boolean eligible            // 지원 가능여부(정책/컷라인 연결 가능)
) {}
