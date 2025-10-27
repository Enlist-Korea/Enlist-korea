package com.militarysupport.scoring.application;

import com.militarysupport.recruit_helper.domain.Crawler.RecruitmentCriteriaEntity;
import com.militarysupport.scoring.infrastructure.persistence.BonusPointDetailRepository;
import com.militarysupport.scoring.infrastructure.persistence.QualificationMajorDetailRepository;
import com.militarysupport.scoring.infrastructure.persistence.RecruitmentCriteriaRepository;
import com.militarysupport.scoring.interfaces.dto.ScoreRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * 스모크 테스트: 서비스가 컴파일/런타임 의존성 없이 동작하는지 확인.
 * - 레포지토리는 Mockito mock 사용 (DB 연결 불필요)
 */
class ScoringServiceTest {

    @Test
    void compileAndRun_withMocks_returnsList() {
        // given: mock repos
        RecruitmentCriteriaRepository criteriaRepo = Mockito.mock(RecruitmentCriteriaRepository.class);
        QualificationMajorDetailRepository qmdRepo = Mockito.mock(QualificationMajorDetailRepository.class);
        BonusPointDetailRepository bonusRepo = Mockito.mock(BonusPointDetailRepository.class);

        // dummy criteria
        RecruitmentCriteriaEntity c = RecruitmentCriteriaEntity.builder()
                .militaryService("테스트 병과")
                .qualificationScore(50)
                .majorScore(40)
                .attendanceScore(5)
                .bonusMaxScore(10)
                .effectiveDate(LocalDate.now())
                .build();
        // JPA가 아닌 순수 객체라 id는 null일 수 있지만 테스트 목적상 OK
        when(criteriaRepo.findAll()).thenReturn(List.of(c));

        ScoringService service = new ScoringService(criteriaRepo, qmdRepo, bonusRepo);

        ScoreRequest req = new ScoreRequest(
                "기사이상", "전공", "4년제졸업", 0, List.of()
        );

        // when
        var result = service.scoreAllBranches(req);

        // then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).branchName()).isEqualTo("테스트 병과");
    }
}
