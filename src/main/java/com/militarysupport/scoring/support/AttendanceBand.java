package com.militarysupport.scoring.support;

/**
 * 출결 구간 밴드 표현: [min, maxInclusive]
 * - 예: "1-4일" → min=1, max=4
 * - 예: "9일+" → min=9, max=∞
 */
public record AttendanceBand(int min, int maxInclusive) {
    public boolean matches(int absences) {
        return absences >= min && absences <= maxInclusive;
    }
}
