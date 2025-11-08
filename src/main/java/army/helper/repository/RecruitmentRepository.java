package army.helper.repository;

import army.helper.domain.Recruitment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface RecruitmentRepository extends JpaRepository<Recruitment, Long> {

    @Query("SELECT r FROM Recruitment r " +
            "JOIN FETCH r.specialty " +
            "WHERE(:branch IS NULL OR r.branch =:branch) " +
            "ORDER BY r.applyStart DESC ")
    List<Recruitment> findFileRecruitments(
            @Param("branch") String branch,
            @Param("mojipGbnm") String mojipGbnm,
            @Param("now")OffsetDateTime now
    );
}
