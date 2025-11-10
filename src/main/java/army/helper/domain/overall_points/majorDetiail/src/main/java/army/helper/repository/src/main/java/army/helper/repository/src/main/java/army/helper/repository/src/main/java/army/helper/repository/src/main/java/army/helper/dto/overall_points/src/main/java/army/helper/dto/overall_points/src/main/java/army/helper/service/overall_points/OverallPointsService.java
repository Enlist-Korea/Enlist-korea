package army.helper.service.overall_points;

import army.helper.dto.overall_points.*;
import army.helper.repository.*;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 1차 평가 점수 계산 서비스
 * - 기술자격·면허: 복수 제출 시 '최고점 1개'만 반영(합산 금지), 상한 50점
 *   * 운전면허증: '차량운전 분야 특기'에 한해 인정, 미인정 등급(1종보통(자동)/2종 등) 제외
 *   * 인정 가능한 항목이 전혀 없으면 '자격증 미소지 20점'
 * - 전공학과: 학적/학점은행제/KP 중 택1, 상한 40점
 *   * 차량운전 분야 특기라면 전공 점수 0점(규정)
 *   * KP 점수: 2년 32 / 1년 30 / 6개월 26
 * - 출결상황: 테이블 조회, 상한 5점
 * - 가산점: 입력값을 0~10으로 캡
 * - total = 위 항목 합
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OverallPointsService {

    private final QualificationDetailRepository qualificationDetailRepository;
    private final AttendanceDetailRepository attendanceDetailRepository;
    private final AcademicStatusScoreRepository academicStatusScoreRepository;
    private final CreditBankScoreRepository creditBankScoreRepository;

    /** 퍼블릭 API: 요청 전체를 받아 점수 계산 */
    @Transactional(Transactional.TxType.SUPPORTS)
    public OverallPoints calculate(OverallPointsRequest req) {
        int qScore = calcQualificationScore(req.getQualifications(), req.isVehicleDrivingSpecialty());
        int mScore = calcMajorScore(req.getMajor(), req.isVehicleDrivingSpecialty());
        int aScore = calcAttendanceScore(req.getAttendanceCountBucket());
        int bScore = normalizeBonus(req.getBonusTotal());

        int total = qScore + mScore + aScore + bScore;

        return OverallPoints.builder()
                .qualificationScore(qScore)
                .majorScore(mScore)
                .attendanceScore(aScore)
                .bonusScore(bScore)
                .total(total)
                .build();
    }

    // -------------------- 자격증 --------------------

    /**
     * 기술자격·면허 점수 계산
     * - 규정: 여러 개 제출 시 '최고 점수 1개만' 부여 (합산 금지)
     * - 운전면허증: '차량운전 분야 특기'인 경우에만 인정
     *              미인정 등급(예: 1종보통(자동), 2종 계열)은 제외
     * - 그 외 자격증은 '직접/간접'에 따라 각 테이블 점수 조회
     * - 아무 것도 인정되지 않으면 '자격증 미소지 20점'
     * - 최종 상한 50점
     */
    int calcQualificationScore(List<OverallPointsRequest.QualificationSelection> list,
                               boolean vehicleDrivingSpecialty) {
        if (list == null || list.isEmpty()) {
            return 20; // 자격증 미소지
        }

        int best = 0;

        for (OverallPointsRequest.QualificationSelection sel : list) {
            if (sel == null) continue;

            String q = safe(sel.getQualifications());
            String level = safe(sel.getQualificationLevel());

            // 1) 운전면허증 특례 처리
            if ("운전면허증".equals(q)) {
                // 차량운전 분야 특기가 아니면 인정되지 않음
                if (!vehicleDrivingSpecialty) continue;

                // 미인정 등급 필터링: "1종보통(자동)" 또는 "2종"으로 시작하는 등
                if (level.contains("자동") || level.startsWith("2종")) continue;

                // 운전면허증은 directScore만 사용(테이블 값 필요)
                int score = qualificationDetailRepository
                        .findDirectScore(q, level)
                        .orElse(0);
                best = Math.max(best, score);
                continue;
            }

            // 2) 일반 자격증(국가기술/일학습/일반자격증 등): 직접/간접에 따라 점수 선택
            int add = sel.isRelated()
                    ? qualificationDetailRepository.findDirectScore(q, level).orElse(0)
                    : qualificationDetailRepository.findIndirectScore(q, level).orElse(0);

            best = Math.max(best, add);
        }

        // 3) 어떤 항목도 인정되지 않으면 '자격증 미소지 20점'
        if (best <= 0) {
            best = 20;
        }

        // 4) 상한 50점 적용 (※ 운전면허증 테이블 값이 크더라도 1차 평가 규정 상한을 우선 적용)
        return Math.min(best, 50);
    }

    // -------------------- 전공 --------------------

    /**
     * 전공 점수 계산
     * - 차량운전 분야 특기면 0점 고정(규정)
     * - ACADEMIC: 학적 테이블 조회
     * - CREDIT_BANK: 학점은행제 테이블 조회
     * - KP: 2년=32 / 1년=30 / 6개월=26
     * - 상한 40점
     */
    int calcMajorScore(OverallPointsRequest.MajorSelection major,
                       boolean vehicleDrivingSpecialty) {
        if (major == null || major.getType() == null) return 0;

        if (vehicleDrivingSpecialty) {
            // 차량운전 특기: 전공 배점 없음
            return 0;
        }

        int score = 0;
        switch (major.getType()) {
            case ACADEMIC -> {
                OverallPointsRequest.AcademicPayload p = major.getAcademic();
                if (p != null) {
                    score = academicStatusScoreRepository
                            .findScore(p.getEducationCategory(), p.getGrade(), p.getStatus(), p.getMajor())
                            .orElse(0);
                }
            }
            case CREDIT_BANK -> {
                OverallPointsRequest.CreditBankPayload p = major.getCreditBank();
                if (p != null) {
                    score = creditBankScoreRepository
                            .findScore(p.getDegreeStatus(), p.getCredits())
                            .orElse(0);
                }
            }
            case KP -> {
                OverallPointsRequest.KPPayload p = major.getKp();
                if (p != null && p.getLevel() != null) {
                    // 공식 배점 반영
                    score = switch (p.getLevel()) {
                        case TWO_YEAR_COMPLETE -> 32;
                        case ONE_YEAR_COMPLETE -> 30;
                        case SIX_MONTH_COMPLETE -> 26;
                    };
                }
            }
        }
        return Math.min(score, 40);
    }

    // -------------------- 출결 --------------------

    /** 출결 점수: 구간 문자열로 점수 테이블 조회, 상한 5점 */
    int calcAttendanceScore(String bucket) {
        if (bucket == null || bucket.isBlank()) return 0;
        int s = attendanceDetailRepository.findScoreByBucket(bucket).orElse(0);
        return Math.min(s, 5);
    }

    // -------------------- 가산점 --------------------

    /** 가산점: 음수 방지 + 10점 상한 */
    int normalizeBonus(Integer bonus) {
        int b = bonus == null ? 0 : Math.max(0, bonus);
        return Math.min(b, 10);
    }

    // -------------------- 공통 유틸 --------------------

    private static String safe(String s) {
        return s == null ? "" : s.trim();
    }
}
