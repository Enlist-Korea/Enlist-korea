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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j; // 1. 로그 사용을 위한 import
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Slf4j // 2. 로그 어노테이션 추가
public abstract class AbstractRecruitmentParser implements RecruitmentParser {

    // 3. 자식 클래스에서 사용할 수 있도록 protected로 변경
    @Getter
    @AllArgsConstructor
    protected static class HeaderInfo {
        String major; // 예: 4학년
        String sub;   // 예: 재학
    }

    // --- 유틸리티 메서드 (protected) ---

    protected Integer extractScoreFromText(String text, String regex) {
        Matcher matcher = Pattern.compile(regex).matcher(text);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    protected Map<Integer, HeaderInfo> parseGridHeader(Element thead) {
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

    // --- 도메인 파싱 로직 (모두 protected로 변경, type 파라미터 제거, 중복 제거) ---

    protected List<AcademicListResponse> parseMajorTableSmart(Element table) {
        List<AcademicListResponse> responses = new ArrayList<>();

        Map<Integer, HeaderInfo> headerMap = parseGridHeader(table.selectFirst("thead"));
        boolean isGridTable = !headerMap.isEmpty();

        log.info("parseMajorTableSmart 시작: isGridTable={}, headerMapSize={}", isGridTable, headerMap.size());

        Elements rows = table.select("tbody > tr");

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) continue;

            String firstText = cells.get(0).text();

            if (firstText.contains("대학교") && isGridTable) {
                responses.addAll(parseAcademicGridRow(row, AcademicCategory.UNIVERSITY, headerMap));
            } else if (firstText.contains("고졸")) {
                responses.addAll(parseHighSchoolRow(row));
            } else if (firstText.contains("폴리텍")) {
                responses.addAll(parseKPScoreList(row));
            } else if (firstText.contains("전문대")) {
                if (cells.size() > 1 && cells.get(1).text().contains("3년")) {
                    log.info(">> 전문대(3년) 파싱");
                    responses.addAll(parseAcademicGridRow(row, AcademicCategory.JUNIOR_COLLEGE_3_YEAR, headerMap));
                }
            } else if (firstText.contains("2년")) {
                log.info(">> 전문대(2년) 파싱");
                responses.addAll(parseAcademicGridRow(row, AcademicCategory.JUNIOR_COLLEGE_2_YEAR, headerMap));
            }
        }
        return responses;
    }

