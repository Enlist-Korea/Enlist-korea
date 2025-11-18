package army.helper.repository.recruitment;

import army.helper.domain.recruitment_status.Branch; // 1. Branch 임포트
import army.helper.domain.recruitment_status.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    // 2. [수정] JOIN FETCH -> LEFT JOIN FETCH
    @Query("SELECT r FROM Recruitment r " +
            "LEFT JOIN FETCH r.specialty " +
            "WHERE (:branch IS NULL OR r.branch = :branch) " + // 3. [수정] :branch는 이제 Enum
            // 4. [추가] specialtyName 필터 추가 (r.specialtyName은 Recruitment 엔티티의 필드)
            "AND (:mojipGbnm IS NULL OR r.specialtyName LIKE %:mojipGbnm%) " +
            "ORDER BY r.applyStart DESC ")
    List<Recruitment> findFileRecruitments(
            @Param("branch") Branch branch, // 5. [수정] String -> Branch 타입
            @Param("mojipGbnm") String mojipGbnm, // (Service의 specialtyName이 여기로 옴)
            @Param("now")OffsetDateTime now
    );
}