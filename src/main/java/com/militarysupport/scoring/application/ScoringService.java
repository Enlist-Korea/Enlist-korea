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
 * 점수 계산 핵심 서비스.
 * - 조원이 만든 "크롤링 엔티티"(Crawler 패키지)의 DB 데이터를 조회해 점수를 계산한다.
 * - 서류 점수(자격/전공/출결/가산)만 합산한다(면접 제외).
 */
@Service
@RequiredArgsConstructor
public class ScoringService {

    private final RecruitmentCriteriaRepository criteriaRepo;         // 병과별 cap 헤더 조회
    private final QualificationMajorDetailRepository qmdRepo;         // 자격/전공/출결 세부 점수표
    private final BonusPointDetailRepository bonusRepo;               // 가산점 카탈로그

    /**
     * 모든 병과(recruitment_criteria 전행)에 대해 사용자 입력 기준으로 점수를 계산해 반환.
     * - 동점자 정렬 등 추가 정책은 컨트롤러/프론트에서 후처리해도 되고, 여기서 comparator로 적용해도 된다.
     */
    public List<BranchScoreResponse> scoreAllBranches(ScoreRequest req) {
        List<RecruitmentCriteriaEntity> criteriaList = criteriaRepo.findAll();
        List<BranchScoreResponse> out = new ArrayList<>(criteriaList.size());

        // 가산점: 동일 category 1개만 인정(입력 기준 우선)
        Map<String, String> chosenByCategory = req.bonusSelected() == null ? Map.of()
                : req.bonusSelected().stream()
                  .collect(Collectors.toMap(ScoreRequest.BonusSelection::category,
                                            ScoreRequest.BonusSelection::label,
                                            (a,b)->a));

        for (RecruitmentCriteriaEntity c : criteriaList) {
            Long criteriaId = c.getId();

            // ① 자격 점수: "자격" + itemLevel(=qualificationLabel) 정확 매칭
            double qual = qmdRepo.findPointsExact(criteriaId, "자격", req.qualificationLabel())
                                 .map(Double::valueOf).orElse(0.0);
            qual = Math.min(qual, nvl(c.getQualificationScore(), 50)); // cap 적용

            // ② 전공 점수: 우선 "전공:레벨"로 매칭, 없으면 "레벨"만으로 fallback
            String key = req.majorTrack() + ":" + req.majorLevel();
            double major = qmdRepo.findPointsExact(criteriaId, "전공", key)
                                  .or(() -> qmdRepo.findPointsExact(criteriaId, "전공", req.majorLevel()))
                                  .map(Double::valueOf).orElse(0.0);
            major = Math.min(major, nvl(c.getMajorScore(), 40));       // cap 적용

            // ③ 출결 점수: "출결" 모든 밴드 불러와 absences에 매칭되는 최고점 선택
            double attendance = resolveAttendanceScore(criteriaId, req.absences());
            attendance = Math.min(attendance, nvl(c.getAttendanceScore(), 5)); // cap 적용

            // ④ 가산 점수: category → label 정확히 일치하는 항목만 합산 (category당 1개)
            double bonus = resolveBonusScore(criteriaId, chosenByCategory);
            bonus = Math.min(bonus, nvl(c.getBonusMaxScore(), 10));     // cap 적용

            double total = qual + major + attendance + bonus;

            out.add(new BranchScoreResponse(
                    String.valueOf(criteriaId),
                    c.getMilitaryService(),
                    round1(qual), round1(major), round1(attendance), round1(bonus),
                    round1(total),
                    true // 컷라인 정책이 도입되면 여기에 비교 로직 추가 예정
            ));
        }

        // 문서점수 내림차순 정렬(필요 시 세부 정렬 규칙 확장 가능)
        out.sort(Comparator.comparing(BranchScoreResponse::totalDocumentScore).reversed());
        return out;
    }

    /** 출결 점수 계산: "출결" 세부 라벨(0일,1-4일,9일+)을 밴드로 파싱해 absences와 매칭 */
    private double resolveAttendanceScore(Long criteriaId, int absences) {
        var rows = qmdRepo.findAllByType(criteriaId, "출결");
        if (rows.isEmpty()) return 0.0;

        double best = 0.0;
        for (QualificationMajorDetailEntity r : rows) {
            AttendanceBand band = AttendanceParser.parse(r.getItemLevel());
            if (band.matches(absences)) {
                best = Math.max(best, r.getScore());
            }
        }
        return best;
    }

    /** 가산 점수 계산: chosen(category→label)과 정확히 일치하는 항목만 합산 */
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
        return bestByCat.values().stream().mapToDouble(Double::doubleValue).sum();
    }

    private static double nvl(Integer v, int def) { return v == null ? def : v; }
    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }
}
