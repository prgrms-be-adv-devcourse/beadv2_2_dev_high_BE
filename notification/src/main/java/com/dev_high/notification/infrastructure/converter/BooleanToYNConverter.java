package com.dev_high.notification.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/*
* Java의 boolean 타입과 DB의 'Y'/'N' char(1) 타입 간의 변환시 사용
* */
@Converter(autoApply = true)
public class BooleanToYNConverter implements AttributeConverter<Boolean, String> {

    // Java (Boolean) -> DB (String)
    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        // true면 "Y", false면 "N"를 반환
        return (attribute) ? "Y" : "N";
    }

    // DB (String) -> Java (Boolean)
    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        // DB 데이터가 "Y"이거나 "y"이면 true를 반환, 아니면 false 반환
        return "Y".equalsIgnoreCase(dbData);
    }
}
