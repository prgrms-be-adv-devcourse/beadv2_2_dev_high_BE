package com.dev_high.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  @Bean
  public OpenAPI openAPI() {
    return new OpenAPI()
        .addServersItem(new Server().url("/"))
        .addSecurityItem(new SecurityRequirement().addList("JWT"))
        .components(new Components()
            .addSecuritySchemes("JWT",
                new SecurityScheme()
                    .name("Authorization")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
            )
        )
        .info(new Info().title("API").version("v1"));
  }

  @Bean
  public OperationCustomizer customize() {
    return (operation, handlerMethod) -> {
      operation.addSecurityItem(new SecurityRequirement().addList("JWT"));
      return operation;
    };
  }
}