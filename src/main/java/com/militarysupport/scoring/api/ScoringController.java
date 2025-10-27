package com.militarysupport.scoring.api;

import com.militarysupport.scoring.interfaces.dto.BranchScoreResponse;
import com.militarysupport.scoring.interfaces.dto.ScoreRequest;
import com.militarysupport.scoring.application.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 점수 계산 API 엔드포인트.
 * - POST /api/score/branches
 * - 요청: ScoreRequest (프론트에서 선택한 라벨/값)
 * - 응답: 병과별 서류점수 합산 결과(내림차순)
 */
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
public class ScoringController {

    private final ScoringService scoringService;

    @PostMapping("/branches")
    public List<BranchScoreResponse> calculate(@RequestBody ScoreRequest request) {
        return scoringService.scoreAllBranches(request);
    }
}
