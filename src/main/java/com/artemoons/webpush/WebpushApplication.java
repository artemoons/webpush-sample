package com.artemoons.webpush;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WebpushApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebpushApplication.class, args);
    }

}
