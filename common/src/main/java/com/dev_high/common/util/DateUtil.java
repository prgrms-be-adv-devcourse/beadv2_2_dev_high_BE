package com.dev_high.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final DateTimeFormatter DEFAULT_FORMATTER =
            DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

    private DateUtil() {}

    // 현재 시간 (항상 KST)
    public static OffsetDateTime now() {
        return OffsetDateTime.now(KST);
    }

    // 문자열 → OffsetDateTime (오프셋 없는 입력은 KST로 해석)
    public static OffsetDateTime parse(String dateStr) {
        LocalDateTime ldt = LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
        return ldt.atZone(KST).toOffsetDateTime();
    }

    public static OffsetDateTime parse(String dateStr, String pattern) {
        LocalDateTime ldt =
                LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
        return ldt.atZone(KST).toOffsetDateTime();
    }

    // OffsetDateTime → 문자열 (표시용)
    public static String format(OffsetDateTime dateTime) {
        return dateTime.format(DEFAULT_FORMATTER);
    }

    public static String format(OffsetDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    // 현재 시간 문자열
    public static String nowStr() {
        return format(now());
    }

    public static String nowStr(String pattern) {
        return format(now(), pattern);
    }
}
