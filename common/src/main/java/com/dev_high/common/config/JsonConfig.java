package com.dev_high.common.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.OffsetDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.OffsetDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.OffsetDateTime;

import static com.dev_high.common.util.DateUtil.DEFAULT_FORMATTER;

@Configuration
public class JsonConfig {

    private static final OffsetDateTimeSerializer LOCAL_DATETIME_SERIALIZER =
            new OffsetDateTimeSerializer(DEFAULT_FORMATTER);

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LOCAL_DATETIME_SERIALIZER);
        javaTimeModule.addDeserializer(
                OffsetDateTime.class,
                new OffsetDateTimeDeserializer(DEFAULT_FORMATTER)
        );
        mapper.registerModule(javaTimeModule);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        return mapper;
    }
}
