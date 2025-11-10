package com.militarysupport.scoring.interfaces.dto;

import java.util.List;

/**
 * 점수 계산 입력 DTO
 * - 자격/전공: ID 또는 라벨(라벨만으로도 매칭 가능)
 * - 출결: absences(결석일수) 외에 특례 입력값을 그대로 받아 백엔드에서 변환/적용
 * - 가산점: category/label 또는 categoryId/detailId (ID가 있으면 ID 우선 매칭하도록 확장 가능)
 */
public record ScoreRequest(
        // --- 자격 ---
        Long qualificationId,                 // 선택: ID 우선 매칭
        String qualificationLabel,            // 선택: 라벨 매칭 ("기사이상", "정보처리기사" 등)

        // --- 전공 ---
        String majorTrack,                    // 필수: "전공" | "비전공"
        Long majorId,                         // 선택: ID
        String majorLevel,                    // 선택: 라벨 ("4년제졸업", "컴퓨터공학" 등)

        // --- 출결(기본) ---
        Integer absences,                     // 필수: 결석 일수 (0 이상)

        // --- 출결(특례/세부) ---
        Integer lateCount,                    // 선택: 지각 횟수
        Integer earlyLeave,                   // 선택: 조퇴 횟수
        Integer resultCount,                  // 선택: 결과 횟수(수업 중 무단 이탈 등)
        Boolean noRecord,                     // 선택: 생활기록부 미제출(true면 2점 고정)
        Boolean specialAvg,                   // 선택: 검정고시/해외학력/초등이하 평균점수 적용

        // --- 가산점 ---
        List<BonusSelection> bonusSelected    // 선택: [{category,label}] (카테고리당 1개)
) {
    public record BonusSelection(
            Long categoryId,  // 선택
            String category,  // 선택: "헌혈", "봉사", "다자녀" 등
            Long detailId,    // 선택
            String label      // 선택: "3회 이상", "24시간 이상" 등
    ) { }
}
