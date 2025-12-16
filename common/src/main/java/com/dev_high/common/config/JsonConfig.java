package com.dev_high.common.config;


import static com.dev_high.common.util.DateUtil.DEFAULT_FORMATTER;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JsonConfig {

  private static final LocalDateTimeSerializer LOCAL_DATETIME_SERIALIZER =
      new LocalDateTimeSerializer(DEFAULT_FORMATTER);

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper mapper = new ObjectMapper();

    JavaTimeModule javaTimeModule = new JavaTimeModule();
    javaTimeModule.addSerializer(LOCAL_DATETIME_SERIALIZER);

    mapper.registerModule(javaTimeModule);
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    return mapper;
  }
}
