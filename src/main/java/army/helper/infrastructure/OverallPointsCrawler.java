package army.helper.infrastructure;

import army.helper.domain.overall_points.AttendanceScoreRule;
import army.helper.domain.overall_points.QualificationDetail.QualificationCategory;
import army.helper.domain.overall_points.QualificationDetail.QualificationScoreRule;
import army.helper.domain.overall_points.bonusDetail.BonusCategory;
import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import army.helper.domain.overall_points.majorDetiail.AcademicCategory;
import army.helper.domain.overall_points.majorDetiail.AcademicScoreRule;
import army.helper.dto.overall_points.detail.AcademicListResponse;
import army.helper.dto.overall_points.detail.AttendanceListResponse;
import army.helper.dto.overall_points.detail.BonusListResponse;
import army.helper.dto.overall_points.detail.QualificationListResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OverallPointsCrawler {

    @Getter
    public static class OverallPointsCrawlerResult {
        private List<AttendanceListResponse> attendanceList = new ArrayList<>();
        private List<QualificationListResponse> qualificationList = new ArrayList<>();
        private List<AcademicListResponse> academicList = new ArrayList<>();
        private List<BonusListResponse> bonusList = new ArrayList<>();
    }

    @Getter
    @AllArgsConstructor
    private static class HeaderInfo {
        String major; // 예: 4학년
        String sub;   // 예: 재학
    }


    public OverallPointsCrawlerResult crawlPageData(String type, String url) {
        OverallPointsCrawlerResult result = new OverallPointsCrawlerResult();
        try {
            log.info("[{}] 페이지 크롤링 시작 : {}", type, url);
            Document doc = Jsoup.connect(url).get();

            // 1. #contents 영역을 가져옵니다.
            Element contentsDiv = doc.selectFirst("#contents");
            if (contentsDiv == null) {
                log.error("HTML 구조 변경됨: #contents 를 찾을 수 없습니다.");
                return result;
            }

            // 2. 상태 변수 (현재 읽고 있는 제목)
            String currentHeading = "";

            // 3. #contents의 모든 자식 요소를 순서대로 훑습니다.
            // (직계 자식이 아니라 모든 하위 요소를 순회하면 너무 많으므로, 주요 컨테이너만 봅니다)
            // 하지만 병무청 사이트 구조상 h3와 table이 형제가 아닐 수 있으므로,
            // 전체를 flatten해서 보는 것이 안전합니다.

            // ⭐️ [핵심] 문서 전체에서 h3, h4, table, ul.list_box 만 순서대로 추출
            Elements allElements = contentsDiv.select("h3, div.layout_h3, h4, table, ul");

            log.info("발견된 주요 요소 수 : {}", allElements.size());

            for (Element element : allElements) {
                String tagName = element.tagName();
                String className = element.className();
                String text = element.text().trim();

                // A. 제목(헤더)을 만났을 때 -> 상태 업데이트
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

                    if (context.contains("출결") || context.contains("결석")) {
                        result.getAttendanceList().addAll(parseAttendanceList(element, type));
                    } else if (context.contains("전공") || context.contains("학력")) {
                        if (context.contains("학점") || context.contains("은행제")) {
                            log.info(">> 학점은행제 테이블 파싱");
                            result.getAcademicList().addAll(parseCreditBankList(element));
                        } else {
                            log.info(">> 일반 학력 테이블 파싱");
                            result.getAcademicList().addAll(parseMajorTableSmart(element, type));
                        }
                    } else if (context.contains("자격") || context.contains("면허")) {
                        result.getQualificationList().addAll(parseQualificationList(element, type));
                    } else if (context.contains("가산점")) {
                        log.info("📊 가산점(헌혈/봉사) 테이블 파싱");
                        List<BonusScoreRule> rules = parseBloodDonationTable(element);
                        result.getBonusList().addAll(rules.stream().map(BonusListResponse::new).collect(Collectors.toList()));
                    }
                }

                // C. 리스트 처리 (가산점)
                else if (tagName.equals("ul")){
                    // 리스트는 별도 캡션이 없으므로 헤딩만으로 판단
                    if (className.contains("menu") || className.contains("tab")) continue;
                    if (currentHeading.contains("가산점")) {
                        log.info("📝 가산점 리스트 파싱 시도. Context: '{}'", currentHeading);
                        result.getBonusList().addAll(parseBonusPointList(element, type));
                    }
                }
            }
            log.info("✅ 크롤링 완료: 출결={}개, 전공={}개, 자격={}개, 가산점={}개",
                    result.getAttendanceList().size(),
                    result.getAcademicList().size(),
                    result.getQualificationList().size(),
                    result.getBonusList().size());
            // (로그 및 리턴 동일) ...

        } catch (IOException e) {
            throw new RuntimeException("크롤링 실패:" + url, e);
        }
        return result;
    }

    /**
     * 부모 요소까지 확인하여 'layout_h3' 클래스나 'h3' 태그를 찾습니다.
     */
    private String findContextTitle(Element element) {
        // 1. 내 바로 위 형제 확인
        String directPrev = getPrecedingHeadingText(element);
        if (!directPrev.isEmpty()) return directPrev;

        // 2. 내 부모의 바로 위 형제 확인 (div.table_wrap 등으로 감싸진 경우)
        if (element.parent() != null) {
            String parentPrev = getPrecedingHeadingText(element.parent());
            if (!parentPrev.isEmpty()) return parentPrev;

            // 3. 부모의 부모까지 확인 (안전장치)
            if (element.parent().parent() != null) {
                return getPrecedingHeadingText(element.parent().parent());
            }
        }
        return "";
    }

    private String getPrecedingHeadingText(Element element) {
        Element prev = element.previousElementSibling();
        while (prev != null) {
            // h1~h6 태그이거나, 병무청 사이트 특유의 클래스(layout_h3)인 경우
            if (prev.tagName().matches("h[1-6]") ||
                    prev.className().contains("layout_h3") ||
                    prev.className().contains("title")) {
                return prev.text();
            }
            prev = prev.previousElementSibling();
        }
        return "";
    }

    // --- [학력 파싱] ---

    private List<AcademicListResponse> parseMajorTableSmart(Element table, String type) {
        List<AcademicListResponse> responses = new ArrayList<>();

        // 헤더 파싱
        Map<Integer, HeaderInfo> headerMap = parseGridHeader(table.selectFirst("thead"));
        boolean isGridTable = !headerMap.isEmpty();

        log.info("parseMajorTableSmart 시작: isGridTable={}, headerMapSize={}", isGridTable, headerMap.size());

        Elements rows = table.select("tbody > tr");

        for (Element row : rows) {
            Elements cells = row.select("th, td"); // th, td 모두 가져옴
            if (cells.isEmpty()) continue;

            String firstText = cells.get(0).text(); // 첫 번째 셀 텍스트

            // 1. 대학교
            if (firstText.contains("대학교") && isGridTable) {
                responses.addAll(parseAcademicGridRow(row, AcademicCategory.UNIVERSITY, headerMap));
            }
            // 2. 고졸
            else if (firstText.contains("고졸")) {
                responses.addAll(parseHighSchoolRow(row));
            }
            // 3. 폴리텍
            else if (firstText.contains("폴리텍")) {
                responses.addAll(parseKPScoreList(row));
            }

            // Case A: "전문대" 행 (Rowspan으로 인해 첫 셀은 "전문대", 두 번째 셀이 "3년")
            else if (firstText.contains("전문대")) {
                // 셀이 2개 이상이고, 두 번째 셀에 "3년"이 있는지 확인
                if (cells.size() > 1 && cells.get(1).text().contains("3년")) {
                    log.info(">> 전문대(3년) 파싱");
                    responses.addAll(parseAcademicGridRow(row, AcademicCategory.JUNIOR_COLLEGE_3_YEAR, headerMap));
                }
            }
            // Case B: "2년" 행 (첫 셀이 바로 "2년")
            else if (firstText.contains("2년")) {
                log.info(">> 전문대(2년) 파싱");
                responses.addAll(parseAcademicGridRow(row, AcademicCategory.JUNIOR_COLLEGE_2_YEAR, headerMap));
            }
        }
        return responses;
    }

    private Map<Integer, HeaderInfo> parseGridHeader(Element thead) {
        Map<Integer, HeaderInfo> headerMap = new HashMap<>();
        if (thead == null) {
            log.warn("parseGridHeader: thead가 null입니다.");
            return headerMap;
        }

        Elements majorHeaders = thead.select("tr:nth-child(1) > th[colspan=2]");
        if (majorHeaders.isEmpty()) majorHeaders = thead.select("tr:nth-child(1) > td[colspan=2]");

        Elements subHeaders = thead.select("tr:nth-child(2) > th");

        log.info("헤더 파싱: major={}, sub={}", majorHeaders.size(), subHeaders.size());

        if (majorHeaders.isEmpty() || subHeaders.isEmpty()) {
            log.warn("parseGridHeader: 헤더를 찾지 못했습니다. (major/sub 비어있음)");
            return headerMap;
        }

        for (int i = 0; i < subHeaders.size(); i++) {
            String subText = subHeaders.get(i).text();
            int majorIndex = i / 2;
            if (majorIndex < majorHeaders.size()) {
                String majorText = majorHeaders.get(majorIndex).text();
                headerMap.put(i + 1, new HeaderInfo(majorText, subText));
            }
        }
        return headerMap;
    }

    private List<AcademicListResponse> parseAcademicGridRow(Element row, AcademicCategory category, Map<Integer, HeaderInfo> headerMap) {
        List<AcademicScoreRule> scores = new ArrayList<>();
        Elements cells = row.select("th, td");
        for (int i = 1; i < cells.size(); i++) {
            String scoreText = cells.get(i).text();
            if (scoreText.equals("-") || scoreText.isEmpty() || !scoreText.matches("\\d+")) {
                continue;
            }

            HeaderInfo header = headerMap.get(i);
            if (header != null) {
                scores.add(AcademicScoreRule.builder()
                        .educationCategory(category)
                        .majorCondition(header.getMajor())
                        .subCondition(header.getSub())
                        .academicScore(Integer.parseInt(scoreText))
                        .build());
            }
        }
        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    // OverallPointsCrawler.java

    private List<AcademicListResponse> parseHighSchoolRow(Element row) {
        List<AcademicScoreRule> scores = new ArrayList<>();

        Elements cells = row.select("th, td");

        // 2. [수정] 방어 로직 추가
        if (cells.size() < 2) {
            log.warn("고졸 행 파싱 실패: 셀 개수 부족 (found {})", cells.size());
            return new ArrayList<>();
        }

        // 3. 두 번째 셀(데이터)에서 텍스트 추출
        String text = cells.get(1).text();

        Integer majorScore = extractScoreFromText(text, "전공\\s*(\\d+)\\s*점");
        Integer nonMajorScore = extractScoreFromText(text, "비전공\\s*(\\d+)\\s*점");

        scores.add(AcademicScoreRule.builder()
                .educationCategory(AcademicCategory.HIGH_SCHOOL)
                .majorCondition("전공")
                .subCondition("")
                .academicScore(majorScore)
                .build());

        scores.add(AcademicScoreRule.builder()
                .educationCategory(AcademicCategory.HIGH_SCHOOL)
                .majorCondition("비전공")
                .subCondition("")
                .academicScore(nonMajorScore)
                .build());

        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    private List<AcademicListResponse> parseKPScoreList(Element row) {
        List<AcademicScoreRule> scores = new ArrayList<>();

        Elements cells = row.select("th, td");

        // 2. [수정] 방어 로직
        if (cells.size() < 2) {
            return new ArrayList<>();
        }

        // 3. 두 번째 셀(데이터)에서 텍스트 추출
        String text = cells.get(1).wholeText().trim();

        String[] lines = text.split("\n");
        Pattern pattern = Pattern.compile("-\\s*(.*?):\\s*(\\d+)\\s*점");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
                scores.add(AcademicScoreRule.builder()
                        .educationCategory(AcademicCategory.KP_SCHOOL)
                        .majorCondition(matcher.group(1).trim())
                        .subCondition("") // subCondition 명시
                        .academicScore(Integer.parseInt(matcher.group(2).trim()))
                        .build());
            }
        }
        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }
    private List<AcademicListResponse> parseCreditBankList(Element table) {
        List<AcademicScoreRule> scores = new ArrayList<>();

        // 실제 테이블 순서: [학사, 전문학사(3년), 전문학사(2년)]
        List<String> columnConditions = List.of("학사", "전문학사(3년)", "전문학사(2년)");

        Elements rows = table.select("tr");

        for (Element row : rows) {
            // 3. 셀 가져오기 (th, td 모두)
            Elements cells = row.select("th, td");

            // 셀 개수가 부족하면 스킵 (조건1 + 점수3개 = 최소 4개 필요)
            if (cells.size() < 4) continue;

            String rowCondition = cells.get(0).text(); // 예: "40학점 이상"

            // "학점"이라는 글자가 포함되어 있어야 하고, "기준"(헤더)이라는 글자는 없어야 함
            if (!rowCondition.contains("학점") || rowCondition.contains("기준")) {
                continue;
            }

            // 5. 점수 매핑
            for (int i = 0; i < columnConditions.size(); i++) {
                // i=0 -> cell index 1 (학사 점수)
                // i=1 -> cell index 2 (3년제 점수)
                // i=2 -> cell index 3 (2년제 점수)
                if (i + 1 >= cells.size()) break;

                String scoreText = cells.get(i + 1).text();

                // 숫자인 경우에만 저장 ("-" 또는 빈칸 무시)
                if (scoreText.matches("\\d+")) {
                    scores.add(AcademicScoreRule.builder()
                            .educationCategory(AcademicCategory.CREDIT_BANK)
                            .majorCondition(rowCondition)          // "40학점 이상"
                            .subCondition(columnConditions.get(i)) // "학사" 등
                            .academicScore(Integer.parseInt(scoreText))
                            .build());
                }
            }
        }
        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    // --- [자격증 파싱] ---

    private List<QualificationListResponse> parseQualificationList(Element table, String type) {
        List<QualificationScoreRule> scores = new ArrayList<>();
        int subConditionIndex = 1;
        int directScoreIndex = 2;
        int indirectScoreIndex = 3;

        QualificationCategory currentCategory = null;
        String currentCategoryName = "";

        Elements rows = table.select("tbody > tr");

        for (Element row : rows) {
            Elements cells = row.select("th, td"); // 자격증 테이블은 보통 td로만 구성됨
            if (cells.isEmpty()) continue;

            String firstCellText = cells.get(0).text();

            if (firstCellText.contains("자격증 미소지")) {
                String scoreText = cells.last().text();
                scores.add(QualificationScoreRule.builder()
                        .qualifications(QualificationCategory.NONE)
                        .mainCondition("미소지")
                        .subCondition("")
                        .typeCondition("일반")
                        .score(Integer.parseInt(scoreText))
                        .build());

            } else if (firstCellText.contains("운전면허")) {
                // 운전면허증 - 대형/특수
                scores.add(QualificationScoreRule.builder()
                        .qualifications(QualificationCategory.DRIVERS)
                        .mainCondition("대형/특수")
                        .subCondition("")
                        .typeCondition("운전면허증")
                        .score(90).build());

                // 운전면허증 - 1종보통
                scores.add(QualificationScoreRule.builder()
                        .qualifications(QualificationCategory.DRIVERS)
                        .mainCondition("1종보통(수동)")
                        .subCondition("")
                        .typeCondition("운전면허증")
                        .score(87).build());

            } else {
                // 일반 자격증 (국가기술, 일학습병행 등) 카테고리 파악
                if (cells.first().hasAttr("rowspan")) {
                    QualificationCategory cat = QualificationCategory.fromString(firstCellText);
                    if (cat != null) currentCategory = cat;
                    currentCategoryName = firstCellText;
                }

                if (currentCategory != null) {
                    // rowspan 여부에 따라 필요한 셀 개수가 달라짐
                    int offset = cells.first().hasAttr("rowspan") ? 1 : 0;
                    int requiredCells = 3 + offset; // 카테고리 셀이 있으면 4개, 없으면 3개 필요

                    // 인덱스 계산 안전장치
                    if (cells.size() < requiredCells) continue;

                    String subCondition = cells.get(subConditionIndex - (1 - offset)).text();
                    String directScore = cells.get(directScoreIndex - (1 - offset)).text();
                    String indirectScore = cells.get(indirectScoreIndex - (1 - offset)).text();

                    // 직접관련 점수
                    scores.add(QualificationScoreRule.builder()
                            .qualifications(currentCategory)
                            .mainCondition("직접관련")
                            .subCondition(subCondition)
                            .typeCondition(currentCategoryName)
                            .score(Integer.parseInt(directScore)).build());

                    // 간접관련 점수
                    scores.add(QualificationScoreRule.builder()
                            .qualifications(currentCategory)
                            .mainCondition("간접관련")
                            .subCondition(subCondition)
                            .typeCondition(currentCategoryName)
                            .score(Integer.parseInt(indirectScore)).build());
                }
            }
        }
        return scores.stream().map(QualificationListResponse::new).collect(Collectors.toList());
    }

    // --- [출결 파싱] ---

    private List<AttendanceListResponse> parseAttendanceList(Element table, String type) {
        List<AttendanceScoreRule> scores = new ArrayList<>();
        Elements rows = table.select("tr");

        Element headerRow = null;
        Element scoreRow = null;

        // 2. "배점"이라는 글자가 있는 행을 찾습니다.
        for (int i = 0; i < rows.size(); i++) {
            // "배점" 텍스트가 포함된 행을 발견하면
            if (rows.get(i).text().contains("배점")) {
                scoreRow = rows.get(i);      // 이 행이 점수 행
                if (i > 0) {
                    headerRow = rows.get(i - 1); // 바로 윗 행이 헤더 행(일수)
                }
                break;
            }
        }

        // 안전장치: 행을 못 찾았으면 빈 리스트 반환
        if (scoreRow == null || headerRow == null) {
            log.warn("출결 테이블 구조가 예상과 다릅니다. (배점 행을 못 찾음)");
            return new ArrayList<>();
        }

        Elements headerCells = headerRow.select("th, td");
        Elements scoreCells = scoreRow.select("th, td");

        // 3. 매핑 및 파싱
        int size = Math.min(headerCells.size(), scoreCells.size());

        for (int i = 0; i < size; i++) {
            String headerText = headerCells.get(i).text(); // 예: "결석 0일"
            String scoreText = scoreCells.get(i).text(); // 예: "15"

            // 숫자인 경우에만 파싱 (라벨 컬럼 자동 스킵)
            if (scoreText.matches("\\d+")) {
                scores.add(AttendanceScoreRule.builder()
                        .attendanceCount(headerText)
                        .attendanceScore(Integer.parseInt(scoreText))
                        .build());
            }
        }
        return scores.stream().map(AttendanceListResponse::new).collect(Collectors.toList());
    }

    // --- [가산점 파싱] ---

    private List<BonusListResponse> parseBonusPointList(Element listSection, String type) {
        List<BonusScoreRule> scores = new ArrayList<>();
        Elements listItems = listSection.select("li");
        Pattern scorePattern = Pattern.compile("(.*?)\\s*(\\d+)\\s*점");

        for (Element li : listItems) {
            String text = li.text();

            // 키워드로 카테고리 찾기
            BonusCategory category = null;
            if (text.contains("모집특기 경력")) category = BonusCategory.SPECIALTY_EXPERIENCE;
            else if (text.contains("군 추천특기")) category = BonusCategory.RECOMMEND_MILITARY;
            else if (text.contains("국가유공자")) category = BonusCategory.CHILDREN_OF_NATIONAL;
            else if (text.contains("다자녀")) category = BonusCategory.MULTIPLE_CHILDREN;
            else if (text.contains("수급권자")) category = BonusCategory.BENEFICIARY;
            else if (text.contains("현역병입영대상")) category = BonusCategory.ELIGIBLE_ACTIVE_DUTY;
            else if (text.contains("국외이주자")) category = BonusCategory.IMMIGRANTS_ACTIVE_DUTY;
            else if (text.contains("운전적성정밀")) category = BonusCategory.DRIVING_APTITUDE_TEST;

            if (category != null) {
                String mainTitle;
                String content;

                if (text.contains(":")) {
                    String[] parts = text.split(":", 2);
                    mainTitle = parts[0].trim();
                    content = parts[1].trim();
                } else {
                    mainTitle = text.trim(); // 콜론이 없으면 전체를 제목으로
                    content = text;
                }

                Matcher matcher = scorePattern.matcher(content);

                while (matcher.find()) {
                    String condition = matcher.group(1)
                            .replaceAll("[,:]", "") // 1. 기호 제거
                            .trim();
                    // "6개월~1년" 같이 조건이 있거나 없으면 null
                    if (condition.isEmpty() || condition.matches("^[^a-zA-Z0-9가-힣]*$")) condition = "";

                    scores.add(BonusScoreRule.builder()
                            .bonusCategory(category)
                            .mainCondition(mainTitle)
                            .subCondition(condition)
                            .bonusScore(Integer.parseInt(matcher.group(2)))
                            .build());
                }
            }
        }
        return scores.stream().map(BonusListResponse::new).collect(Collectors.toList());
    }

    private List<BonusScoreRule> parseBloodDonationTable(Element table) {
        List<BonusScoreRule> scores = new ArrayList<>();

        // 1. 모든 행(tr)을 가져옵니다. (thead, tbody 구분 없음)
        Elements rows = table.select("tr");
        List<Integer> scoreValues = new ArrayList<>();

        // 2. '배점' 행 찾기 (헤더 역할)
        for (Element row : rows) {
            if (row.text().contains("배점")) {
                Elements cells = row.select("th, td");
                for (Element cell : cells) {
                    // "1점", "2점" 등에서 숫자만 추출
                    String txt = cell.text().replaceAll("[^0-9]", "");
                    if (!txt.isEmpty()) {
                        scoreValues.add(Integer.parseInt(txt));
                    }
                }
                break; // 점수 행을 찾았으면 루프 종료
            }
        }

        // 점수 기준을 못 찾았으면 빈 리스트 반환 (안전장치)
        if (scoreValues.isEmpty()) {
            log.warn("헌혈/봉사 테이블에서 '배점' 정보를 찾지 못했습니다.");
            return scores;
        }

        // 3. 데이터 행 파싱 ("헌혈", "봉사")
        for (Element row : rows) {
            String text = row.text();

            BonusCategory category = null;
            String mainTitle = "";

            if (text.contains("헌혈")) {
                category = BonusCategory.BLOOD_DONATION;
                mainTitle = "헌혈(횟수)";
            } else if (text.contains("봉사")) {
                category = BonusCategory.VOLUNTEER;
                mainTitle = "봉사(시간)";
            }

            if (category == null) continue; // 배점 행이나 기타 행 스킵

            Elements cells = row.select("th, td");
            int dataIndex = 0;

            for (Element cell : cells) {
                String cellText = cell.text();
                // 라벨 컬럼("헌혈", "봉사", "배점")은 건너뛰기
                if (cellText.contains("헌혈") || cellText.contains("봉사") || cellText.contains("배점")) continue;

                // 점수 리스트 크기 내에서만 매핑
                if (dataIndex < scoreValues.size()) {
                    scores.add(BonusScoreRule.builder()
                            .bonusCategory(category)
                            .mainCondition(mainTitle) // "헌혈(횟수)" or "봉사(시간)"
                            .subCondition(cellText)   // "1회", "8~15시간"
                            .bonusScore(scoreValues.get(dataIndex))
                            .build());
                    dataIndex++;
                }
            }
        }
        return scores;
    }

    private Integer extractScoreFromText(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }
}