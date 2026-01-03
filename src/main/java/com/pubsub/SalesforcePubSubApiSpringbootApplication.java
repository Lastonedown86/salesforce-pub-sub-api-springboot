package com.pubsub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SalesforcePubSubApiSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesforcePubSubApiSpringbootApplication.class, args);
    }

}
