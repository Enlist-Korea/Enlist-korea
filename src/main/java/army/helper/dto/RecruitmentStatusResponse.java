package army.helper.dto;

import army.helper.domain.Branch;
import army.helper.domain.Recruitment;
import com.fasterxml.jackson.annotation.JsonProperty; // 1. @JsonProperty 임포트
import jakarta.xml.bind.annotation.*;
import java.math.RoundingMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 모집 현황 응답 DTO (JSON/XML 공용)
 * - JSON: 프론트엔드 API 응답용 (List<RecruitmentStatusResponse> 형태)
 * - XML: 레거시 병무청 API 파싱용 (toEntity() 등)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@XmlRootElement(name = "item") // XML 파싱용
@XmlAccessorType(XmlAccessType.FIELD) // XML 파싱용
public class RecruitmentStatusResponse {

    /**
     * React Key용 고유 ID (DB Entity ID)
     */
    private Long id;

    /**
     * 군 구분명 (육군, 해군, 공군, 해병대)
     */
    @XmlElement(name = "gunGbnm")
    private String branch;

    /**
     * 모집 구분명 (기술행정병, 일반병 등)
     */
    @XmlElement(name = "mojipGbnm")
    private String mojipGbnm;

    /**
     * 특기 코드 (XML: gsteukgiCd, JSON: code)
     */
    @XmlElement(name = "gsteukgiCd")
    @JsonProperty("code") // JSON 출력 시 'code'
    private String specialtyCode;

    /**
     * 특기 이름 (XML: gsteukgiNm, JSON: name)
     */
    @XmlElement(name = "gsteukgiNm")
    @JsonProperty("name") // JSON 출력 시 'name'
    private String specialtyName;

    /**
     * 모집 시작일시 (XML: jeopsuSjdtm, JSON: applyStart)
     * - fromEntity() 사용 시 ISO 8601 문자열 (예: 2025-11-01T09:00:00+09:00)
     * - XML 파싱 시 yyyyMMdd 문자열 (예: 20251101)
     */
    @XmlElement(name = "jeopsuSjdtm")
    private String applyStart;

    /**
     * 모집 종료일시 (XML: jeopsuJrdtm, JSON: applyEnd)
     * - fromEntity() 사용 시 ISO 8601 문자열
     * - XML 파싱 시 yyyyMMdd 문자열
     */
    @XmlElement(name = "jeopsuJrdtm")
    private String applyEnd;

    /**
     * 모집 인원 (XML: jeopsuPcnt, JSON: quota)
     */
    @XmlElement(name = "jeopsuPcnt")
    private String quota;

    /**
     * 접수 인원 (XML: seonbalPcnt, JSON: jeopsuPcnt)
     * (프론트 Mock 데이터의 'jeopsuPcnt'와 일치시키기 위함)
     */
    @XmlElement(name = "seonbalPcnt")
    @JsonProperty("jeopsuPcnt") // JSON 출력 시 'jeopsuPcnt'
    private String acceptPcnt;

    /**
     * 경쟁률
     */
    @XmlElement(name = "rate")
    private String rate;

    /**
     * 모집년도
     */
    @XmlElement(name = "mojipYy")
    private String recruitYear;

    /**
     * 모집차수
     */
    @XmlElement(name = "mojipTms")
    private String recruitRound;

    /**
     * 입영일 (프론트 Mock 데이터와 스키마 일치용)
     */
    @XmlElement(name = "iyyjsijakYm")
    private String enlistStart;

    /**
     * 모집 상태 (모집중, 모집예정, 모집완료, 미정)
     * - fromEntity() 실행 시 이 필드에 DB 날짜 기준의 계산 결과가 저장됨.
     */
    @Builder.Default
    private String mojipStatus = null;



    /**
     * [핵심 수정] 모집 상태를 반환합니다.
     * 1. fromEntity()를 통해 DB에서 조회 시:
     * - 'mojipStatus' 필드에 이미 계산된 값(예: "모집중")이 있으므로 그 값을 즉시 반환합니다.
     * - RecruitmentService의 필터 로직(statusType.equals(dto.getMojipStatus()))이 이 경로를 탑니다.
     * 2. XML 파싱 등으로 DTO가 생성된 경우:
     * - 'mojipStatus' 필드가 null이므로, 'applyStart'(yyyyMMdd)를 파싱하여 상태를 동적으로 계산합니다.
     */
    public String getMojipStatus() {
        // 1. [DB 조회 시] 'mojipStatus' 필드에 값이 있으면(fromEntity가 채워줌) 즉시 반환
        if (this.mojipStatus != null) {
            return this.mojipStatus;
        }

        // 2. [XML 파싱 시] 'mojipStatus' 필드가 비어있으면, applyStart(yyyyMMdd) 기준으로 계산
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime start = parseDate(applyStart); // yyyyMMdd 파싱 시도
        OffsetDateTime end = parseDate(applyEnd);

        if (start == null || end == null) return "미정";
        if (now.isBefore(start)) return "모집예정";
        if (now.isAfter(end)) return "모집완료";
        return "모집중";
    }

    /**
     * 특기 코드가 존재하는지 확인 (XML 파싱용)
     */
    public boolean hasSpecialtyCode() {
        return specialtyCode != null && !specialtyCode.isBlank();
    }

