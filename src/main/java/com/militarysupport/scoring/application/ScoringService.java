package com.militarysupport.scoring.application;

import com.militarysupport.recruit_helper.domain.Crawler.BonusPointDetailEntity;
import com.militarysupport.recruit_helper.domain.Crawler.QualificationMajorDetailEntity;
import com.militarysupport.recruit_helper.domain.Crawler.RecruitmentCriteriaEntity;
import com.militarysupport.scoring.interfaces.dto.BranchScoreResponse;
import com.militarysupport.scoring.interfaces.dto.ScoreRequest;
import com.militarysupport.scoring.infrastructure.persistence.BonusPointDetailRepository;
import com.militarysupport.scoring.infrastructure.persistence.QualificationMajorDetailRepository;
import com.militarysupport.scoring.infrastructure.persistence.RecruitmentCriteriaRepository;
import com.militarysupport.scoring.support.AttendanceBand;
import com.militarysupport.scoring.support.AttendanceParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 점수 계산 핵심 서비스 (면접 제외, 서류 점수만).
 * - DB(크롤링 엔티티)에서 병과별 cap/세부 배점표 조회
 * - 자격/전공/출결/가산 합산
 * - 기술행정병 규정 보강:
 *   1) 동점자 정렬 규칙: 자격▶전공▶출결▶가산 (생년월일은 데이터 미보유로 보류)
 *   2) 가산점 그룹 캡: 헌혈+봉사 통합 최대 3점
 *   3) 출결 특례: 생활기록부 미제출=2점, 검정/해외/초등=평균점수 플래그, 지각/조퇴/결과 → 결석일 변환
 */
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final RecruitmentCriteriaRepository criteriaRepo;
    private final QualificationMajorDetailRepository qmdRepo;
    private final BonusPointDetailRepository bonusRepo;

    public List<BranchScoreResponse> scoreAllBranches(ScoreRequest req) {
        List<RecruitmentCriteriaEntity> criteriaList = criteriaRepo.findAll();
        List<BranchScoreResponse> out = new ArrayList<>(criteriaList.size());

        // 가산점: 동일 category 1개만 인정(입력 기준 우선)
        Map<String, String> chosenByCategory = normalizeBonusSelections(req);

        for (RecruitmentCriteriaEntity c : criteriaList) {
            Long criteriaId = c.getId();

            // ① 자격 점수
            double qual = resolveQualificationScore(criteriaId, req);
            qual = Math.min(qual, nvl(c.getQualificationScore(), 50));

            // ② 전공 점수 ("전공:레벨" → 없으면 "레벨" fallback)
            double major = resolveMajorScore(criteriaId, req);
            major = Math.min(major, nvl(c.getMajorScore(), 40));

            // ③ 출결 점수 (특례/변환 적용)
            double attendance = resolveAttendanceScore(criteriaId, req);
            attendance = Math.min(attendance, nvl(c.getAttendanceScore(), 5));

            // ④ 가산 점수 (카테고리 1개 인정 + 그룹 캡: 헌혈+봉사 ≤ 3)
            double bonus = resolveBonusScore(criteriaId, chosenByCategory);
            bonus = Math.min(bonus, nvl(c.getBonusMaxScore(), 10));

            double total = qual + major + attendance + bonus;

            out.add(new BranchScoreResponse(
                    String.valueOf(criteriaId),
                    c.getMilitaryService(),
                    round1(qual), round1(major), round1(attendance), round1(bonus),
                    round1(total),
                    true
            ));
        }

        // 🔹 동점자 정렬: 총점 ▶ 자격 ▶ 전공 ▶ 출결 ▶ 가산
        out.sort((a, b) -> {
            int byTotal = Double.compare(b.totalDocumentScore(), a.totalDocumentScore());
            if (byTotal != 0) return byTotal;
            int byQual = Double.compare(b.qualScore(), a.qualScore());
            if (byQual != 0) return byQual;
            int byMajor = Double.compare(b.majorScore(), a.majorScore());
            if (byMajor != 0) return byMajor;
            int byAtt = Double.compare(b.attendanceScore(), a.attendanceScore());
            if (byAtt != 0) return byAtt;
            int byBonus = Double.compare(b.bonusScore(), a.bonusScore());
            if (byBonus != 0) return byBonus;
            return 0; // 생년월일은 데이터 미보유로 보류
        });

        return out;
    }

    // --------------------------
    // 자격 점수
    // --------------------------
    private double resolveQualificationScore(Long criteriaId, ScoreRequest req) {
        // ID 기반을 도입했으면, ID로 먼저 매칭하는 쿼리/테이블이 있어야 함.
        // (현재는 라벨 매칭만 구현되어 있으므로 label 우선)
        String label = req.qualificationLabel();
        if (label == null || label.isBlank()) return 0.0;

        return qmdRepo.findPointsExact(criteriaId, "자격", label)
                .map(Double::valueOf).orElse(0.0);
    }

    // --------------------------
    // 전공 점수 (전공:레벨 → 레벨 fallback)
    // --------------------------
    private double resolveMajorScore(Long criteriaId, ScoreRequest req) {
        String track = req.majorTrack();
        String level = req.majorLevel();

        if (track == null || level == null || track.isBlank() || level.isBlank()) {
            return 0.0;
        }
        String key = track + ":" + level;

        return qmdRepo.findPointsExact(criteriaId, "전공", key)
                .or(() -> qmdRepo.findPointsExact(criteriaId, "전공", level))
                .map(Double::valueOf).orElse(0.0);
    }

    // --------------------------
    // 출결 점수 (특례/변환)
    // --------------------------
    /**
     * 규칙:
     * - 생활기록부 미제출: 2점 고정
     * - 특례(검정고시/해외학력/초등 이하): 평균점수 적용 → 정책값(임시 3점) 사용 또는 테이블에서 별도 조회
     * - 결과 2회 = 지각 1회
     * - 지각/조퇴 2회 = 결석 1일
     * - 최종 결석일수로 밴드 매칭(0일=5, 1~4=4, 5~8=3, 9+=2)
     */
    private double resolveAttendanceScore(Long criteriaId, ScoreRequest req) {
        // 0) 미제출: 2점 고정
        if (Boolean.TRUE.equals(req.noRecord())) {
            return fixedAttendanceScore(criteriaId, 2.0);
        }

        // 1) 특례: 평균점수 적용
        if (Boolean.TRUE.equals(req.specialAvg())) {
            // 정책값: 3.0 (또는 병과별 평균을 별도 테이블에서 조회하도록 확장 가능)
            return fixedAttendanceScore(criteriaId, 3.0);
        }

        // 2) 지각/조퇴/결과 → 결석일로 변환
        int absences = nvl(req.absences(), 0);
        int late = nvl(req.lateCount(), 0);
        int early = nvl(req.earlyLeave(), 0);
        int result = nvl(req.resultCount(), 0);

        // 결과 2회 = 지각 1회
        int lateFromResult = result / 2;
        int lateTotal = late + early + lateFromResult;

        // 지각/조퇴 2회 = 결석 1일
        int extraAbs = lateTotal / 2;
        int finalAbsences = absences + extraAbs;

        // 3) 밴드 매칭
        List<QualificationMajorDetailEntity> rows = qmdRepo.findAllByType(criteriaId, "출결");
        if (rows.isEmpty()) return 0.0;

        double best = 0.0;
        for (QualificationMajorDetailEntity r : rows) {
            AttendanceBand band = AttendanceParser.parse(r.getItemLevel());
            if (band.matches(finalAbsences)) {
                best = Math.max(best, r.getScore());
            }
        }
        return best;
    }

    /**
     * 출결 점수를 "고정 점수"로 처리해야 할 때(미제출/평균점수 특례)
     * - DB의 cap(최대 5점)을 초과하지 않도록 처리
     */
    private double fixedAttendanceScore(Long criteriaId, double fixed) {
        List<QualificationMajorDetailEntity> rows = qmdRepo.findAllByType(criteriaId, "출결");
        if (rows.isEmpty()) return fixed; // cap은 상위에서 다시 한 번 걸림
        // 밴드표가 있더라도 고정점수는 그대로 반환 (상위에서 cap 적용)
        return fixed;
    }

    // --------------------------
    // 가산 점수 (카테고리 1개 + 그룹 캡)
    // --------------------------
    /**
     * 규칙:
     * - category당 1개만 인정(선택된 label과 정확히 일치)
     * - 합산 후, "헌혈 + 봉사 ≤ 3점" 그룹 캡 적용
     */
    private double resolveBonusScore(Long criteriaId, Map<String, String> chosenByCategory) {
        if (chosenByCategory.isEmpty()) return 0.0;

        var catalog = bonusRepo.findByCriteriaAndCategories(criteriaId, chosenByCategory.keySet());
        Map<String, Double> bestByCat = new HashMap<>();

        for (BonusPointDetailEntity row : catalog) {
            String want = chosenByCategory.get(row.getCategory());
            if (want != null && want.equals(row.getDetail())) {
                bestByCat.merge(row.getCategory(), row.getScore().doubleValue(), Math::max);
            }
        }

        // ① 기본 합산
        double sum = bestByCat.values().stream().mapToDouble(Double::doubleValue).sum();

        // ② 그룹 캡: "헌혈"+"봉사" 합산 최대 3점
        double blood = pick(bestByCat, "헌혈");
        double volunteer = pick(bestByCat, "봉사");
        double hv = blood + volunteer;
        if (hv > 3.0) {
            sum -= (hv - 3.0);
        }

        return sum;
    }

    // --------------------------
    // 유틸리티
    // --------------------------
    private Map<String, String> normalizeBonusSelections(ScoreRequest req) {
        if (req.bonusSelected() == null || req.bonusSelected().isEmpty()) return Map.of();
        // 현재는 라벨 기반 매칭. 추후 ID 기반 매칭을 도입할 경우, 여기서 ID→라벨 매핑 수행 가능.
        return req.bonusSelected().stream()
                .filter(it -> it != null && it.category() != null && it.label() != null)
                .collect(Collectors.toMap(
                        ScoreRequest.BonusSelection::category,
                        ScoreRequest.BonusSelection::label,
                        (a, b) -> a
                ));
    }

    private static double pick(Map<String, Double> map, String key) {
        // 카테고리명이 데이터에서 조금 다를 수 있으면 startsWith/contains로 완화 가능
        return map.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getKey().equals(key))
                .mapToDouble(Map.Entry::getValue)
                .sum();
    }

    private static int nvl(Integer v, int d) { return v == null ? d : v; }
    private static double nvl(Integer v, double d) { return v == null ? d : v; }
    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}
