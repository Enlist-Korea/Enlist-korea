package army.helper.dto.overall_points;

import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

/**
 * [요청 DTO] 1차 평가 요소 입력(자격/전공/출결/가산점)
 * - vehicleDrivingSpecialty: 차량운전 분야 특기 여부(true면 전공=0점, 운전면허증만 자격으로 인정)
 * - qualifications: 복수로 받아오지만 규정상 "최고점 1개만" 반영됨(서비스에서 처리)
 * - major: ACADEMIC / CREDIT_BANK / KP 중 택1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OverallPointsRequest {

    /** 차량운전 분야 특기 여부 (수송운용/견인/차륜형/…/크레인차량 운전 등) */
    @Builder.Default
    private boolean vehicleDrivingSpecialty = false;

    /** 기술자격·면허 선택 목록(여러 개 가능) — 서비스에서 최고점 1개만 반영 */
    @Singular
    private List<QualificationSelection> qualifications;

    /** 전공학과 선택 (학적/학점은행제/KP 중 택1) */
    @NotNull
    private MajorSelection major;

    /** 출결 구간 ("0일","1~4일","5~8일","9일 이상") */
    @NotBlank
    private String attendanceCountBucket;

    /** 가산점 총합(서버에서 10점 상한) */
    @Min(0)
    @Max(100)
    private Integer bonusTotal;

    // ---------- Nested Types ----------

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class QualificationSelection {
        /** 자격군(예: 국가기술자격증/일학습병행자격증/일반자격증/운전면허증/자격증 미소지) */
        @NotBlank private String qualifications;

        /** 등급(예: 기사이상/L6/공인/대형/1종보통(수동) 등) */
        @NotBlank private String qualificationLevel;

        /** 직접(true)/간접(false) — 운전면허증에는 무시됨 */
        private boolean related;
    }

    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MajorSelection {
        /** 전공 선택 타입 */
        @NotNull private MajorType type;

        private AcademicPayload academic;
        private CreditBankPayload creditBank;
        private KPPayload kp;

        public enum MajorType { ACADEMIC, CREDIT_BANK, KP }
    }

    /** 전공(학적) 페이로드 */
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AcademicPayload {
        @NotBlank private String educationCategory; // "대학교","전문대(3년)","전문대(2년)","고졸"
        @NotNull  private Integer grade;            // 1,2,3,4
        @NotBlank private String status;            // "재학","수료"
        @NotNull  private Boolean major;            // 전공(true)/비전공(false)
    }

    /** 전공(학점은행제) 페이로드 */
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CreditBankPayload {
        @NotBlank private String degreeStatus; // "학사","전문학사(3년)","전문학사(2년)"
        @NotBlank private String credits;      // "40학점 이상","80학점 이상" 등 테이블 기준 문자열
    }

    /** 전공(KP: 폴리텍/인력개발원) 페이로드 */
    @Data
    @NoArgsConstructor @AllArgsConstructor @Builder
    public static class KPPayload {
        @NotNull private KPLevel level; // 2년/1년/6개월 이상 수료
        public enum KPLevel { TWO_YEAR_COMPLETE, ONE_YEAR_COMPLETE, SIX_MONTH_COMPLETE }
    }
}
