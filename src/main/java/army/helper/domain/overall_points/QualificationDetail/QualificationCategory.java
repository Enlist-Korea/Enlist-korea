package army.helper.domain.overall_points.QualificationDetail;

import lombok.Getter;

@Getter
public enum QualificationCategory {
    NATIONAL_TECHNICAL("국가기술자격증"),
    WORK_STUDY("일학습병행자격증"),
    GENERAL("일반자격증"),
    DRIVERS("운전면허증"),
    NONE("자격증 미소지");

    // 2. 텍스트를 저장할 필드
    private final String displayText;

    // 3. 텍스트를 받는 생성자
    QualificationCategory(String displayText) {
        this.displayText = displayText;
    }

    /**
     * 4. fromString 메서드 구현
     * 테이블에서 파싱한 텍스트를 기반으로 적절한 Enum을 찾습니다.
     * @param firstCellText (예: "국가기술자격증", "자격증 미소지")
     * @return 매칭되는 Enum, 없으면 null
     */
    public static QualificationCategory fromString(String firstCellText) {
        if (firstCellText == null || firstCellText.trim().isEmpty()) {
            return null;
        }

        String trimmedText = firstCellText.trim();

        // 모든 Enum 값을 순회하며 일치하는 텍스트를 찾습니다.
        for (QualificationCategory category : QualificationCategory.values()) {
            if (category.displayText.equals(trimmedText)) {
                return category;
            }
        }

        // "기사이상", "산업기사" 등 일치하는 메인 카테고리가 없으면 null 반환
        return null;
    }
}