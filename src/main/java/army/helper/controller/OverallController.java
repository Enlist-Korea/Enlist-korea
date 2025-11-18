package army.helper.controller;


import army.helper.dto.overall_points.ScoreQueryDto;
import army.helper.service.overall_points.ScoreQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Slf4j
public class OverallController {
    private final ScoreQueryService scoreQueryService;

    @PostMapping("/scores")
    public ResponseEntity<Integer> getOverallPoints(@RequestBody ScoreQueryDto queryDto){
        int score = scoreQueryService.getScore(queryDto);
        return ResponseEntity.ok(score);
    }
}