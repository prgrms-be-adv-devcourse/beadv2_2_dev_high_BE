package com.dev_high.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

  private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
  private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern(DEFAULT_PATTERN);

  private DateUtil() {}

  // 현재 날짜/시간
  public static LocalDateTime now() {
    return LocalDateTime.now();
  }

  // 현재 날짜/시간 문자열 (기본 패턴)
  public static String nowStr() {
    return now().format(DEFAULT_FORMATTER);
  }

  // 현재 날짜/시간 문자열 (커스텀 패턴)
  public static String nowStr(String pattern) {
    return now().format(DateTimeFormatter.ofPattern(pattern));
  }

  // 문자열 → LocalDateTime
  public static LocalDateTime parse(String dateStr) {

    return LocalDateTime.parse(dateStr, DEFAULT_FORMATTER);
  }

  public static LocalDateTime parse(String dateStr, String pattern) {
    return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
  }

  // LocalDateTime → 문자열
  public static String format(LocalDateTime dateTime) {
    return dateTime.format(DEFAULT_FORMATTER);
  }

  public static String format(LocalDateTime dateTime, String pattern) {
    return dateTime.format(DateTimeFormatter.ofPattern(pattern));
  }
}
