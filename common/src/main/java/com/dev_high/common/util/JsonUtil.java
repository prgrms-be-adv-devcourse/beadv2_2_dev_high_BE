package com.dev_high.common.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


public class JsonUtil {

  private static final ObjectMapper mapper = new ObjectMapper()
      .registerModule(new JavaTimeModule()) // LocalDateTime 지원
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

  private JsonUtil() {
  }

  public static String toJson(Object obj) {
    try {
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException("JSON 변환 실패", e);
    }
  }

  public static <T> T fromJson(String json, Class<T> clazz) {
    try {
      return mapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new RuntimeException("JSON 파싱 실패", e);
    }
  }

  public static <T> T fromPayload(Object payload, Class<T> clazz) {
    try {
      String json = mapper.writeValueAsString(payload);
      return mapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new RuntimeException("Payload 변환 실패: " + clazz.getSimpleName(), e);
    }
  }
}
