package army.helper.service;

import army.helper.domain.Recruitment;
import army.helper.domain.Specialty;
import army.helper.dto.RequirementApiDto;
import army.helper.infrastructure.ApiClient;
import army.helper.repository.RecruitmentRepository;
import army.helper.repository.SpecialtyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecruitSyncService {

    private final ApiClient apiClient;
    private final RecruitmentRepository recruitmentRepository;
    private final SpecialtyRepository specialtyRepository;

    private String mapApiToStandardCode(String apiCode){
        return "ST_" + apiCode;
    }

    public int syncAll(){
        log.info("Starting data synchronization process.");
        List<Recruitment> recruitments = apiClient.fetchCurrentRecruitments();
        List<RequirementApiDto> requirementApiDto = apiClient.fetchCurrentRecruitRequirements();

        recruitmentRepository.deleteAll();
        specialtyRepository.deleteAll();

        recruitmentRepository.saveAll(recruitments);
        log.info("Saved() recruiments", recruitments.size());

        List<Specialty> specialtiesToSave = mapRequirementToSpecialties(requirementApiDto);
        specialtyRepository.saveAll(specialtiesToSave);
        log.info("Saved() specialties", specialtiesToSave.size());
        return recruitments.size();
    }

    private List<Specialty> mapRequirementToSpecialties(List<RequirementApiDto> requirements){
        return requirements.stream().map(req -> {
            String major = req.getMajorRequirement();
            String certName = req.getCertificateName();

            return Specialty.builder()
                    .branch(req.getStatus())
                    .code(mapApiToStandardCode(req.getSpecialtyCode()))
                    .name(req.getSpecialtyName())
                    .requiredCertificateName(certName != null && !certName.isBlank() ? certName:null)
                    .requiredMajorNames(major != null && !major.isBlank() ?major:null)
                    .build();
        }).collect(Collectors.toList());
    }
}
