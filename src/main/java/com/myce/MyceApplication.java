package com.myce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableJpaRepositories(basePackages = {
    "com.myce.advertisement.repository",
    "com.myce.expo.repository", 
    "com.myce.member.repository",
    "com.myce.payment.repository",
    "com.myce.qrcode.repository",
    "com.myce.reservation.repository",
    "com.myce.system.repository",
    "com.myce.common.repository"
})
@EnableMongoRepositories(basePackages = {
    "com.myce.chat.repository",
    "com.myce.notification.repository"
})
public class MyceApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyceApplication.class, args);
    }

}
