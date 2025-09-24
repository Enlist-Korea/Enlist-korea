package com.militarysupport.recruit_helper.repository;

import com.militarysupport.recruit_helper.domain.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    Optional<Specialty> findByName(String name);

    List<Specialty> findByBranchAndIsActive(String branch, boolean isActive);

}
