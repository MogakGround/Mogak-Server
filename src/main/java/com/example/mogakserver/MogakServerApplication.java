package com.example.mogakserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableJpaAuditing
@SpringBootApplication
@EnableFeignClients(basePackages = "com.example.mogakserver.external.kakao.feign")
@EnableScheduling
public class MogakServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MogakServerApplication.class, args);
    }

}
