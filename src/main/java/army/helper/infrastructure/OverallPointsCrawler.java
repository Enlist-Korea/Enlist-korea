package army.helper.infrastructure;

import army.helper.dto.overall_points.OverallPointsCrawlerResult;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverallPointsCrawler{

    private final List<RecruitmentParser> parsers;

    public OverallPointsCrawlerResult crawlPageData(String type, String url){
        try {
            RecruitmentParser parser = parsers.stream()
                    .filter( p -> p.supports(type))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("미지원 타입"));

            log.info("[{}] 크롤링 시작. 담당 파서: {}", type, parser.getClass().getSimpleName());

            Document doc = Jsoup.connect(url).get();
            return parser.parse(doc);
        } catch(IOException e){
            throw new RuntimeException("연결 실패", e);
        }
    }
}