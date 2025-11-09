package army.helper.service;

import army.helper.domain.Branch;
import army.helper.domain.Recruitment;
import army.helper.dto.RecruitmentStatusResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class RecruitmentMapper {

    private RecruitmentMapper() {} // 정적 유틸 전용

    public static Recruitment toEntity(RecruitmentStatusResponse dto) {
        return Recruitment.builder()
                .branch(toBranch(dto.getBranch()))
                .mojipGbnm(dto.getMojipGbnm())
                .code(dto.getSpecialtyCode())
                .specialtyName(dto.getSpecialtyName())
                .quota(parseInt(dto.getQuota()))
                .applied(parseInt(dto.getAcceptPcnt()))
                .applyStart(parseDate(dto.getApplyStart()))
                .applyEnd(parseDate(dto.getApplyEnd()))
                .status(dto.getMojipStatus())
                .rate(parseDecimal(dto.getRate()))
                .enlistDate(parseDate(dto.getEnlistStart()))
                .build();
    }

    private static Branch toBranch(String raw) {
        return Branch.fromRaw(raw);
    }

    private static boolean hasText(String text) {
        return text != null && !text.isBlank();
    }

    private static Integer parseInt(String raw) {
        try {
            return hasText(raw) ? Integer.parseInt(raw.trim()) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static BigDecimal parseDecimal(String raw) {
        try {
            return hasText(raw) ? new BigDecimal(raw.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static OffsetDateTime parseDate(String dateString) {
        if (dateString == null || dateString.isBlank()) {
            return null;
        }

        try {
            LocalDate localDate;
            // 2. 날짜 형식이 8자리(yyyyMMdd)인 경우
            if (dateString.length() == 8) {
                localDate = LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyyMMdd"));
            }
            // 3. 날짜 형식이 6자리(yyyyMM)인 경우 (예: iyyjsijakYm)
            else if (dateString.length() == 6) {
                // 해당 월의 1일로 설정
                YearMonth yearMonth = YearMonth.parse(dateString, DateTimeFormatter.ofPattern("yyyyMM"));
                localDate = yearMonth.atDay(1);
            }
            // 4. 그 외 알 수 없는 형식
            else {
                log.warn("Invalid date format detected: {}", dateString);
                return null;
            }

            // 5. 한국 시간(KST, +09:00) 기준으로 자정(00:00)으로 변환
            return localDate.atStartOfDay(ZoneId.of("Asia/Seoul")).toOffsetDateTime();

        } catch (Exception e) {
            log.warn("Failed to parse date string: {}", dateString, e);
            return null; // 파싱 실패 시 null 반환
        }
    }
}