    /**
     * (중요) DB Entity -> DTO 변환 메소드
     * Service 레이어에서 DB 조회 후 프론트엔드로 데이터를 보낼 때 사용됩니다.
     */
    public static RecruitmentStatusResponse fromEntity(Recruitment entity) {
        if (entity == null) return null;

        String calculatedRate = null;
        Integer quota = entity.getQuota(); // 모집인원
        Integer applied = entity.getApplied();

        // 2. quota와 applied가 모두 존재하고, quota가 0이 아닐 때만 계산
        if (quota != null && applied != null && quota > 0) {
            try {
                BigDecimal quotaBd = new BigDecimal(quota);
                BigDecimal appliedBd = new BigDecimal(applied);

                // 3. 경쟁률 계산 (접수인원 / 모집인원), 소수점 2자리 반올림
                BigDecimal rateBd = appliedBd.divide(quotaBd, 2, RoundingMode.HALF_UP);
                calculatedRate = rateBd.toPlainString();

            } catch (ArithmeticException e) {
                // (혹시 모를 산술 오류 시) 기존 DB 값 사용
                calculatedRate = (entity.getRate() != null) ? entity.getRate().toPlainString() : "0.00";
            }
        }
        // 4. 계산이 불가능하면, DB에 저장된 API 원본 rate 값을 사용 (Fallback)
        else if (entity.getRate() != null) {
            calculatedRate = entity.getRate().toPlainString();
        }
        // -
        return RecruitmentStatusResponse.builder()
                // 1. 프론트엔드 스키마와 일치하는 필드 매핑
                .id(entity.getId()) // BaseEntity의 ID
                .branch(entity.getBranch() != null ? entity.getBranch().name() : null)
                .mojipGbnm(entity.getMojipGbnm())
                .specialtyCode(entity.getCode()) // JSON 'code'
                .specialtyName(entity.getSpecialtyName()) // JSON 'name'
                .quota(entity.getQuota() != null ? String.valueOf(entity.getQuota()) : null)
                .acceptPcnt(entity.getApplied() != null ? String.valueOf(entity.getApplied()) : null) // JSON 'jeopsuPcnt'
                .rate(calculatedRate)

                // 2. 날짜 필드: DB의 OffsetDateTime -> ISO 8601 문자열로 변환
                .applyStart(entity.getApplyStart() != null ? entity.getApplyStart().toString() : null)
                .applyEnd(entity.getApplyEnd() != null ? entity.getApplyEnd().toString() : null)
                // TODO: Entity에 입영일 필드 추가 시 주석 해제
                .enlistStart(entity.getEnlistDate() != null ? entity.getEnlistDate().toString() : null)

                // 3. [핵심] DB 날짜 기준으로 '모집 상태'를 미리 계산하여 'mojipStatus' 필드에 저장
                .mojipStatus(determineRecruitmentStatus(
                        entity.getApplyStart(),
                        entity.getApplyEnd()
                ))
                .build();
    }

    /**
     * [Helper] DB에서 가져온 OffsetDateTime 날짜를 기준으로 모집 상태를 결정합니다.
     */
    private static String determineRecruitmentStatus(
            java.time.OffsetDateTime start,
            java.time.OffsetDateTime end
    ) {
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now();

        if (start == null || end == null) return "미정";
        if (now.isBefore(start)) return "모집예정";
        if (now.isAfter(end)) return "모집완료";
        return "모집중";
    }


    // --- (이하 XML 파싱을 위한 레거시 메소드) ---

    /**
     * DTO -> Entity 변환 (XML 파싱 후 DB 저장용)
     */
    public Recruitment toEntity() {
        return Recruitment.builder()
                .branch(Branch.fromRaw(branch))
                .code(specialtyCode)
                .specialtyName(specialtyName)
                .mojipGbnm(mojipGbnm)
                .quota(parseInt(quota))
                .applied(parseInt(acceptPcnt)) // XML의 seonbalPcnt가 DB의 applied로 매핑됨
                .rate(parseRate(rate))
                // 참고: XML 파싱 시점의 applyStart/End는 yyyyMMdd이므로,
                // RecruitmentMapper에서 별도로 OffsetDateTime으로 변환해줘야 함.
                .build();
    }

    /**
     * [Helper] yyyyMMdd 형식의 문자열을 OffsetDateTime으로 파싱 (XML 파싱용)
     */
    private OffsetDateTime parseDate(String yyyymmdd) {
        try {
            if (yyyymmdd == null || yyyymmdd.isBlank()) return null;
            // ISO 8601 형식(fromEntity)이 들어오면 파싱 실패하여 catch로 빠지고 null 반환
            return OffsetDateTime.parse(
                    yyyymmdd + "T00:00:00+09:00",
                    DateTimeFormatter.ISO_OFFSET_DATE_TIME
            );
        } catch (Exception e) {
            // yyyyMMdd 형식이 아니면(예: ISO 8601) null 반환
            return null;
        }
    }

    /**
     * [Helper] 문자열 -> Integer 파싱 (XML 파싱용)
     */
    private Integer parseInt(String value) {
        try {
            return (value == null || value.isBlank()) ? null : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * [Helper] 문자열 -> BigDecimal 파싱 (XML 파싱용)
     */
    private BigDecimal parseRate(String value) {
        try {
            return (value == null || value.isBlank()) ? null : new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}