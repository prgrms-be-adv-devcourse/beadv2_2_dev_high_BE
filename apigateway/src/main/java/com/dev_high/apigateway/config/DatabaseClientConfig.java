package com.dev_high.apigateway.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
public class DatabaseClientConfig {

    @Bean
    @Qualifier("queryDatabaseClient")
    public DatabaseClient queryDatabaseClient(
            @Qualifier("queryConnectionFactory") ConnectionFactory cf
    ) {
        return DatabaseClient.builder()
                .connectionFactory(cf)
                .build();
    }
}
