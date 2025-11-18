package army.helper.service.overall_points;

import army.helper.config.CrawlProperties;
import army.helper.domain.overall_points.AttendanceScoreRule;
import army.helper.domain.overall_points.QualificationDetail.QualificationCategory;
import army.helper.domain.overall_points.QualificationDetail.QualificationScoreRule;
import army.helper.domain.overall_points.bonusDetail.BonusCategory;
import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import army.helper.domain.overall_points.majorDetiail.AcademicCategory;
import army.helper.domain.overall_points.majorDetiail.AcademicScoreRule;
import army.helper.dto.CrawlTarget;
import army.helper.dto.overall_points.detail.AcademicListResponse;
import army.helper.dto.overall_points.detail.AttendanceListResponse;
import army.helper.dto.overall_points.detail.BonusListResponse;
import army.helper.dto.overall_points.detail.QualificationListResponse;
import army.helper.infrastructure.OverallPointsCrawler;
import army.helper.infrastructure.OverallPointsCrawler.OverallPointsCrawlerResult;
import army.helper.repository.overall.AcademicScoreRuleRepository;
import army.helper.repository.overall.AttendanceScoreRuleRepository;
import army.helper.repository.overall.BonusScoreRuleRepository;
import army.helper.repository.overall.QualificationScoreRuleRepository;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataSaveService {
    private final OverallPointsCrawler crawler;
    private final CrawlProperties properties;

    private final QualificationScoreRuleRepository qualificationScoreRuleRepository;
    private final AcademicScoreRuleRepository academicScoreRuleRepository;
    private final AttendanceScoreRuleRepository attendanceScoreRuleRepository;
    private final BonusScoreRuleRepository bonusScoreRuleRepository;


    @PostConstruct
    public void initCrawl() {
        log.info("🚀 @PostConstruct: 즉시 크롤링을 1회 실행합니다...");
        crawlAndSave(); // 4. 기존 크롤링 로직 호출
    }

    @Scheduled(cron = " 0 15 5 * * *", zone = "Asia/Seoul")
    public void crawlAndSave(){
        log.info("전체 url 크롤링 및 저장합니다.");
        log.info("중복 방지를 위해 기존 점수 데이터를 초기화합니다.");
        academicScoreRuleRepository.deleteAll();
        qualificationScoreRuleRepository.deleteAll();
        attendanceScoreRuleRepository.deleteAll();
        bonusScoreRuleRepository.deleteAll();
        List<CrawlTarget> targets = properties.getTargets();

        for(CrawlTarget target : targets){
            log.info("[{}] 페이지 크롤링 시작 {}", target.getType(), target.getUrl());

            try{
                OverallPointsCrawlerResult result = crawler.crawlPageData(target.getType(), target.getUrl());
                log.info("✅ 크롤링 완료 [{}]: 학력 = {}개, 출결 = {}개, 자격 = {}개, 가산점 = {}개",
                        target.getType(),
                        result.getAcademicList().size(),
                        result. getAttendanceList().size(),
                        result.getQualificationList().size(),
                        result.getBonusList().size());

                saveAcademicData(result.getAcademicList());
                saveQualificationData(result.getQualificationList());
                saveAttendanceData(result.getAttendanceList());
                saveBonusData(result.getBonusList());
            } catch (Exception e){
                log.error("[{}] 페이지 처리 중 오류 발생: {}", target.getType(), target.getUrl(), e);
            }
        }
        log.info("전체 크롤링 작업을 완료했습니다.");
    }

    private void saveAcademicData(List<AcademicListResponse> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.warn("저장할 학력 점수 데이터가 없습니다.");
            return; // 저장할 데이터가 없으면 종료
        }

        // 1. DTO 리스트를 Entity 리스트로 변환 (Stream.map 활용)
        List<AcademicScoreRule> rulesToSave = dtos.stream()
                .map(dto -> {
                    // DTO의 String category(예: "HIGH_SCHOOL")를 Enum으로 변환
                    AcademicCategory categoryEnum = AcademicCategory.valueOf(dto.getCategory());

                    return AcademicScoreRule.builder()
                            .educationCategory(categoryEnum)
                            .majorCondition(dto.getMajorCondition())
                            .subCondition(dto.getSubCondition())
                            .academicScore(dto.getScore())
                            .build();
                })
                .collect(Collectors.toList());

        // 2. Entity 리스트를 DB에 일괄 저장
        academicScoreRuleRepository.saveAll(rulesToSave);

        log.info("✅ {}개의 학력 점수 규칙을 DB에 저장했습니다.", rulesToSave.size());
    }

    private void saveQualificationData(List<QualificationListResponse> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.warn("저장할 학력 점수 데이터가 없습니다.");
            return; // 저장할 데이터가 없으면 종료
        }

        // 1. DTO 리스트를 Entity 리스트로 변환 (Stream.map 활용)
        List<QualificationScoreRule> rulesToSave = dtos.stream()
                .map(dto -> {
                    QualificationCategory categoryEnum = QualificationCategory.valueOf(dto.getQualifications());

                    return QualificationScoreRule.builder()
                            .qualifications(categoryEnum)
                            .mainCondition(dto.getMainCondition())
                            .subCondition(dto.getSubCondition())
                            .typeCondition(dto.getTypeCondition())
                            .score(dto.getScore())
                            .build();
                })
                .collect(Collectors.toList());

        // 2. Entity 리스트를 DB에 일괄 저장
        qualificationScoreRuleRepository.saveAll(rulesToSave);

        log.info("✅ {}개의 자격증 점수 규칙을 DB에 저장했습니다.", rulesToSave.size());
    }

    private void saveAttendanceData(List<AttendanceListResponse> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.warn("저장할 학력 점수 데이터가 없습니다.");
            return; // 저장할 데이터가 없으면 종료
        }

        // 1. DTO 리스트를 Entity 리스트로 변환 (Stream.map 활용)
        List<AttendanceScoreRule> rulesToSave = dtos.stream()
                .map(dto -> {
                    return AttendanceScoreRule.builder()
                            .attendanceCount(dto.getCount())
                            .attendanceScore(dto.getScore())
                            .build();
                })
                .collect(Collectors.toList());

        // 2. Entity 리스트를 DB에 일괄 저장
        attendanceScoreRuleRepository.saveAll(rulesToSave);

        log.info("✅ {}개의 출결 점수 규칙을 DB에 저장했습니다.", rulesToSave.size());
    }

    private void saveBonusData(List<BonusListResponse> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            log.warn("저장할 가산점 점수 데이터가 없습니다.");
            return ;
        }

        List<BonusScoreRule> rulesToSave = dtos.stream()

                .map(dto -> {
                    BonusCategory categoryEnum = BonusCategory.valueOf(dto.getCategory());
                    return BonusScoreRule.builder()
                            .bonusCategory(categoryEnum)
                            .mainCondition(dto.getMainCondition())
                            .subCondition(dto.getSubCondition())
                            .bonusScore(dto.getBonusScore())
                            .build();
                })
                .collect(Collectors.toList());

        bonusScoreRuleRepository.saveAll(rulesToSave);

        log.info("✅{}개의 가산점 점수 규칙을 DB에 저장했습니다.", rulesToSave.size());
    }
}
