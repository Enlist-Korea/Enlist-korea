package com.militarysupport.scoring.interfaces.dto;

import java.util.List;

/**
 * 사용자 입력 DTO.
 * - 프론트에서 고른 라벨/값을 받아서 점수표(DB)와 매칭한다.
 * - bonusSelected: 가산점은 '카테고리별 1개'만 인정 → 서버에서 카테고리-라벨 정확 매칭
 */
public record ScoreRequest(
        String qualificationLabel,          // 예: "기사이상" (자격/면허 라벨)
        String majorTrack,                  // 예: "전공" | "비전공" (전공 트랙)
        String majorLevel,                  // 예: "4년제졸업" (전공 레벨)
        int absences,                       // 예: 2 (출결: 결석일수)
        List<BonusSelection> bonusSelected  // 가산점 선택 목록 {category, label}
) {
    /**
     * 가산점 선택 1건.
     * category: "경력","헌혈" 등
     * label   : "관련 경력 1년 이상" 등
     */
    public record BonusSelection(String category, String label) {}
}
