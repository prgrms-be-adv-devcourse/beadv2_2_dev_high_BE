package com.dev_high.user.notification.infrastructure.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/*
* Java의 boolean 타입과 DB의 'Y'/'N' char(1) 타입 간의 변환시 사용
* */
@Converter(autoApply = true)
public class BooleanToYNConverter implements AttributeConverter<Boolean, Character> {

    // Java (Boolean) -> DB (Character)
    @Override
    public Character convertToDatabaseColumn(Boolean attribute) {
        // attribute가 null이 아니고, true일 경우에만 'Y'를 반환
        // 그 외의 경우 (null 또는 false)에는 'N'을 반환
        return (attribute != null && attribute) ? 'Y' : 'N';
    }

    // DB (Character) -> Java (Boolean)
    @Override
    public Boolean convertToEntityAttribute(Character dbData) {
        // DB 데이터가 'Y'이면 true, 그 외 (null, 'N' 등)는 false를 반환
        return 'Y' == dbData;
    }
}
