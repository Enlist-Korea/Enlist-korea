package army.helper.dto;

import army.helper.domain.Recruitment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecruitmentStatusResponse {

    private String branch;
    private String mojipGbnm;
    private String specialtyCode;
    private String specialtyName;
    private String mojipStatus;
    private Integer quota;
    private Integer acceptPcnt;
    private OffsetDateTime applyStart;
    private OffsetDateTime applyEnd;
    private BigDecimal rate;

    public static RecruitmentStatusResponse fromEntity(Recruitment entity){
        OffsetDateTime now = LocalDateTime.now().atZone(ZoneId.of("Asia/Seoul")).toOffsetDateTime();

        String determinedStatus = determineRecruitmentStatus(
                entity.getApplyStart(),
                entity.getApplyEnd(),
                now
        );

        return RecruitmentStatusResponse.builder()
                .branch(entity.getBranch())
                .mojipGbnm(entity.getMojipGbnm())
                .specialtyCode(entity.getCode())
                .specialtyName(entity.getSpecialtyName())
                .mojipStatus(determinedStatus)
                .quota(entity.getQuota())
                .acceptPcnt(entity.getApplied())
                .applyStart(entity.getApplyStart())
                .applyEnd(entity.getApplyEnd())
                .build();
    }

    private static String determineRecruitmentStatus(
            OffsetDateTime start,
            OffsetDateTime end,
            OffsetDateTime now){

        if (now.isBefore(start)){
            return "모집예정";
        } else if(now.isAfter(end)) {
            return "모집완료";
        }else{
            return "모집중";
        }
    }
}
