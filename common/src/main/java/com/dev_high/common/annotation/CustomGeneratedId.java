package com.dev_high.common.annotation;

import com.dev_high.common.util.CustomIdGenerator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import org.hibernate.annotations.IdGeneratorType;
import java.lang.annotation.Target;


@IdGeneratorType(CustomIdGenerator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomGeneratedId {
  String method();
}
