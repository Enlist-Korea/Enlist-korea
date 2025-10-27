package com.militarysupport.scoring.support;

/**
 * 출결 라벨 파서.
 * - "0일"       -> [0,0]
 * - "1-4일"/"1–4일" (하이픈/엔대시) -> [1,4]
 * - "9일+"      -> [9, 무한대]
 */
public final class AttendanceParser {
    private AttendanceParser() {}

    public static AttendanceBand parse(String itemLevel) {
        if (itemLevel == null) return new AttendanceBand(0, Integer.MAX_VALUE);
        String s = itemLevel.replace("일", "").trim();
        if (s.endsWith("+")) {
            int min = Integer.parseInt(s.substring(0, s.length()-1));
            return new AttendanceBand(min, Integer.MAX_VALUE);
        }
        if (s.contains("-") || s.contains("–")) {
            String[] parts = s.split("[-–]");
            int a = Integer.parseInt(parts[0].trim());
            int b = Integer.parseInt(parts[1].trim());
            return new AttendanceBand(a, b);
        }
        int v = Integer.parseInt(s);
        return new AttendanceBand(v, v);
    }
}
