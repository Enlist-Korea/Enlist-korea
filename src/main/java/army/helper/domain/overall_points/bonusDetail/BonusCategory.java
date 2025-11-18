package army.helper.domain.overall_points.bonusDetail;

import lombok.Getter;

@Getter
public enum BonusCategory {
    SPECIALTY_EXPERIENCE("모집특기 경력"),
    RECOMMEND_MILITARY("군 추천특기 지원자"),
    CHILDREN_OF_NATIONAL("국가유공자 자녀"),
    MULTIPLE_CHILDREN("다자녀 가정 자녀"),
    BENEFICIARY("수급권자"),
    BLOOD_DONATION("헌혈"),
    VOLUNTEER("사회봉사활동"),
    ELIGIBLE_ACTIVE_DUTY("현역병입영대상 판정자"),
    IMMIGRANTS_ACTIVE_DUTY("국외이주자 중 현역병복무지원자"),
    DRIVING_APTITUDE_TEST("군운전적성정밀검사 합격자");

    private final String displayText;

    BonusCategory(String displayText) {
        this.displayText = displayText;
    }

    /**
     * 파싱한 텍스트를 기반으로 적절한 BonusCategory Enum을 찾습니다.
     * @param text (예: "국가유공자 자녀", "헌혈")
     * @return 매칭되는 Enum, 없으면 null
     */
    public static BonusCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        String trimmedText = text.trim();

        for (BonusCategory category : BonusCategory.values()) {
            // Enum에 저장된 displayText와 일치하는지 확인
            // (주의: contains가 아닌 equals로 정확히 일치하는지 확인)
            if (category.displayText.equals(trimmedText)) {
                return category;
            }
        }

        // 일치하는 Enum이 없으면 null 반환
        return null;
    }
}