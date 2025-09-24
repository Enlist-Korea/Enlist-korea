package com.militarysupport.recruit_helper.config;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
public class RecruitParsing {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private static final DateTimeFormatter YM = DateTimeFormatter.ofPattern("yyyyMM");
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public int toIntSafe(String s) {
        try { return (s == null || s.isBlank()) ? 0 : Integer.parseInt(s); }
        catch (NumberFormatException e) { return 0; }
    }

    public BigDecimal toDecimalSafe(String s) {
        try { return (s == null || s.isBlank()) ? null : new BigDecimal(s); }
        catch (NumberFormatException e) { return null; }
    }

    public OffsetDateTime toDateTimeSafe(String s) {
        try { return (s == null || s.isBlank()) ? null
                : LocalDateTime.parse(s, DT).atZone(KST).toOffsetDateTime(); }
        catch (DateTimeParseException e) { return null; }
    }

    public YearMonth toYearMonthSafe(String s) {
        try { return (s == null || s.isBlank()) ? null : YearMonth.parse(s, YM); }
        catch (DateTimeParseException e) { return null; }
    }
}