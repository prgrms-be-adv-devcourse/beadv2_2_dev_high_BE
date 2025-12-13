package com.dev_high.common.util;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {

  private static final ObjectMapper mapper = new ObjectMapper();

  private JsonUtil() {}

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
}
