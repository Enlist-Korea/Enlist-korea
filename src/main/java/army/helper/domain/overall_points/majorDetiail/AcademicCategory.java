package army.helper.domain.overall_points.majorDetiail;

import lombok.Getter;

@Getter
public enum AcademicCategory {
    // 1. 각 Enum 상수에 매핑되는 텍스트를 생성자로 전달합니다.
    UNIVERSITY("대학교"),
    HIGH_SCHOOL("고졸"),
    KP_SCHOOL("한국폴리텍대학인력개발원"),
    JUNIOR_COLLEGE_2_YEAR("전문대(2년)"),
    JUNIOR_COLLEGE_3_YEAR("전문대(3년)"),
    CREDIT_BANK("학점은행제");

    // 2. 텍스트를 저장할 필드
    private final String displayText;

    // 3. 텍스트를 받는 생성자
    AcademicCategory(String displayText) {
        this.displayText = displayText;
    }


    public static AcademicCategory fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }

        // ⭐️ [수정] 입력값의 공백을 모두 제거
        String normalizedText = text.replace(" ", "").trim();

        for (AcademicCategory category : AcademicCategory.values()) {
            // Enum의 텍스트도 공백을 모두 제거하고 비교
            String normalizedEnumText = category.getDisplayText().replace(" ", "").trim();

            if (normalizedEnumText.equals(normalizedText)) {
                return category;
            }
        }
        return null;
    }
}