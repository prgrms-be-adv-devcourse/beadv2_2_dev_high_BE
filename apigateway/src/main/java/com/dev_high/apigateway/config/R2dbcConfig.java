package com.dev_high.apigateway.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;
import static io.r2dbc.spi.ConnectionFactoryOptions.PASSWORD;
import static io.r2dbc.spi.ConnectionFactoryOptions.USER;

@Configuration
public class R2dbcConfig {

    @Bean
    @Qualifier("queryConnectionFactory")
    public ConnectionFactory queryConnectionFactory(
            @Value("${spring.r2dbc.url}") String r2dbcUrl,
            @Value("${spring.r2dbc.username}") String username,
            @Value("${spring.r2dbc.password}") String password
    ) {
        ConnectionFactoryOptions base = ConnectionFactoryOptions.parse(r2dbcUrl);

        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .from(base)
                .option(ConnectionFactoryOptions.USER, username)
                .option(ConnectionFactoryOptions.PASSWORD, password)
                .build();

        ConnectionFactory raw = ConnectionFactories.get(options);

        ConnectionPoolConfiguration config = ConnectionPoolConfiguration.builder(raw)
                .validationQuery("SELECT 1")
                .maxIdleTime(Duration.ofMinutes(30))
                .maxAcquireTime(Duration.ofSeconds(5))
                .maxCreateConnectionTime(Duration.ofSeconds(5))
                .build();

        return new ConnectionPool(config);
    }

    @Bean
    @Qualifier("listenConnectionFactory")
    public ConnectionFactory listenConnectionFactory(
            @Value("${spring.r2dbc.url}") String r2dbcUrl,
            @Value("${spring.r2dbc.username}") String username,
            @Value("${spring.r2dbc.password}") String password
    ) {
        ConnectionFactoryOptions base = ConnectionFactoryOptions.parse(r2dbcUrl);

        ConnectionFactoryOptions options = ConnectionFactoryOptions.builder()
                .from(base)
                .option(USER, username)
                .option(PASSWORD, password)
                .build();

        return ConnectionFactories.get(options);
    }
}
