package army.helper.repository;

import army.helper.domain.Branch;
import army.helper.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
    Optional<Specialty> findByName(String specialtyName);
    List<Specialty> findByBranchAndActive(String branch, boolean active);
    boolean existsByBranchAndCode(Branch branch, String code);
}
