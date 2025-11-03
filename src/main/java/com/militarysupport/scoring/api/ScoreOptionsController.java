package com.militarysupport.scoring.api;

import com.militarysupport.scoring.infrastructure.persistence.QualificationMajorDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 🎯 자격증 / 전공 선택 옵션 제공 API
 * 
 * 프론트엔드에서 셀렉박스로 렌더링할 수 있도록
 * DB에 저장된 distinct 항목들을 제공한다.
 * 
 * - /api/score/options
 *   -> { "qualifications": [...], "majors": [...] }
 */
@RestController
@RequestMapping("/api/score")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000") // 프론트 개발 서버 주소에 맞게 조정
public class ScoreOptionsController {

    private final QualificationMajorDetailRepository qmdRepo;

    /**
     * ✅ 전체 자격 / 전공 옵션 조회
     * 예시 응답:
     * {
     *   "qualifications": ["기사이상", "산업기사", "정보처리기사"],
     *   "majors": ["컴퓨터공학", "기계공학", "전기전자공학"]
     * }
     */
    @GetMapping("/options")
    public Map<String, List<String>> getOptions() {
        List<String> qualifications = qmdRepo.findDistinctItemLevelByType("자격");
        List<String> majors = qmdRepo.findDistinctItemLevelByType("전공");

        return Map.of(
            "qualifications", qualifications,
            "majors", majors
        );
    }

    /**
     * ✅ 특정 키워드 검색 (자동완성용)
     * 예: /api/score/options/search?query=정보
     * -> ["정보처리기사", "정보보호기사"]
     */
    @GetMapping("/options/search")
    public List<String> searchOptions(@RequestParam String query) {
        return qmdRepo.searchItemLevelContaining(query);
    }
}
