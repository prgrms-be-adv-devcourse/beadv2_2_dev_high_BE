package com.dev_high.auction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EntityScan(basePackages = {
    "com.dev_high.auction.domain",
    "com.dev_high.product.domain" // Product 엔티티 포함
})
@SpringBootApplication(scanBasePackages = {"com.dev_high.auction", "com.dev_high.common"})
@EnableDiscoveryClient
public class AuctionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuctionApplication.class, args);
    }

}
