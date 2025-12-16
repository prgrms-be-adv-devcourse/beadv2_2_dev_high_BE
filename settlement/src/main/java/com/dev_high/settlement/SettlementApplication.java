package com.dev_high.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
    "com.dev_high.settlement",
    "com.dev_high.common"
})
public class SettlementApplication {

  public static void main(String[] args) {
    SpringApplication.run(SettlementApplication.class, args);
  }

}
