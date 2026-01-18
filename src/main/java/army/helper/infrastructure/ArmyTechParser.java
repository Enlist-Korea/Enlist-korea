package army.helper.infrastructure;

import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import army.helper.dto.overall_points.OverallPointsCrawlerResult; // DTO 위치에 맞게 import 확인 필요
import army.helper.dto.overall_points.detail.BonusListResponse;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j; // 3. 로그 사용을 위해 추가
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j // 3. 로그 어노테이션 추가
@Component
public class ArmyTechParser extends AbstractRecruitmentParser {

    @Override
    public boolean supports(String type) {
        return "육군 기술행정병".equals(type);
    }

    @Override
    public OverallPointsCrawlerResult parse(Document doc) {
        OverallPointsCrawlerResult result = new OverallPointsCrawlerResult();

        // 1. HTML 파싱 로직 시작 (try-catch 제거됨)
        Element contentsDiv = doc.selectFirst("#contents");
        if (contentsDiv == null) {
            log.error("HTML 구조 변경됨: #contents 를 찾을 수 없습니다.");
            return result;
        }

        String currentHeading = "";
        Elements allElements = contentsDiv.select("h3, div.layout_h3, h4, table, ul");

        log.info("발견된 주요 요소 수 : {}", allElements.size());

        for (Element element : allElements) {
            String tagName = element.tagName();
            String className = element.className();
            String text = element.text().trim();

            // A. 제목(헤더) 업데이트
            if (tagName.matches("h[3-4]") || className.contains("layout_h3")) {
                currentHeading = text;
                log.debug("📍 제목 발견: {}", currentHeading);
                continue;
            }

            // B. 테이블 처리
            if (tagName.equals("table")) {
                String caption = element.selectFirst("caption") != null ? element.selectFirst("caption").text() : "";
                String context = (currentHeading + " " + caption).trim();

                log.info("📊 테이블 파싱 시도. Context: '{}'", context);

                // 2. 메서드 호출 시 불필요한 'type' 인자 제거 (부모 클래스와 맞춤)
                if (context.contains("출결") || context.contains("결석")) {
                    result.getAttendanceList().addAll(parseAttendanceList(element));
                } else if (context.contains("전공") || context.contains("학력")) {
                    if (context.contains("학점") || context.contains("은행제")) {
                        log.info(">> 학점은행제 테이블 파싱");
                        result.getAcademicList().addAll(parseCreditBankList(element));
                    } else {
                        log.info(">> 일반 학력 테이블 파싱");
                        result.getAcademicList().addAll(parseMajorTableSmart(element));
                    }
                } else if (context.contains("자격") || context.contains("면허")) {
                    result.getQualificationList().addAll(parseQualificationList(element));
                } else if (context.contains("가산점")) {
                    log.info("📊 가산점(헌혈/봉사) 테이블 파싱");
                    List<BonusScoreRule> rules = parseBloodDonationTable(element);
                    result.getBonusList().addAll(rules.stream().map(BonusListResponse::new).collect(Collectors.toList()));
                }
            }

            // C. 리스트 처리 (가산점)
            else if (tagName.equals("ul")){
                if (className.contains("menu") || className.contains("tab")) continue;
                if (currentHeading.contains("가산점")) {
                    log.info("📝 가산점 리스트 파싱 시도. Context: '{}'", currentHeading);
                    result.getBonusList().addAll(parseBonusPointList(element));
                }
            }
        }

        log.info("✅ 크롤링 완료: 출결={}개, 전공={}개, 자격={}개, 가산점={}개",
                result.getAttendanceList().size(),
                result.getAcademicList().size(),
                result.getQualificationList().size(),
                result.getBonusList().size());

        return result;
    }
    // 불필요한 catch 블록 제거됨
}