    protected List<AcademicListResponse> parseAcademicGridRow(Element row, AcademicCategory category, Map<Integer, HeaderInfo> headerMap) {
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

    protected List<AcademicListResponse> parseHighSchoolRow(Element row) {
        List<AcademicScoreRule> scores = new ArrayList<>();
        Elements cells = row.select("th, td");

        if (cells.size() < 2) {
            log.warn("고졸 행 파싱 실패: 셀 개수 부족 (found {})", cells.size());
            return new ArrayList<>();
        }

        String text = cells.get(1).text();
        Integer majorScore = extractScoreFromText(text, "전공\\s*(\\d+)\\s*점");
        Integer nonMajorScore = extractScoreFromText(text, "비전공\\s*(\\d+)\\s*점");

        scores.add(AcademicScoreRule.builder().educationCategory(AcademicCategory.HIGH_SCHOOL)
                .majorCondition("전공").subCondition("").academicScore(majorScore).build());
        scores.add(AcademicScoreRule.builder().educationCategory(AcademicCategory.HIGH_SCHOOL)
                .majorCondition("비전공").subCondition("").academicScore(nonMajorScore).build());

        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    protected List<AcademicListResponse> parseKPScoreList(Element row) {
        List<AcademicScoreRule> scores = new ArrayList<>();
        Elements cells = row.select("th, td");

        if (cells.size() < 2) return new ArrayList<>();

        String text = cells.get(1).wholeText().trim();
        String[] lines = text.split("\n");
        Pattern pattern = Pattern.compile("-\\s*(.*?):\\s*(\\d+)\\s*점");

        for (String line : lines) {
            Matcher matcher = pattern.matcher(line.trim());
            if (matcher.find()) {
                scores.add(AcademicScoreRule.builder()
                        .educationCategory(AcademicCategory.KP_SCHOOL)
                        .majorCondition(matcher.group(1).trim())
                        .subCondition("")
                        .academicScore(Integer.parseInt(matcher.group(2).trim()))
                        .build());
            }
        }
        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    protected List<AcademicListResponse> parseCreditBankList(Element table) {
        List<AcademicScoreRule> scores = new ArrayList<>();
        List<String> columnConditions = List.of("학사", "전문학사(3년)", "전문학사(2년)");
        Elements rows = table.select("tr");

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.size() < 4) continue;

            String rowCondition = cells.get(0).text();
            if (!rowCondition.contains("학점") || rowCondition.contains("기준")) continue;

            for (int i = 0; i < columnConditions.size(); i++) {
                if (i + 1 >= cells.size()) break;
                String scoreText = cells.get(i + 1).text();
                if (scoreText.matches("\\d+")) {
                    scores.add(AcademicScoreRule.builder()
                            .educationCategory(AcademicCategory.CREDIT_BANK)
                            .majorCondition(rowCondition)
                            .subCondition(columnConditions.get(i))
                            .academicScore(Integer.parseInt(scoreText))
                            .build());
                }
            }
        }
        return scores.stream().map(AcademicListResponse::new).collect(Collectors.toList());
    }

    protected List<QualificationListResponse> parseQualificationList(Element table) {
        List<QualificationScoreRule> scores = new ArrayList<>();
        int subConditionIndex = 1;
        int directScoreIndex = 2;
        int indirectScoreIndex = 3;

        QualificationCategory currentCategory = null;
        String currentCategoryName = "";

        Elements rows = table.select("tbody > tr");

        for (Element row : rows) {
            Elements cells = row.select("th, td");
            if (cells.isEmpty()) continue;
            String firstCellText = cells.get(0).text();

            if (firstCellText.contains("자격증 미소지")) {
                String scoreText = cells.last().text();
                scores.add(QualificationScoreRule.builder().qualifications(QualificationCategory.NONE)
                        .mainCondition("미소지").subCondition("").typeCondition("일반")
                        .score(Integer.parseInt(scoreText)).build());
            } else if (firstCellText.contains("운전면허")) {
                scores.add(QualificationScoreRule.builder().qualifications(QualificationCategory.DRIVERS)
                        .mainCondition("대형/특수").subCondition("").typeCondition("운전면허증").score(90).build());
                scores.add(QualificationScoreRule.builder().qualifications(QualificationCategory.DRIVERS)
                        .mainCondition("1종보통(수동)").subCondition("").typeCondition("운전면허증").score(87).build());
            } else {
                if (cells.first().hasAttr("rowspan")) {
                    QualificationCategory cat = QualificationCategory.fromString(firstCellText);
                    if (cat != null) currentCategory = cat;
                    currentCategoryName = firstCellText;
                }
                if (currentCategory != null) {
                    int offset = cells.first().hasAttr("rowspan") ? 1 : 0;
                    int requiredCells = 3 + offset;
                    if (cells.size() < requiredCells) continue;

                    String subCondition = cells.get(subConditionIndex - (1 - offset)).text();
                    String directScore = cells.get(directScoreIndex - (1 - offset)).text();
                    String indirectScore = cells.get(indirectScoreIndex - (1 - offset)).text();

                    scores.add(QualificationScoreRule.builder().qualifications(currentCategory)
                            .mainCondition("직접관련").subCondition(subCondition).typeCondition(currentCategoryName)
                            .score(Integer.parseInt(directScore)).build());
                    scores.add(QualificationScoreRule.builder().qualifications(currentCategory)
                            .mainCondition("간접관련").subCondition(subCondition).typeCondition(currentCategoryName)
                            .score(Integer.parseInt(indirectScore)).build());
                }
            }
        }
        return scores.stream().map(QualificationListResponse::new).collect(Collectors.toList());
    }

    protected List<AttendanceListResponse> parseAttendanceList(Element table) {
        List<AttendanceScoreRule> scores = new ArrayList<>();
        Elements rows = table.select("tr");
        Element headerRow = null;
        Element scoreRow = null;

        for (int i = 0; i < rows.size(); i++) {
            if (rows.get(i).text().contains("배점")) {
                scoreRow = rows.get(i);
                if (i > 0) headerRow = rows.get(i - 1);
                break;
            }
        }

        if (scoreRow == null || headerRow == null) {
            log.warn("출결 테이블 구조가 예상과 다릅니다. (배점 행을 못 찾음)");
            return new ArrayList<>();
        }

        Elements headerCells = headerRow.select("th, td");
        Elements scoreCells = scoreRow.select("th, td");
        int size = Math.min(headerCells.size(), scoreCells.size());

        for (int i = 0; i < size; i++) {
            String headerText = headerCells.get(i).text();
            String scoreText = scoreCells.get(i).text();
            if (scoreText.matches("\\d+")) {
                scores.add(AttendanceScoreRule.builder()
                        .attendanceCount(headerText)
                        .attendanceScore(Integer.parseInt(scoreText))
                        .build());
            }
        }
        return scores.stream().map(AttendanceListResponse::new).collect(Collectors.toList());
    }

    protected List<BonusListResponse> parseBonusPointList(Element listSection) {
        List<BonusScoreRule> scores = new ArrayList<>();
        Elements listItems = listSection.select("li");
        Pattern scorePattern = Pattern.compile("(.*?)\\s*(\\d+)\\s*점");

        for (Element li : listItems) {
            String text = li.text();
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
                    mainTitle = text.trim();
                    content = text;
                }
                Matcher matcher = scorePattern.matcher(content);
                while (matcher.find()) {
                    String condition = matcher.group(1).replaceAll("[,:]", "").trim();
                    if (condition.isEmpty() || condition.matches("^[^a-zA-Z0-9가-힣]*$")) condition = "";
                    scores.add(BonusScoreRule.builder().bonusCategory(category)
                            .mainCondition(mainTitle).subCondition(condition)
                            .bonusScore(Integer.parseInt(matcher.group(2))).build());
                }
            }
        }
        return scores.stream().map(BonusListResponse::new).collect(Collectors.toList());
    }

    protected List<BonusScoreRule> parseBloodDonationTable(Element table) {
        List<BonusScoreRule> scores = new ArrayList<>();
        Elements rows = table.select("tr");
        List<Integer> scoreValues = new ArrayList<>();

        for (Element row : rows) {
            if (row.text().contains("배점")) {
                Elements cells = row.select("th, td");
                for (Element cell : cells) {
                    String txt = cell.text().replaceAll("[^0-9]", "");
                    if (!txt.isEmpty()) scoreValues.add(Integer.parseInt(txt));
                }
                break;
            }
        }

        if (scoreValues.isEmpty()) {
            log.warn("헌혈/봉사 테이블에서 '배점' 정보를 찾지 못했습니다.");
            return scores;
        }

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

            if (category == null) continue;

            Elements cells = row.select("th, td");
            int dataIndex = 0;
            for (Element cell : cells) {
                String cellText = cell.text();
                if (cellText.contains("헌혈") || cellText.contains("봉사") || cellText.contains("배점")) continue;

                if (dataIndex < scoreValues.size()) {
                    scores.add(BonusScoreRule.builder()
                            .bonusCategory(category).mainCondition(mainTitle).subCondition(cellText)
                            .bonusScore(scoreValues.get(dataIndex)).build());
                    dataIndex++;
                }
            }
        }
        return scores;
    }
}