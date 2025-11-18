package army.helper.service.overall_points;

import army.helper.domain.overall_points.AttendanceScoreRule;
import army.helper.domain.overall_points.QualificationDetail.QualificationCategory;
import army.helper.domain.overall_points.QualificationDetail.QualificationScoreRule;
import army.helper.domain.overall_points.bonusDetail.BonusCategory;
import army.helper.domain.overall_points.bonusDetail.BonusScoreRule;
import army.helper.domain.overall_points.majorDetiail.AcademicCategory;
import army.helper.domain.overall_points.majorDetiail.AcademicScoreRule;
import army.helper.dto.overall_points.ScoreQueryDto;
import army.helper.repository.overall.AcademicScoreRuleRepository;
import army.helper.repository.overall.AttendanceScoreRuleRepository;
import army.helper.repository.overall.BonusScoreRuleRepository;
import army.helper.repository.overall.QualificationScoreRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreQueryService {
    private final AcademicScoreRuleRepository academicScoreRuleRepository;
    private final AttendanceScoreRuleRepository attendanceScoreRuleRepository;
    private final BonusScoreRuleRepository bonusScoreRuleRepository;
    private final QualificationScoreRuleRepository qualificationScoreRuleRepository;

    public int getScore(ScoreQueryDto queryDto){
        return switch (queryDto.getQueryGroup()) {
            case "ACADEMIC" -> findAcademicScore(queryDto);
            case "QUALIFICATION" -> findQualificationScore(queryDto);
            case "ATTENDANCE" -> findAttendanceScore(queryDto);
            case "BONUS" -> findBonusScore(queryDto);
            default -> {
                log.warn("알 수 없는 점수 조회 그룹: {}", queryDto.getQueryGroup());
                yield 0;
            }
        };
    }
    private int findAcademicScore(ScoreQueryDto dto) {
        AcademicCategory categoryEnum = AcademicCategory.valueOf(dto.getCategory());
        return academicScoreRuleRepository.findByEducationCategoryAndMajorConditionAndSubCondition(
                        categoryEnum,
                        dto.getMainCondition(),
                        dto.getSubCondition()
                )
                .map(AcademicScoreRule::getAcademicScore) // .map()과 .orElse() 사용
                .orElse(0); // 찾지 못하면 0점 반환
    }

    private int findQualificationScore(ScoreQueryDto dto) {
        QualificationCategory categoryEnum = QualificationCategory.valueOf(dto.getCategory());
        return qualificationScoreRuleRepository.findByQualificationsAndMainConditionAndSubConditionAndTypeCondition(
                        categoryEnum,
                        dto.getMainCondition(),
                        dto.getSubCondition(),
                         dto.getTypeCondition()
                )
                .map(QualificationScoreRule::getScore) // .map()과 .orElse() 사용
                .orElse(0); // 찾지 못하면 0점 반환
    }

    private int findAttendanceScore(ScoreQueryDto dto) {
        return attendanceScoreRuleRepository.findByAttendanceCount(
                dto.getAttendanceCount()
                 )
                .map(AttendanceScoreRule::getAttendanceScore)
                .orElse(0);

    }
    private int findBonusScore(ScoreQueryDto dto) {
        BonusCategory categoryEnum = BonusCategory.valueOf(dto.getCategory());
        return bonusScoreRuleRepository.findByBonusCategoryAndMainConditionAndSubCondition(
                        categoryEnum,
                        dto.getMainCondition(),
                        dto.getSubCondition()
                )
                .map(BonusScoreRule::getBonusScore) // .map()과 .orElse() 사용
                .orElse(0); // 찾지 못하면 0점 반환
    }
}
