package army.helper.dto;

import lombok.Data;

@Data
public class RequirementApiDto {
    private String status;
    private String specialtyCode;
    private String specialtyName;

    //필수 조건 필드
    private String majorRequirement; // 학과, 전공
    private String certificateName; // 자격증 이름
}
