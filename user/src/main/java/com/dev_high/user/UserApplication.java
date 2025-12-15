package com.dev_high.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EntityScan(basePackages = {
        "com.dev_high.user.user.domain",
        "com.dev_high.user.seller.domain",
        "com.dev_high.user.wishlist.domain",
        "com.dev_high.product.domain" // Product 엔티티 포함
})
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {"com.dev_high.user", "com.dev_high.common"})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
