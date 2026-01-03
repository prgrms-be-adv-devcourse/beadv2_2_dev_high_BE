package com.dev_high.common.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("com.dev_high")
@EnableJpaRepositories("com.dev_high")
@ComponentScan("com.dev_high")
public class CommonConfig {

}
