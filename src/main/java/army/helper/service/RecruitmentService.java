package army.helper.service;

import army.helper.domain.Recruitment;
import army.helper.dto.RecruitmentStatusResponse;
import army.helper.repository.RecruitmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecruitmentService {

    private final RecruitmentRepository recruitmentRepository;

    public List<RecruitmentStatusResponse> findStatusByFilters(
            String status,
            String specialtyName,
            String statusType){

        OffsetDateTime now = OffsetDateTime.now();

        List<Recruitment> entities = recruitmentRepository.findFileRecruitments(
                status,
                specialtyName,
                now
        );

        return entities.stream()
                .map(RecruitmentStatusResponse::fromEntity)
                .filter(dto -> statusType == null || statusType.equals(dto.getMojipStatus()))
                .collect(Collectors.toList());
    }
}
