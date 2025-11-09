package army.helper.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.Data;

@Data
public class RequirementApiDto {

    @JacksonXmlProperty(localName = "mjstaNm") // 모집상태
    private String status;

    @JacksonXmlProperty(localName = "spcCd") //  실제 XML의 특기코드 태그명
    private String specialtyCode;

    @JacksonXmlProperty(localName = "spcNm") //  특기명
    private String specialtyName;

    @JacksonXmlProperty(localName = "mojipGbnm") // 모집 구분명
    private String mojipGbnm;

    @JacksonXmlProperty(localName = "majorRqrNm") // 전공 요구
    private String majorRequirement;

    @JacksonXmlProperty(localName = "certifiRqrNm") // 자격증 요구
    private String certificateName;
}